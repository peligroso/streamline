package org.juxtapose.streamline.stm;

import java.util.HashSet;

import org.juxtapose.streamline.producer.IDataProducer;
import org.juxtapose.streamline.util.IDataSubscriber;
import org.juxtapose.streamline.util.IPublishedData;
import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;

import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.PersistentHashMap;

/**
 * @author Pontus Jörgne
 * 17 okt 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class PublishedDataFactory implements IPublishedDataFactory
{
	/**
	 * @param inStatus
	 * @param inProducer
	 * @return
	 */
	public IPublishedData createData( Status inStatus, IDataProducer inProducer )
	{
		IPersistentMap<String, DataType<?>> dataMap = PersistentHashMap.create();
		PersistentArrayList<IDataSubscriber> subscribers = new PersistentArrayList<IDataSubscriber>();
		
		return new PublishedData( dataMap, new HashSet<String>(), subscribers, inProducer, inStatus, 0l, true );
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.impl.IPublishedDataFactory#createData(org.juxtapose.streamline.util.Status, org.juxtapose.streamline.producer.IDataProducer, org.juxtapose.streamline.util.IDataSubscriber)
	 */
	public IPublishedData createData( Status inStatus, IDataProducer inProducer, IDataSubscriber inSubscriber )
	{
		IPersistentMap<String, DataType<?>> dataMap = PersistentHashMap.create( );
		PersistentArrayList<IDataSubscriber> subscribers = new PersistentArrayList<IDataSubscriber>( new IDataSubscriber[]{inSubscriber});
		
		return new PublishedData( dataMap, new HashSet<String>(), subscribers, inProducer, inStatus, 0l, true );
	}
}
