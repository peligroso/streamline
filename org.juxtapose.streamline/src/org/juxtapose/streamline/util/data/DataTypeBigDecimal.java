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
	
}
