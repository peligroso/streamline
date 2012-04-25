package org.juxtapose.streamline.util.data;

import org.juxtapose.streamline.producer.IDataKey;
import org.juxtapose.streamline.stm.DataSerializer;
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
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.util.data.DataType#serialize(java.lang.Integer)
	 */
	public byte[] serialize( Integer inField )
	{
		//[Field, REF, mapByteLength, mapBytes]
		byte[] mapBytes = DataSerializer.serialize( referenceData.getDataMap() );
		byte[] bytes = new byte[mapBytes.length+9];
		serializeInt( bytes, 0, inField );
		bytes[4] = REF;
		serializeInt( bytes, 5, mapBytes.length );
		System.arraycopy( mapBytes, 0, bytes, 9, mapBytes.length );
		
		return bytes;
	}

}
