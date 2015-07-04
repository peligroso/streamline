package org.juxtapose.streamline.stm;

import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.util.ISTMEntrySubscriber;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.Status;

/**
 * @author Pontus J�rgne
 * 17 okt 2011
 * Copyright (c) Pontus J�rgne. All rights reserved
 */
public interface ISTMEntryFactory
{
	public ISTMEntry createData( ISTMEntryProducer inProducer );
	public ISTMEntry createData( Status inStatus, ISTMEntryProducer inProducer, ISTMEntrySubscriber inSubscriber );
}
