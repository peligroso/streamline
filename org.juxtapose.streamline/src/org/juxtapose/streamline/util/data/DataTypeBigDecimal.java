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
	
	public final byte[] serialize( byte[] inField )
	{
		//[Field, BIG_DEC, unscaledValue, lengthOfUnscaledValue, scale]
		BigInteger theInt = get().unscaledValue();
		int scale = get().scale();
		
		byte[] intBytes = theInt.toByteArray();
//		byte[] bytes = new byte[ 1 + 4 + intBytes.length + 4 ];
		byte[] bytes = getByteArrayFrame(inField, intBytes.length + 9);
		
		bytes[inField.length] = BIG_DEC;
		serializeInt( bytes, inField.length+1, intBytes.length );
		System.arraycopy( intBytes, 0, bytes, inField.length+5, intBytes.length );
		serializeInt( bytes, inField.length+5+intBytes.length, scale);
		return bytes;
		
	}


}
