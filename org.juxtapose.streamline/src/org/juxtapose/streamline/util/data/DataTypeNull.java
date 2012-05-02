package org.juxtapose.streamline.util.data;

public class DataTypeNull extends DataType<Object>{

	public DataTypeNull(Object inValue) {
		super(inValue);
	}

	public final byte[] serialize( byte[] inField )
	{
		byte[] bytes = getByteArrayFrame(inField, 1);
		bytes[inField.length] = NULL;
		
		return bytes;
		
	}
}
