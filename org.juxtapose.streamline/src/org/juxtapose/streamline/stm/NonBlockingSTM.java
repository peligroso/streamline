package org.juxtapose.streamline.stm;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.producer.ISTMEntryProducerService;
import org.juxtapose.streamline.producer.executor.IExecutor;
import org.juxtapose.streamline.tools.STMAssertionUtil;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntrySubscriber;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeRef;

import com.trifork.clj_ds.IPersistentMap;
/**
 * @author Pontus Jörgne
 * Jan 2, 2012
 * Copyright (c) Pontus Jörgne. All rights reserved
 * 
 * NonBlockngSTM is experimental ant not complete. It exhibits strange behavior and does not support DataTypeRef
 * Use BlockingSTM
 */

/**
 * @author Pontus Jörgne
 * Jan 6, 2012
 * Copyright (c) Pontus Jörgne. All rights reserved
 * STM implementation that uses locking as synchronization method around transactions
 */
public class NonBlockingSTM extends STM
{
	private static final int LOCK_ARR_SIZE = 512;
	private static final int HASH_MASK = LOCK_ARR_SIZE -1;
	
	private final AtomicReference<ReentrantSTMLock>[]  m_hashLocks = new AtomicReference[LOCK_ARR_SIZE];
	
	public void init( IExecutor inExecutor, boolean inMaster )
	{
		for( int i = 0; i < m_hashLocks.length; i++ )
		{
			ReentrantSTMLock lock = new ReentrantSTMLock();
			AtomicReference<ReentrantSTMLock> lockRef = new AtomicReference<ReentrantSTMLock>( lock );
			m_hashLocks[i] = lockRef;
		}
		super.init( inExecutor, inMaster );
		
	}
	
	private int getHash( String inString )
	{
		int hash = inString.hashCode();
		return hash & HASH_MASK;
	}
	/**
	 * @param inKey
	 */
	protected void lock( String inKey )
	{
		lockOrUnlock( inKey, true );
	}
	
	/**
	 * @param inKey
	 */
	protected void unlock( String inKey )
	{
		lockOrUnlock( inKey, false );
	}
	
	/**
	 * @param inKey
	 * @param inLock
	 */
	private void lockOrUnlock( String inKey, boolean inLock )
	{
		int hash = getHash( inKey );
		AtomicReference<ReentrantSTMLock> lockRef = m_hashLocks[hash];
		ReentrantSTMLock newLock = null;
		ReentrantSTMLock lock;
		do
		{
			do
			{
				lock = lockRef.get();
				newLock = inLock ? lock.accuire() : lock.release();
			}
			while( newLock == null );
		}
		while( !lockRef.compareAndSet( lock, newLock ));
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
			IPersistentMap<String, DataType<?>> inst = inTransaction.getStateInstruction();
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
	
	public void updateSubscriberPriority( ISTMEntryKey inDataKey, ISTMEntrySubscriber inSubscriber )
	{
		lock( inDataKey.getKey() );
		
		ISTMEntryProducer producer = null;
		int newPriority = 0;
		
		HashSet<TemporaryController> dependencies = null;
		
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
			
			if( producer == null || producer.isDisposed() )
			{
				//producer is disposed
				return;
			}
			
			dependencies = producer.getDependencyControllers();
		}
		catch( Throwable t )
		{
			logError( t.getMessage(), t );
		}
		finally
		{
			unlock( inDataKey.getKey() );
		}
		
		if( dependencies != null )
		{
			for( TemporaryController tc : dependencies )
			{
				tc.setPriority( newPriority );
			}
		}
		
	}

}
