package org.juxtapose.streamline.util.data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class DataTypeBigDecimal extends DataType<BigDecimal>{

	public DataTypeBigDecimal(BigDecimal inValue) 
	{
		super( inValue );
	}
	
	public DataTypeBigDecimal( double inValue) 
	{
		super( new BigDecimal( inValue ) );
	}
	
	public DataTypeBigDecimal( double inValue, MathContext inContext) 
	{
		super( new BigDecimal( inValue, inContext ) );
	}
	
}
