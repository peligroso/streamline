package org.juxtapose.streamline.laboration.performance;

import java.util.Random;

import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.PersistentHashMap;

/**
 * 
 * @author Pontus
 *
 *	100 000 set and 10 000 get
 *
 *	NOP 	assignment time to do 100000 assignments and 10000 reads: 8910412 avg for each operation: 81 anchor10000
 *	Assign:	time to do 100000 assignments and 10000 reads: 14029023 avg for each operation: 113 anchor-1592240142784605370  	32 ns over NOP
 *	Map:	time to do 100000 assignments and 10000 reads: 39634482 avg for each operation: 270 anchor-6854261056496305582		189 ns over NOP (6 times slower than assign)
 *
 *	10 000 set and 100 000 get
 *
 *	NOP:	time to do 10 000 assignments and 100 000 reads: 3179296 avg for each operation: 28 anchor10000
 *	Assign:	time to do 10 000 assignments and 100 000 reads: 3965886 avg for each operation: 36 anchor-3897281743898921993		8 ns over NOP
 *	Map:	time to do 10 000 assignments and 100 000 reads: 6613090 avg for each operation: 60 anchor-431033611692094618		32 ns over NOP (4 times slower)
 *
 *	Read overhead for Map is ~24 ns 
 *	Write overhead for Map is ~157 ns
 *
 *	Looking at some paths over fairly complex calculation and object relations the field sets are ~ 7 - 18 and gets are 12 - 25
 *	This would mean a over head of 1387 - 3423 ns.
 *	To put this in perspective. It is the same overhead it takes to convert one double to string. 1/25 the overhead of a context switch.
 *	plus it saves the overhead of creating messages between parts of the system.
 * 
 */

public class FieldAssignment {

	interface Performance
	{
		public void assign( int inPlace, Long inVal );
		public Long get( int inPlace );
	}
	
	class AssignPerformance implements Performance
	{
		Long one;
		Long two;
		Long three;
		Long four;
		Long five;
		Long six;
		Long seven;
		Long eight;
		Long nine;
		Long ten;
		
		public void assign( int inPlace, Long inVal )
		{
			if( inPlace == 1 )
				one = inVal;
			else if( inPlace == 2 )
				two = inVal;
			else if( inPlace == 3 )
				three = inVal;
			else if( inPlace == 4 )
				four = inVal;
			else if( inPlace == 5 )
				five = inVal;
			else if( inPlace == 6 )
				six = inVal;
			else if( inPlace == 7 )
				seven = inVal;
			else if( inPlace == 8 )
				eight = inVal;
			else if( inPlace == 9 )
				nine = inVal;
			else if( inPlace == 10 )
				ten = inVal;
		}
		
		public Long get( int inPlace )
		{
			if( inPlace == 1 )
				return one;
			else if( inPlace == 2 )
				return two;
			else if( inPlace == 3 )
				return three;
			else if( inPlace == 4 )
				return four;
			else if( inPlace == 5 )
				return five;
			else if( inPlace == 6 )
				return six;
			else if( inPlace == 7 )
				return seven;
			else if( inPlace == 8 )
				return eight;
			else if( inPlace == 9 )
				return nine;
			else
				return ten;
		}
	}
	
	static final  int ONE = 1;
	static final  int TWO = 2;
	static final  int THREE = 3;
	static final  int FOUR = 4;
	static final  int FIVE = 5;
	static final  int SIX = 6;
	static final  int SEVEN = 7;
	static final  int EIGHT = 8;
	static final  int NINE = 9;
	static final  int TEN = 9;
	
	class MapPerformance implements Performance
	{
		IPersistentMap<Integer, Long> map = PersistentHashMap.emptyMap();
		
		public void assign( int inPlace, Long inVal )
		{
			if( inPlace == 1 )
				map = map.assoc(ONE, inVal);
			else if( inPlace == 2 )
				map = map.assoc(TWO, inVal);
			else if( inPlace == 3 )
				map = map.assoc(THREE, inVal);
			else if( inPlace == 4 )
				map = map.assoc(FOUR, inVal);
			else if( inPlace == 5 )
				map = map.assoc(FIVE, inVal);
			else if( inPlace == 6 )
				map = map.assoc(SIX, inVal);
			else if( inPlace == 7 )
				map = map.assoc(SEVEN, inVal);
			else if( inPlace == 8 )
				map = map.assoc(EIGHT, inVal);
			else if( inPlace == 9 )
				map = map.assoc(NINE, inVal);
			else if( inPlace == 10 )
				map = map.assoc(TEN, inVal);
		}
		
		public Long get( int inPlace )
		{
			if( inPlace == 1 )
				return map.valAt(ONE);
			else if( inPlace == 2 )
				return map.valAt(TWO);
			else if( inPlace == 3 )
				return map.valAt(THREE);
			else if( inPlace == 4 )
				return map.valAt(FOUR);
			else if( inPlace == 5 )
				return map.valAt(FIVE);
			else if( inPlace == 6 )
				return map.valAt(SIX);
			else if( inPlace == 7 )
				return map.valAt(SEVEN);
			else if( inPlace == 8 )
				return map.valAt(EIGHT);
			else if( inPlace == 9 )
				return map.valAt(NINE);
			else
				return map.valAt(TEN);
		}
	}
	
	
	class NopPerformance implements Performance
	{
		
		public void assign( int inPlace, Long inVal )
		{
	
		}
		
		public Long get( int inPlace )
		{
			return 1l;
		}
	}
	
	public void start( )
	{
		Performance obj = new MapPerformance();
		Random rand = new Random();

		for( int i = 0; i < 10000; i++ )
		{
			test( rand, obj, 10, 1 );
		}

		long start = System.nanoTime();
		Long anchor = 0l;
		
		for( int i = 0; i < 10000; i++ )
		{
			Long res = test( rand, obj, 1, 10 );
			if( res != null )
				anchor += res;
		}
		long end = System.nanoTime();
		long time = end - start;

		long each = time / 110000;

		System.out.println("time to do 10 000 assignments and 100 000 reads: "+time+" avg for each operation: "+each+" anchor"+anchor);
	}

	public void assign( Random inRand, Performance inObj, int inAssignTimes )
	{
		for( int i = 0; i < inAssignTimes; i++ )
		{
			Long val = inRand.nextLong();
			int place = inRand.nextInt( 10 );

			inObj.assign( place, val );
		}
	}

	public Long test( Random inRand, Performance inObj, int inAssignTimes, int inGetTimes )
	{
		assign( inRand, inObj, inAssignTimes );
		int place = inRand.nextInt( 10 );

		long ret = 0;
		for( int i = 0; i < inAssignTimes; i++ )
		{
			Long r = inObj.get( place );
			if( r != null )
				ret += r;
		}
		return ret;
	}
	

	public static void main( String... inArgs )
	{
		new FieldAssignment().start();
	}
}
