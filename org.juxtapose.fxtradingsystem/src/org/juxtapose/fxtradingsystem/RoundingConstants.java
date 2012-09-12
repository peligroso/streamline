package org.juxtapose.fxtradingsystem;

import java.math.MathContext;
import java.math.RoundingMode;

public class RoundingConstants {

	private static final MathContext ROUND_UP[] = new MathContext[]{ 	new MathContext(0, RoundingMode.CEILING ), 
																new MathContext(1, RoundingMode.CEILING ),
																new MathContext(2, RoundingMode.CEILING ),
																new MathContext(3, RoundingMode.CEILING ),
																new MathContext(4, RoundingMode.CEILING ),
																new MathContext(5, RoundingMode.CEILING ),
																new MathContext(6, RoundingMode.CEILING ),
																new MathContext(7, RoundingMode.CEILING ),
																new MathContext(8, RoundingMode.CEILING ),
																new MathContext(9, RoundingMode.CEILING ),
																new MathContext(10, RoundingMode.CEILING ),
																new MathContext(11, RoundingMode.CEILING ),
																new MathContext(12, RoundingMode.CEILING ),
																new MathContext(13, RoundingMode.CEILING ),
																new MathContext(14, RoundingMode.CEILING )};
	
	private static final MathContext ROUND_DOWN[] = new MathContext[]{ 	new MathContext(0, RoundingMode.FLOOR ), 
																new MathContext(1, RoundingMode.FLOOR ),
																new MathContext(2, RoundingMode.FLOOR ),
																new MathContext(3, RoundingMode.FLOOR ),
																new MathContext(4, RoundingMode.FLOOR ),
																new MathContext(5, RoundingMode.FLOOR ),
																new MathContext(6, RoundingMode.FLOOR ),
																new MathContext(7, RoundingMode.FLOOR ),
																new MathContext(8, RoundingMode.FLOOR ),
																new MathContext(9, RoundingMode.FLOOR ),
																new MathContext(10, RoundingMode.FLOOR ),
																new MathContext(11, RoundingMode.FLOOR ),
																new MathContext(12, RoundingMode.FLOOR ),
																new MathContext(13, RoundingMode.FLOOR ),
																new MathContext(14, RoundingMode.FLOOR )};
	
	public static MathContext roundUp( int inDecimals )
	{
		return inDecimals <= 14 ? ROUND_UP[inDecimals] : new MathContext( inDecimals, RoundingMode.CEILING );
	}
	
	public static MathContext roundDown( int inDecimals )
	{
		return inDecimals <= 14 ? ROUND_DOWN[inDecimals] : new MathContext( inDecimals, RoundingMode.CEILING );
	}
}

