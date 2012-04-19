package org.juxtapose.streamline.util.data;

public class DataTypeLong extends DataType<Long>
{
	public DataTypeLong(Long inValue)
	{
		super(inValue);
	}
	
	public final byte[] serialize( Integer inField )
	{
		long number = get();
		int sign = Long.signum( number );
		
		byte[] bytes = getNumberProperties( number );
		serializeInt( bytes, 0, inField );
		
		if( sign == -1 )
			number *= -1;
		
		for( int i = bytes.length-1; i > 4; i-- )
		{
			int shift = (bytes.length-1) - (i);
			bytes[i] = (byte)(number >>> 8 * shift);
		}
		
		return bytes;
	}

}
