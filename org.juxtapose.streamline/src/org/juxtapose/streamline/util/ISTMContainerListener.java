package org.juxtapose.streamline.util;

import org.juxtapose.streamline.producer.ISTMEntryKey;

public interface ISTMContainerListener
{
	public void onContainerRefAdded( ISTMEntryKey inKey, ISTMEntry inEntry );
	public void onContainerRefUpdated( ISTMEntryKey inKey, ISTMEntry inEntry );
	public void onContainerRefRemoved( ISTMEntryKey inKey );
	
}
