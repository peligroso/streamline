package org.juxtapose.streamline.util;

import org.juxtapose.streamline.producer.ISTMEntryKey;

public interface ISTMEntryRequestSubscriber extends ISTMEntrySubscriber
{
	public void deliverKey( ISTMEntryKey inDataKey, Object inTag );
	public void queryNotAvailible( Object inTag );
}
