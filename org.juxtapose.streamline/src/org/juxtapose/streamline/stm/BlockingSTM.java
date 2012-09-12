package org.juxtapose.streamline.stm;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.producer.ISTMEntryProducerService;
import org.juxtapose.streamline.producer.executor.IExecutor;
import org.juxtapose.streamline.util.ISTMEntrySubscriber;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeRef;

import com.trifork.clj_ds.IPersistentMap;

/**
 * @author Pontus Jörgne
 * Jan 6, 2012
 * Copyright (c) Pontus Jörgne. All rights reserved
 * STM implementation that uses locking as synchronization method around transactions
 */
public class BlockingSTM extends STM
{
	//Fair locking implies that first come, first serve. Fair locking = false may lead to unwanted behavior on highly contended data 
	public final boolean FAIR_LOCKING = true; 
	private final ConcurrentHashMap<String, ReentrantLock> m_keyToLock = new ConcurrentHashMap<String, ReentrantLock>();
	
	/**
	 * @param inKey
	 */
	private void lock( String inKey )
	{
		boolean set = false;
		
		do
		{
			ReentrantLock lock = m_keyToLock.get( inKey );
			if( lock != null )
			{
				lock.lock();
				set = m_keyToLock.replace( inKey, lock, lock );
				if( ! set )
					lock.unlock();
			}
			else
			{
				lock = new ReentrantLock( FAIR_LOCKING );
				lock.lock();
				set = null == m_keyToLock.putIfAbsent( inKey, lock );
			}
		}while( !set );
	}
	
	/**
	 * @param inKey
	 */
	private void unlock( String inKey )
	{
		ReentrantLock lock = m_keyToLock.get( inKey );
		if( lock != null )
		{
			lock.unlock();
		}
		else
		{
			logError("Tried to unlock already disposed lock");
		}
	}

	
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.impl.STM#commit(org.juxtapose.streamline.stm.impl.Transaction)
	 */
	public void commit( STMTransaction inTransaction )
	{	
		ISTMEntryKey dataKey = inTransaction.getDataKey();
		
		ISTMEntry newData = null;
		
		ReferenceLink[] addedLinks = null;
		ReferenceLink[] removedLinks = null;
		TemporaryController[] removedDependencies = null;
		
		lock( dataKey.getKey() );
		
		try
		{
			ISTMEntry existingData = keyToData.get( dataKey.getKey() );
			if( existingData == null )
			{
				//data has been removed due to lack of interest, transaction is discarded
				return;
			}

			if( !STMUtil.validateProducerToData(existingData, inTransaction) )
			{
				logError( "Wrong version DataProducer tried to update data: "+dataKey );
				//The producer for this data is of the wrong version, Transaction is discarded
				return;
			}

			inTransaction.putInitDataState( existingData.getDataMap(), existingData.getStatus() );
			
			inTransaction.execute();
			
			if( inTransaction.isDisposed() )
			{
				return;
			}
			IPersistentMap<String, DataType<?>> inst = inTransaction.getStateInstruction();
			Set<String> delta = inTransaction.getDeltaState();
			if( !existingData.isCompleteVersion() )
			{
				/**If previous update was a partial update we need to merge the deltas**/
				delta.addAll( existingData.getDeltaSet() );
			}
			newData = existingData.setUpdatedData( inst, delta, inTransaction.getStatus(), inTransaction.isCompleteStateTransition() );
			
			keyToData.put( dataKey.getKey(), newData );
			
			if( inTransaction.containesReferenceInstructions() )
			{
				//Init reference links
				Map< String, DataTypeRef > dataReferences = inTransaction.getAddedReferences();
				addedLinks = dataReferences == null ? null : new ReferenceLink[ dataReferences.size() ];

				if( dataReferences == null || !dataReferences.isEmpty() )
				{
					ISTMEntryProducer producer = newData.getProducer();
					if( producer == null )
						logError( "Tried to add reference to data with null producer" );
					else
					{
						int i = 0;
						for( String fieldKey : dataReferences.keySet() )
						{
							DataTypeRef ref = dataReferences.get( fieldKey );
							ReferenceLink refLink = new ReferenceLink( producer, this, fieldKey, ref );
							producer.addDataReferences( fieldKey, refLink );
							addedLinks[i] = refLink;
							i++;
						}
					}
				}


				//Dispose reference links
				List< String > removedReferences = inTransaction.getRemovedReferences();
				removedLinks = removedReferences == null ? null : new ReferenceLink[ removedReferences.size() ];

				if( removedReferences != null && !removedReferences.isEmpty() )
				{
					ISTMEntryProducer producer = newData.getProducer();
					if( producer == null )
						logError( "Tried to remove reference from data with null producer" );
					else
					{
						int i = 0;
						for( String fieldKey : dataReferences.keySet() )
						{
							ReferenceLink refLink = producer.removeReferenceLink( fieldKey );
							if( refLink == null )
							{
								logError( "Tried to remove reference Link that is not stored in the producer" );
							}
							removedLinks[i] = refLink;
							i++;
						}
					}
				}
			}
			
			if( inTransaction instanceof DependencyTransaction )
			{
				ISTMEntryProducer producer = newData.getProducer();
				if( producer == null )
				{
					logError( "Tried to add dependency to data with null producer" );
					return;
				}
				removedDependencies = prepareDependencies( (DependencyTransaction)inTransaction, producer );
			}
			
		}catch( Throwable t )
		{
			logError( t.getMessage(), t );
		}
		finally
		{
			unlock( dataKey.getKey() );
		}
		
		
		if( newData != null )
		{
			/**Incomplete stateTransition does not go out as an update to subscribers**/
			if( inTransaction.isCompleteStateTransition() )
			{
				newData.updateSubscribers( dataKey );
			}
		}
		
		if( inTransaction.containesReferenceInstructions() )
		{
			if( addedLinks != null )
			{
				for( ReferenceLink link : addedLinks )
				{
					link.init();
				}
			}

			if( removedLinks != null )
			{
				for( ReferenceLink link : removedLinks )
				{
					link.dispose();
				}
			}
		}
		
		if( inTransaction instanceof DependencyTransaction )
		{
			executeDependencies( (DependencyTransaction)inTransaction, newData.getProducer(), removedDependencies );
		}
	}
	
