package org.juxtapose.streamline.util;

import org.juxtapose.streamline.producer.IDataKey;



public interface IDataSubscriber
{
	public void updateData( IDataKey inKey, IPublishedData inData, boolean inFirstUpdate );
}
