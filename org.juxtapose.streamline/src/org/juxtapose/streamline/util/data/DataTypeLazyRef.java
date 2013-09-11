package org.juxtapose.streamline.util.data;

import org.juxtapose.streamline.producer.ISTMEntryKey;

public class DataTypeLazyRef
{
	final ISTMEntryKey key;
	
	public DataTypeLazyRef(ISTMEntryKey inValue)
	{
		key = inValue;
	}
	
	public ISTMEntryKey get()
	{
		return key;
	}
}
