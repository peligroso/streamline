package org.juxtapose.streamline.util.data;

import java.math.BigDecimal;
import java.math.BigInteger;

public class DataTypeBigDecimal extends DataType<BigDecimal>{

	public DataTypeBigDecimal(BigDecimal inValue) 
	{
		super( inValue );
	}
	
	public DataTypeBigDecimal( double inValue) 
	{
		super( new BigDecimal( inValue ) );
	}
	
	public final byte[] serialize( Integer inField )
	{
		BigInteger theInt = get().unscaledValue();
		byte[] bdBytes = theInt.toByteArray();
		byte[] bytes = new byte[bdBytes.length+9];
		serializeInt( bytes, 0, inField );
		bytes[4] = BIG_DEC;
		serializeInt( bytes, 5, bdBytes.length );
		System.arraycopy( bdBytes, 0, bytes, 9, bdBytes.length );
		
		return bytes;
		
	}


}
