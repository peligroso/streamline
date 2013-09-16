
package org.juxtapose.streamline.util.data;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.util.ISTMEntry;
//
///**
// * @author Pontus Jörgne
// * Dec 30, 2011
// * Copyright (c) Pontus Jörgne. All rights reserved
// */
public class DataTypeRef extends DataType<ISTMEntryKey>
{
	final ISTMEntry referenceData;
	
	public DataTypeRef(ISTMEntryKey inValue)
	{
		super( inValue );
		referenceData = null;
	}
	
	public DataTypeRef(ISTMEntryKey inValue, ISTMEntry inData )
	{
		super( inValue );
		referenceData = inData;
	}
	
	public ISTMEntry getReferenceData()
	{
		return referenceData;
	}
	
}
