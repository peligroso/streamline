package org.juxtapose.streamline.stm;

import java.util.HashSet;

import org.juxtapose.streamline.producer.IDataProducer;
import org.juxtapose.streamline.util.IDataSubscriber;
import org.juxtapose.streamline.util.IPublishedData;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;

import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.IPersistentVector;
import com.trifork.clj_ds.PersistentHashMap;
import com.trifork.clj_ds.PersistentVector;

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
		IPersistentMap<Integer, DataType<?>> dataMap = PersistentHashMap.create();
		IDataSubscriber[] subscribers = new IDataSubscriber[]{};
		
		return new PublishedData( dataMap, new HashSet<Integer>(), subscribers, inProducer, inStatus, 0l, true );
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.impl.IPublishedDataFactory#createData(org.juxtapose.streamline.util.Status, org.juxtapose.streamline.producer.IDataProducer, org.juxtapose.streamline.util.IDataSubscriber)
	 */
	public IPublishedData createData( Status inStatus, IDataProducer inProducer, IDataSubscriber inSubscriber )
	{
		IPersistentMap<Integer, DataType<?>> dataMap = PersistentHashMap.create( );
		IDataSubscriber[] subscribers = new IDataSubscriber[]{inSubscriber};
		
		return new PublishedData( dataMap, new HashSet<Integer>(), subscribers, inProducer, inStatus, 0l, true );
	}
}
