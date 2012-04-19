package org.juxtapose.streamline.util.data;

public class DataTypeBoolean extends DataType<Boolean>
{

	public DataTypeBoolean(Boolean inValue)
	{
		super( inValue );
	}
	
	public final byte[] serialize( Integer inField )
	{
		byte[] bytes = new byte[5];
		serializeInt( bytes, 0, inField );
		bytes[4] = get() ? BOOLEAN_TRUE : BOOLEAN_FALSE;
		
		return bytes;
		
	}

}
