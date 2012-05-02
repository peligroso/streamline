package org.juxtapose.streamline.util.data;

public class DataTypeLong extends DataType<Long>
{
	public DataTypeLong(Long inValue)
	{
		super(inValue);
	}
	
	public final byte[] serialize( byte[] inField )
	{
		long number = get();
		int sign = Long.signum( number );
		byte[] numberProps = getNumberProperties( number );
		
		byte[] bytes = getByteArrayFrame(inField, numberProps.length);
		
		bytes[inField.length] = numberProps[0];
				
		if( sign == -1 )
			number *= -1;
		
		for( int i = bytes.length-1; i > inField.length; i-- )
		{
			int shift = (bytes.length-1) - (i);
			bytes[i] = (byte)(number >>> 8 * shift);
		}
		
		return bytes;
	}

}
