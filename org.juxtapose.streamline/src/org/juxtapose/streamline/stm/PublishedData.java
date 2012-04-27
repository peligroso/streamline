package org.juxtapose.streamline.stm;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.juxtapose.streamline.producer.IDataKey;
import org.juxtapose.streamline.producer.IDataProducer;
import org.juxtapose.streamline.util.IDataSubscriber;
import org.juxtapose.streamline.util.IPublishedData;
import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeNull;
import org.juxtapose.streamline.util.data.DataTypeRef;

import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.IPersistentVector;
import com.trifork.clj_ds.ISeq;
import com.trifork.clj_ds.PersistentTreeMap.Seq;

/**
 * @author Pontus Jörgne
 * 28 jun 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 * 
 * This class belongs to the the STM
 * only STM may create or modify a PublishedData on pub/sub requests
 *
 */
final class PublishedData implements IPublishedData
{
	final IPersistentMap<Integer, DataType<?>> dataMap;
	final Set<Integer> deltaSet;
	
	final PersistentArrayList<IDataSubscriber> subscribers;
	
	
	final IDataProducer producer;
	
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
	protected PublishedData( IPersistentMap<Integer, DataType<?>> inData, Set<Integer> inChanges, PersistentArrayList<IDataSubscriber> inSubscribers, IDataProducer inProducer, Status inStatus, long inSequenceID, boolean inCompleteUpdate ) 
	{
		dataMap = inData;
		deltaSet = Collections.unmodifiableSet( inChanges );
		subscribers = inSubscribers;
		producer = inProducer;
		status = inStatus;
		sequenceID = inSequenceID;
		completeVersion = inCompleteUpdate;
	}
	
	public void updateSubscribers( IDataKey inKey )
	{
		for( int i = 0; i < subscribers.size(); i++ )
		{
			IDataSubscriber subscriber = subscribers.get( i );
			subscriber.updateData( inKey, this, false );
		}
	}
	
	/**
	 * @param inSubscriber
	 * @return
	 */
	public IPublishedData addSubscriber( IDataSubscriber inSubscriber )
	{
		PersistentArrayList<IDataSubscriber> newSub = subscribers.add( inSubscriber );
		return new PublishedData( dataMap, deltaSet, newSub, producer, status, sequenceID, completeVersion );
	}
	
	/**
	 * @param inSubscriber
	 * @return
	 */
	public IPublishedData removeSubscriber( IDataSubscriber inSubscriber )
	{ 
		PersistentArrayList<IDataSubscriber> newSub = subscribers.remove( inSubscriber );
		
		return new PublishedData( dataMap, deltaSet, newSub, producer, status, sequenceID, completeVersion );
	}
	
	/**
	 * @return
	 */
	public boolean hasSubscribers()
	{
		return subscribers.size() > 0;
	}
	
	/**
	 * @param inKey
	 * @param inValue
	 * @return
	 * @throws Exception
	 */
	public IPublishedData putDataValue( Integer inKey, DataType<?> inValue )throws Exception
	{
		IPersistentMap<Integer, DataType<?>> newMap;
		
		if( inValue instanceof DataTypeNull )
			newMap = dataMap.without( inKey );
		else
			newMap = dataMap.assoc( inKey, inValue );
		
		return new PublishedData( newMap, deltaSet, subscribers, producer, status, sequenceID+1, completeVersion );
	}
	
	/**
	 * @param inStateTransitionMap
	 * @return
	 * @throws Exception
	 */
	public IPublishedData putDataValues( HashMap<Integer, DataType<?>> inStateTransitionMap )throws Exception
	{
		IPersistentMap<Integer, DataType<?>> newDataMap = dataMap;
		
		for( Integer key : inStateTransitionMap.keySet() )
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
		
		return new PublishedData( newDataMap, deltaSet, subscribers, producer, status, sequenceID+1, completeVersion );
	}
	
	/**
	 * @param inDataMap
	 * @return
	 */
	public IPublishedData setDataMap( IPersistentMap<Integer, DataType<?>> inDataMap )
	{
		return new PublishedData( inDataMap, deltaSet, subscribers, producer, status, sequenceID+1, completeVersion );
	}
	
	/**
	 * @param inDataMap
	 * @return
	 */
	public IPublishedData setUpdatedData( IPersistentMap<Integer, DataType<?>> inDataMap, Set<Integer> inDelta, Status inStatus, boolean inCompleteUpdate )
	{
		return new PublishedData( inDataMap, inDelta, subscribers, producer, inStatus, (inCompleteUpdate ? sequenceID+1 : sequenceID), inCompleteUpdate );
	}
	
	public boolean isCompleteVersion()
	{
		return completeVersion;
	}

	
	/**
	 * @return
	 */
	public IPersistentMap<Integer, DataType<?>> getDataMap()
	{
		return dataMap;
	}
	
	/**
	 * @return
	 */
	public Set<Integer> getDeltaSet()
	{
		return deltaSet;
	}
	
	public IDataProducer getProducer()
	{
		return producer;
	}
	
	/**
	 * @param inKey
	 * @return
	 */
	public DataType<?> getValue( int inKey )
	{
		return dataMap.valAt( inKey );
	}

	@Override
	public boolean isDeltaValue(int inKey)
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
	
}
