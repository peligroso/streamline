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
		//[Field, BIG_DEC, unscaledValue, lengthOfUnscaledValue, scale]
		BigInteger theInt = get().unscaledValue();
		int scale = get().scale();
		
		byte[] intBytes = theInt.toByteArray();
		byte[] bytes = new byte[intBytes.length+13];
		serializeInt( bytes, 0, inField );
		bytes[4] = BIG_DEC;
		serializeInt( bytes, 5, intBytes.length );
		System.arraycopy( intBytes, 0, bytes, 9, intBytes.length );
		serializeInt( bytes, intBytes.length+9, scale);
		return bytes;
		
	}


}
