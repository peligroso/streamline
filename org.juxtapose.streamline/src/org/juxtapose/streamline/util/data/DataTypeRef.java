package org.juxtapose.streamline.util.data;

import org.juxtapose.streamline.producer.IDataKey;
import org.juxtapose.streamline.util.IPublishedData;

/**
 * @author Pontus Jörgne
 * Dec 30, 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
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
