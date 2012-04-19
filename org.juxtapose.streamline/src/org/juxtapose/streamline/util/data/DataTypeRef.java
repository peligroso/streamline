package org.juxtapose.streamline.util.data;

import org.juxtapose.streamline.producer.IDataKey;
import org.juxtapose.streamline.util.IPublishedData;

/**
 * @author Pontus J�rgne
 * Dec 30, 2011
 * Copyright (c) Pontus J�rgne. All rights reserved
 */
public class DataTypeRef extends DataType<IDataKey>
{
	final IPublishedData referenceData;
	
	public DataTypeRef(IDataKey inValue)
	{
		super(inValue);
		referenceData = null;
	}
	
	public DataTypeRef(IDataKey inValue, IPublishedData inData )
	{
		super(inValue);
		referenceData = inData;
	}
	
	public IPublishedData getReferenceData()
	{
		return referenceData;
	}
	
	

}
