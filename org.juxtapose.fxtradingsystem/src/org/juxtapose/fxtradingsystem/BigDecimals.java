package org.juxtapose.fxtradingsystem;

import java.math.BigDecimal;

import org.juxtapose.streamline.util.data.DataTypeBigDecimal;

public class BigDecimals 
{
	private static final int INT_CACHE_SIZE = 1000001;
	
	private static final DataTypeBigDecimal[] ints = new DataTypeBigDecimal[INT_CACHE_SIZE];
	
	public static final DataTypeBigDecimal MINUS_ONE = new DataTypeBigDecimal( new BigDecimal( -1 ) );
	public static final DataTypeBigDecimal ZERO = new DataTypeBigDecimal( BigDecimal.ZERO );
	public static final DataTypeBigDecimal ONE = new DataTypeBigDecimal( BigDecimal.ONE );
	
	static
	{
		for( int i = 0; i < INT_CACHE_SIZE; i++ )
		{
			final DataTypeBigDecimal data = new DataTypeBigDecimal( new BigDecimal(i) );
			ints[i] = data;
		}
	}
	
	public static DataTypeBigDecimal getInt( int inInt )
	{
		if( inInt < INT_CACHE_SIZE && inInt > 0 )
			return ints[inInt];
		else
		{
			return new DataTypeBigDecimal( new BigDecimal(inInt) );
		}
	}
}
