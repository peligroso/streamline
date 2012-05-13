package org.juxtapose.streamline.util.data;

import org.juxtapose.streamline.producer.IDataKey;
import org.juxtapose.streamline.stm.DataSerializer;
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
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.util.data.DataType#serialize(java.lang.Integer)
	 */
	public byte[] serialize( byte[] inField )
	{
		//[Field, REF, mapByteLength, mapBytes]
		byte[] mapBytes = DataSerializer.serializeData( referenceData.getDataMap() );
		byte[] bytes = getByteArrayFrame(inField, mapBytes.length+5);
		bytes[inField.length] = REF;
		serializeInt( bytes, inField.length+1, mapBytes.length );
		System.arraycopy( mapBytes, 0, bytes, inField.length+5, mapBytes.length );
		
		return bytes;
	}

}
