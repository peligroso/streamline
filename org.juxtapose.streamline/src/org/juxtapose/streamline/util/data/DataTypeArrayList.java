package org.juxtapose.streamline.util.data;

import org.juxtapose.streamline.util.PersistentArrayList;

/**
 * @author Pontus Jörgne
 * May 11, 2012
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class DataTypeArrayList extends DataType<PersistentArrayList<DataType<?>>> {

	DataTypeArrayList(PersistentArrayList<DataType<?>> inValue) {
		super(inValue);
	}

}
