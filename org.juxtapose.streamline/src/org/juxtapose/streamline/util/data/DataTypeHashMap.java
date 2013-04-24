package org.juxtapose.streamline.util.data;

import com.trifork.clj_ds.IPersistentMap;

public class DataTypeHashMap extends DataType<IPersistentMap<String, DataType<?>>> {

	public DataTypeHashMap(IPersistentMap<String, DataType<?>> inValue) {
		super(inValue);
	}

}
