package org.juxtapose.streamline.util.data;

import com.trifork.clj_ds.PersistentHashMap;

public class DataTypeHashMap extends DataType<PersistentHashMap<String, DataType<?>>> {

	DataTypeHashMap(PersistentHashMap<String, DataType<?>> inValue) {
		super(inValue);
	}

}
