package org.juxtapose.streamline.util.data;

public class DataTypeBoolean extends DataType<Boolean>
{

	public DataTypeBoolean(Boolean inValue)
	{
		super( inValue );
	}
	
	public final byte[] serialize( byte[] inField )
	{
		byte[] bytes = getByteArrayFrame(inField, 1);
		bytes[inField.length] = get() ? BOOLEAN_TRUE : BOOLEAN_FALSE;
		
		return bytes;
		
	}

}
