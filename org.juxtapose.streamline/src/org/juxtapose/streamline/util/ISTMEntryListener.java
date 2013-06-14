package org.juxtapose.streamline.util;

import org.juxtapose.streamline.producer.ISTMEntryKey;


public interface ISTMEntryListener
{
	public void STMEntryUpdated( ISTMEntryKey inKey, ISTMEntry inEntry, boolean inFullUpdate );
}
