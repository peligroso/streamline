package org.juxtapose.streamline.util.data;

public class DataTypeString extends DataType<String> {

	public DataTypeString(String inValue) 
	{
		super(inValue);
	}
	
	public final byte[] serialize( Integer inField )
	{
		byte[] strBytes = get().getBytes();
		byte[] bytes = new byte[strBytes.length+9];
		serializeInt( bytes, 0, inField );
		bytes[4] = STRING;
		serializeInt( bytes, 5, strBytes.length );
		System.arraycopy( strBytes, 0, bytes, 9, strBytes.length );
		
		return bytes;
		
	}

}
