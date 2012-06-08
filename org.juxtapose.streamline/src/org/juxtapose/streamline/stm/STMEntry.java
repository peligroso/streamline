package org.juxtapose.streamline.stm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.producer.executor.IExecutor;
import org.juxtapose.streamline.util.ISTMEntrySubscriber;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeNull;
import org.juxtapose.streamline.util.data.DataTypeRef;

import com.trifork.clj_ds.IPersistentMap;

/**
 * @author Pontus Jörgne
 * 28 jun 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 * 
 * This class belongs to the the STM. PublishedData is an STM entry
 * only STM may create or modify a PublishedData on pub/sub requests
 *
 */
final class STMEntry implements ISTMEntry
{
	final IPersistentMap<String, DataType<?>> dataMap;
	final Set<String> deltaSet;
	
	final PersistentArrayList<ISTMEntrySubscriber> lowPrioSubscribers;
	final PersistentArrayList<ISTMEntrySubscriber> highPrioSubscribers;
	
	
	final ISTMEntryProducer producer;
	
	final Status status;
	
	final long sequenceID;
	
	final boolean completeVersion;
	
	/**
	 * @param inData
	 * @param inLastUpdate
	 * @param inSubscribers
	 * @param inProducer
	 * @param inStatus
	 */
	protected STMEntry( IPersistentMap<String, DataType<?>> inData, Set<String> inChanges, PersistentArrayList<ISTMEntrySubscriber> inLowPrioSubscribers, PersistentArrayList<ISTMEntrySubscriber> inHighPrioSubscribers, ISTMEntryProducer inProducer, Status inStatus, long inSequenceID, boolean inCompleteUpdate ) 
	{
		dataMap = inData;
		deltaSet = Collections.unmodifiableSet( inChanges );
		lowPrioSubscribers = inLowPrioSubscribers;
		highPrioSubscribers = inHighPrioSubscribers;
		producer = inProducer;
		status = inStatus;
		sequenceID = inSequenceID;
		completeVersion = inCompleteUpdate;
	}
	
	public void updateSubscribers( ISTMEntryKey inKey )
	{
		for( int i = 0; i < highPrioSubscribers.size(); i++ )
		{
			ISTMEntrySubscriber subscriber = highPrioSubscribers.get( i );
			subscriber.updateData( inKey, this, false );
		}
		
		for( int i = 0; i < lowPrioSubscribers.size(); i++ )
		{
			ISTMEntrySubscriber subscriber = lowPrioSubscribers.get( i );
			subscriber.updateData( inKey, this, false );
		}
	}
	
	/**
	 * @param inSubscriber
	 * @return
	 */
	public ISTMEntry addSubscriber( ISTMEntrySubscriber inSubscriber )
	{
		if( inSubscriber.getPriority() == IExecutor.HIGH )
		{
			PersistentArrayList<ISTMEntrySubscriber> newSub = highPrioSubscribers.add( inSubscriber );
			return new STMEntry( dataMap, deltaSet, lowPrioSubscribers, newSub, producer, status, sequenceID, completeVersion );
		}
		else
		{
			PersistentArrayList<ISTMEntrySubscriber> newSub = lowPrioSubscribers.add( inSubscriber );
			return new STMEntry( dataMap, deltaSet, newSub, highPrioSubscribers, producer, status, sequenceID, completeVersion );

		}
	}
	
	/**
	 * @param inSubscriber
	 * @return
	 */
	public ISTMEntry removeSubscriber( ISTMEntrySubscriber inSubscriber )
	{ 
		if( inSubscriber.getPriority() == IExecutor.HIGH )
		{
			PersistentArrayList<ISTMEntrySubscriber> newSub = highPrioSubscribers.remove( inSubscriber );
			return new STMEntry( dataMap, deltaSet, lowPrioSubscribers, newSub, producer, status, sequenceID, completeVersion );
		}
		else
		{
			PersistentArrayList<ISTMEntrySubscriber> newSub = highPrioSubscribers.add( inSubscriber );
			return new STMEntry( dataMap, deltaSet, newSub, highPrioSubscribers, producer, status, sequenceID, completeVersion );

		}
	}
	
	/**
	 * @return
	 */
	public boolean hasSubscribers()
	{
		return lowPrioSubscribers.size() > 0 || highPrioSubscribers.size() > 0;
	}
	
	/**
	 * @param inKey
	 * @param inValue
	 * @return
	 * @throws Exception
	 */
	public ISTMEntry putDataValue( String inKey, DataType<?> inValue )throws Exception
	{
		IPersistentMap<String, DataType<?>> newMap;
		
		if( inValue instanceof DataTypeNull )
			newMap = dataMap.without( inKey );
		else
			newMap = dataMap.assoc( inKey, inValue );
		
		return new STMEntry( newMap, deltaSet, lowPrioSubscribers, highPrioSubscribers, producer, status, sequenceID+1, completeVersion );
	}
	
