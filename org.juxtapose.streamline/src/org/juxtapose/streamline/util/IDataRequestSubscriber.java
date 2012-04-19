package org.juxtapose.streamline.util;

import org.juxtapose.streamline.producer.IDataKey;

public interface IDataRequestSubscriber extends IDataSubscriber
{
	public void deliverKey( IDataKey inDataKey, Long inTag );
	public void queryNotAvailible( Long inTag );
}