	/**
	 * @param inTransaction
	 * @param inProducer
	 * @return
	 */
	private TemporaryController[] prepareDependencies( DependencyTransaction inTransaction, ISTMEntryProducer inProducer )
	{
		Map< String, TemporaryController > dependencies = inTransaction.getAddedDependencies();
		for( String key : dependencies.keySet() )
		{
			TemporaryController controller = dependencies.get( key ); 
			inProducer.addDependency( key, controller );
		}
		
		List< String > removedDependencies = inTransaction.getRemovedDependencies();
		TemporaryController[] removedControllers = new TemporaryController[removedDependencies.size()];
		int i = 0;
		for( String key : removedDependencies )
		{
			TemporaryController controller = inProducer.removeDependency( key );
			removedControllers[i] = controller;
			i++;
		}
		
		return removedControllers;
	}
	
	/**
	 * @param inTransaction
	 * @param inProducer
	 */
	private void executeDependencies( DependencyTransaction inTransaction, ISTMEntryProducer inProducer, TemporaryController[] inRemovedDependencies )
	{
		Map< String, TemporaryController > dependencies = inTransaction.getAddedDependencies();
		for( TemporaryController controller : dependencies.values() )
		{
			controller.init();
		}
		
		for( TemporaryController controller : inRemovedDependencies )
		{
			controller.dispose();
		}
	}
	
