package org.juxtapose.streamline.util;

import org.juxtapose.streamline.producer.ISTMEntryKey;



public interface ISTMEntrySubscriber
{
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData, boolean inFirstUpdate );
}