	/**
	 * @param inStateTransitionMap
	 * @return
	 * @throws Exception
	 */
	public ISTMEntry putDataValues( HashMap<String, DataType<?>> inStateTransitionMap )throws Exception
	{
		IPersistentMap<String, DataType<?>> newDataMap = dataMap;
		
		for( String key : inStateTransitionMap.keySet() )
		{
			DataType<?> value = inStateTransitionMap.get( key );
			if( value instanceof DataTypeNull )
				newDataMap = newDataMap.without( key );
			else if( value instanceof DataTypeRef )
			{
				newDataMap = newDataMap.assoc( key, value );
			}
			else
			{
				newDataMap = newDataMap.assoc( key, value );
			}
		}
		
		return new STMEntry( newDataMap, deltaSet, lowPrioSubscribers, highPrioSubscribers, producer, status, sequenceID+1, completeVersion );
	}
	
	/**
	 * @param inDataMap
	 * @return
	 */
	public ISTMEntry setDataMap( IPersistentMap<String, DataType<?>> inDataMap )
	{
		return new STMEntry( inDataMap, deltaSet, lowPrioSubscribers, highPrioSubscribers, producer, status, sequenceID+1, completeVersion );
	}
	
	/**
	 * @param inDataMap
	 * @return
	 */
	public ISTMEntry setUpdatedData( IPersistentMap<String, DataType<?>> inDataMap, Set<String> inDelta, Status inStatus, boolean inCompleteUpdate )
	{
		return new STMEntry( inDataMap, inDelta, lowPrioSubscribers, highPrioSubscribers, producer, inStatus, (inCompleteUpdate ? sequenceID+1 : sequenceID), inCompleteUpdate );
	}
	
	public boolean isCompleteVersion()
	{
		return completeVersion;
	}

	
	/**
	 * @return
	 */
	public IPersistentMap<String, DataType<?>> getDataMap()
	{
		return dataMap;
	}
	
	/**
	 * @return
	 */
	public Set<String> getDeltaSet()
	{
		return deltaSet;
	}
	
	public ISTMEntryProducer getProducer()
	{
		return producer;
	}
	
	/**
	 * @param inKey
	 * @return
	 */
	public DataType<?> getValue( String inKey )
	{
		return dataMap.valAt( inKey );
	}

	@Override
	public boolean isDeltaValue(String inKey)
	{
		return deltaSet.contains( inKey );
	}
	
	public Status getStatus()
	{
		return status;
	}

	public long getSequenceID()
	{
		return sequenceID;
	}
	
	public int getHighPriosubscriberCount()
	{
		return highPrioSubscribers.size();
	}
	
	public int getPriority()
	{
		return highPrioSubscribers.size() > 0 ? IExecutor.HIGH : IExecutor.LOW;
	}

	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.util.ISTMEntry#changeSubscriberPriority(org.juxtapose.streamline.util.ISTMEntrySubscriber, int)
	 * this method will return null if the subscriber is not contained within the anticipated list.
	 */
	@Override
	public ISTMEntry changeSubscriberPriority( ISTMEntrySubscriber inSubscriber, int inNewPriority ) 
	{
		if( inNewPriority == IExecutor.HIGH )
		{
			PersistentArrayList<ISTMEntrySubscriber> newLowPrioSubscribers = lowPrioSubscribers.remove( inSubscriber );
			if( newLowPrioSubscribers == lowPrioSubscribers )
			{
				return null;
			}
			PersistentArrayList<ISTMEntrySubscriber> newHighPrioSubscribers = highPrioSubscribers.add( inSubscriber );
			
			return new STMEntry( dataMap, deltaSet, newLowPrioSubscribers, newHighPrioSubscribers, producer, status, sequenceID, completeVersion );
		}
		else if( inNewPriority == IExecutor.LOW )
		{
			PersistentArrayList<ISTMEntrySubscriber> newHighPrioSubscribers = highPrioSubscribers.remove( inSubscriber );
			if( newHighPrioSubscribers == highPrioSubscribers )
			{
				return null;
			}
			PersistentArrayList<ISTMEntrySubscriber> newLowPrioSubscribers = lowPrioSubscribers.add( inSubscriber );
			
			return new STMEntry( dataMap, deltaSet, newLowPrioSubscribers, newHighPrioSubscribers, producer, status, sequenceID, completeVersion );
		}
		else 
		{
			return this;
		}
	}
	
}