	public void updateSubscriberPriority( ISTMEntryKey inDataKey, ISTMEntrySubscriber inSubscriber )
	{
		lock( inDataKey.getKey() );
		
		ISTMEntryProducer producer = null;
		int newPriority = 0;
		
		try
		{
			ISTMEntry existingData = keyToData.get( inDataKey.getKey() );

			if( existingData == null )
			{
				//All Subscribers has left the building
				return;
			}
			
			ISTMEntry newEntry = existingData.changeSubscriberPriority( inSubscriber, inSubscriber.getPriority() );
			
			if( newEntry == null )
			{
				//Subscriber has left the entry
				return;
			}
			
			if( existingData.getPriority() == newEntry.getPriority() )
			{
				//No side effects
				return;
			}
			
			newPriority = newEntry.getPriority();
			producer = newEntry.getProducer();
			
			/**
			 * Ok, normally we would just to state changing inside a transaction, 
			 * but in this case we need to reach out into the producer and ongoing into its dependencies to update the priority.
			 * Even though we are reaching far beyond our domain and possible across multiple locks in the STM it might be thought of as a long
			 * run of state changes. setPriority should just switch a variable and cascade it no.
			 * 
			 * -On second though not so sure, this could cause deadlocks if other parts of the system uses nestled locking, like updates.
			 * Isn't is better to just send an unsynchronized call to the producer and let him sync his own subscriber list?? 
			 */
			producer.setPriority( newPriority );

		}
		catch( Throwable t )
		{
			logError( t.getStackTrace().toString() );
		}
		finally
		{
			unlock( inDataKey.getKey() );
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.impl.STM#subscribe(org.juxtapose.streamline.util.producer.IDataKey, org.juxtapose.streamline.util.IDataSubscriber)
	 */
	public void subscribeToData( ISTMEntryKey inDataKey, ISTMEntrySubscriber inSubscriber )
	{
		ISTMEntryProducerService producerService = idToProducerService.get( inDataKey.getService() );
		if( producerService == null )
		{
			logError( "Key: "+inDataKey+" not valid, producer service does not exist"  );
			return;
		}
		
		ISTMEntryProducer producer = null;
		ISTMEntry newData = null;
		
		lock( inDataKey.getKey() );
		
		try
		{
			ISTMEntry existingData = keyToData.get( inDataKey.getKey() );

			if( existingData == null )
			{
				//First subscriber
				producer = producerService.getDataProducer( inDataKey );

				//REVISIT Potentially we should not notify subscribers for certain newDatas and just wait for the initial update instead.
				newData = createEmptyData( Status.ON_REQUEST, producer, inSubscriber);
				keyToData.put( inDataKey.getKey(), newData );

				if( inSubscriber.getPriority() == IExecutor.HIGH )
					producer.setPriority( IExecutor.HIGH );
			}
			else
			{
				newData = existingData.addSubscriber( inSubscriber );
				keyToData.put( inDataKey.getKey(), newData );

				if( newData.getPriority() != existingData.getPriority() )
					newData.getProducer().setPriority( newData.getPriority() );
			}

		}catch( Throwable t )
		{
			logError( t.getStackTrace().toString() );
		}
		finally
		{
			unlock( inDataKey.getKey() );
		}

		if( newData != null )
			inSubscriber.updateData( inDataKey, newData, true );

		if( producer != null )
			producer.init();
	}

	
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.exp.ISTM#unsubscribeToData(org.juxtapose.streamline.producer.IDataKey, org.juxtapose.streamline.util.IDataSubscriber)
	 */
	@Override
	public void unsubscribeToData(ISTMEntryKey inDataKey, ISTMEntrySubscriber inSubscriber)
	{
		ISTMEntryProducerService producerService = idToProducerService.get( inDataKey.getService() );
		if( producerService == null )
		{
			logError( "Key: "+inDataKey+" not valid, producer service does not exist"  );
			return;
		}
		
		ISTMEntryProducer producer = null;
		
		lock( inDataKey.getKey() );
		
		try
		{
			ISTMEntry existingData = keyToData.get( inDataKey.getKey() );

			if( existingData == null )
			{
				logError( "Key: "+inDataKey+", Data has already been removed which is unconditional since an existing subscriber is requesting to unsubscribe"  );
				return;
			}
			else
			{
				ISTMEntry newData = existingData.removeSubscriber( inSubscriber );
				if( newData.hasSubscribers() )
				{
					keyToData.replace( inDataKey.getKey(), newData );
				}
				else
				{
					keyToData.remove( inDataKey.getKey() );
					producer = existingData.getProducer();
				}
			}

		}catch( Throwable t )
		{
			logError( t.getStackTrace().toString() );
		}
		finally
		{
			unlock( inDataKey.getKey() );
		}

		if( producer != null )
			producer.dispose();
		
	}
}
