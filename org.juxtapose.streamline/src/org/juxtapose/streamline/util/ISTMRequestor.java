package org.juxtapose.streamline.util;


import com.trifork.clj_ds.IPersistentMap;

public interface ISTMRequestor
{
	public void reply( int inTag, long inType, String inMessage, IPersistentMap<String, Object> inData);
}
