package org.juxtapose.streamline.util.data;

public class DataTypeString extends DataType<String> {

	public DataTypeString(String inValue) 
	{
		super(inValue);
	}
	
	public final byte[] serialize( byte[] inField )
	{
		byte[] strBytes = get().getBytes();
		byte[] bytes = getByteArrayFrame(inField, strBytes.length+5);
		bytes[inField.length] = STRING;
		serializeInt( bytes, inField.length+1, strBytes.length );
		System.arraycopy( strBytes, 0, bytes, inField.length+5, strBytes.length );
		
		return bytes;
		
	}

}
