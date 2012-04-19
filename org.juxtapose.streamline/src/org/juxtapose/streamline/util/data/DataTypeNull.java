package org.juxtapose.streamline.util.data;

public class DataTypeNull extends DataType<Object>{

	public DataTypeNull(Object inValue) {
		super(inValue);
	}

	public final byte[] serialize( Integer inField )
	{
		byte[] bytes = new byte[5];
		serializeInt( bytes, 0, inField );
		bytes[5] = NULL;
		
		return bytes;
		
	}
}
