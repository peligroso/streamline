package org.juxtapose.streamline.util;

import org.juxtapose.streamline.util.data.DataType;

import com.trifork.clj_ds.IPersistentMap;

public interface ISTMRequestor
{
	public void reply( int inTag, IPersistentMap<String, DataType<?>> inData);
	public void requestError( int inTag, String inError );
}
