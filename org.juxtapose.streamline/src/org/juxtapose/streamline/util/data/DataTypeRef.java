
package org.juxtapose.streamline.util.data;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.util.ISTMEntry;
//
///**
// * @author Pontus J�rgne
// * Dec 30, 2011
// * Copyright (c) Pontus J�rgne. All rights reserved
// */
public class DataTypeRef
{
	final ISTMEntryKey key;

	final ISTMEntry referenceData;
	
	public DataTypeRef(ISTMEntryKey inValue)
	{
		key = inValue;
		referenceData = null;
	}
	
	public DataTypeRef(ISTMEntryKey inValue, ISTMEntry inData )
	{
		key = inValue;
		referenceData = inData;
	}
	
	public ISTMEntry getReferenceData()
	{
		return referenceData;
	}
	
	public ISTMEntryKey get()
	{
		return key;
	}
}
