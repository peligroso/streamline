package org.juxtapose.streamline.stm;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.tools.STMAssertionUtil;
import org.juxtapose.streamline.util.ISTMEntry;
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
	protected void lock( String inKey )
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
	protected void unlock( String inKey )
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

			if( !STMAssertionUtil.validateProducerToData(existingData, inTransaction) )
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
			IPersistentMap<String, Object> inst = inTransaction.getStateInstruction();
			Set<String> delta = inTransaction.getDeltaState();
			if( !existingData.isCompleteVersion() )
			{
				/**If previous update was a partial update we need to merge the deltas**/
				delta.addAll( existingData.getDeltaSet() );
			}
			newData = existingData.setUpdatedData( inst, delta, inTransaction.isCompleteStateTransition() );
			
			keyToData.put( dataKey.getKey(), newData );
			
			if( inTransaction.containesReferenceInstructions() )
			{
				//Init reference links
				Map< String, DataTypeRef > dataReferences = inTransaction.getAddedReferences();
				addedLinks = dataReferences == null ? null : new ReferenceLink[ dataReferences.size() ];

				if( dataReferences != null )
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

				if( removedReferences != null )
				{
					ISTMEntryProducer producer = newData.getProducer();
					if( producer == null )
						logError( "Tried to remove reference from data with null producer" );
					else
					{
						int i = 0;
						for( String fieldKey : removedReferences )
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
				newData.updateSubscribers( dataKey, inTransaction.isFullUpdate() );
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
	

}
