package org.juxtapose.streamline.stm;

import java.util.HashSet;

import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.producer.executor.IExecutor;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntrySubscriber;
import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeStatus;

import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.PersistentHashMap;

/**
 * @author Pontus Jörgne
 * 17 okt 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class STMEntryFactory implements IPublishedDataFactory
{
	/**
	 * @param inStatus
	 * @param inProducer
	 * @return
	 */
	public ISTMEntry createData( Status inStatus, ISTMEntryProducer inProducer )
	{
		IPersistentMap<String, DataType<?>> dataMap = PersistentHashMap.create();
		PersistentArrayList<ISTMEntrySubscriber> lowPrioSubscribers = new PersistentArrayList<ISTMEntrySubscriber>();
		PersistentArrayList<ISTMEntrySubscriber> highPrioSubscribers = new PersistentArrayList<ISTMEntrySubscriber>();
		
		return new STMEntry( dataMap, new HashSet<String>(), lowPrioSubscribers, highPrioSubscribers, inProducer, 0l, true );
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.impl.IPublishedDataFactory#createData(org.juxtapose.streamline.util.Status, org.juxtapose.streamline.producer.IDataProducer, org.juxtapose.streamline.util.IDataSubscriber)
	 */
	public ISTMEntry createData( Status inStatus, ISTMEntryProducer inProducer, ISTMEntrySubscriber inSubscriber )
	{
		IPersistentMap<String, DataType<?>> dataMap = PersistentHashMap.create( );
		dataMap = dataMap.assoc( DataConstants.FIELD_STATUS, new DataTypeStatus( inStatus ) );
		
		PersistentArrayList<ISTMEntrySubscriber> emptySubscribers = new PersistentArrayList<ISTMEntrySubscriber>();
		
		if( inSubscriber.getPriority() == IExecutor.HIGH )
		{
			PersistentArrayList<ISTMEntrySubscriber> highPrioSubscribers = new PersistentArrayList<ISTMEntrySubscriber>( new ISTMEntrySubscriber[]{inSubscriber});			
			return new STMEntry( dataMap, new HashSet<String>(), emptySubscribers, highPrioSubscribers, inProducer, 0l, true );
		}
		else
		{
			PersistentArrayList<ISTMEntrySubscriber> lowPrioSubscribers = new PersistentArrayList<ISTMEntrySubscriber>( new ISTMEntrySubscriber[]{inSubscriber});			
			return new STMEntry( dataMap, new HashSet<String>(), lowPrioSubscribers, emptySubscribers, inProducer, 0l, true );
		}
	}
}
