package org.juxtapose.fxtradingsystem;

import java.math.BigDecimal;

public class BigDecimals 
{
	private static final int INT_CACHE_SIZE = 1000001;
	
	private static final BigDecimal[] ints = new BigDecimal[INT_CACHE_SIZE];
	
	public static final BigDecimal MINUS_ONE = new BigDecimal( -1 );
	public static final BigDecimal ZERO = BigDecimal.ZERO;
	public static final BigDecimal ONE = BigDecimal.ONE;
	
	static
	{
		for( int i = 0; i < INT_CACHE_SIZE; i++ )
		{
			final BigDecimal data = new BigDecimal(i);
			ints[i] = data;
		}
	}
	
	public static BigDecimal getInt( int inInt )
	{
		if( inInt < INT_CACHE_SIZE && inInt > 0 )
			return ints[inInt];
		else
		{
			return new BigDecimal(inInt);
		}
	}
}
