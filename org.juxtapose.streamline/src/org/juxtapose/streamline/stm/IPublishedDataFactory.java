package org.juxtapose.streamline.stm;

import org.juxtapose.streamline.producer.IDataProducer;
import org.juxtapose.streamline.util.IDataSubscriber;
import org.juxtapose.streamline.util.IPublishedData;
import org.juxtapose.streamline.util.Status;

/**
 * @author Pontus J�rgne
 * 17 okt 2011
 * Copyright (c) Pontus J�rgne. All rights reserved
 */
public interface IPublishedDataFactory
{
	public IPublishedData createData( Status inStatus, IDataProducer inProducer );
	public IPublishedData createData( Status inStatus, IDataProducer inProducer, IDataSubscriber inSubscriber );
}
