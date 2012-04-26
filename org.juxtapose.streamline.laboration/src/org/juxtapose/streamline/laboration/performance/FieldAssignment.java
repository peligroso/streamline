package org.juxtapose.streamline.laboration.performance;

import java.util.Random;

import org.juxtapose.streamline.util.data.DataTypeLong;

import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.PersistentHashMap;

/**
 * 
 * @author Pontus
 *
 *	1000 000 set and 100 000 get
 *
 *	NOP 		time to do 1000 000 assignments and 100 000 reads: 8910412 avg for each operation: 81 anchor10000
 *	Assign:		time to do 1000 000 assignments and 100 000 reads: 95655191 avg for each operation: 86 anchor-8629611403722282306  		5 ns over NOP
 *	MapPrime:	time to do 1000 000 assignments and 100 000 reads: 192477648 avg for each operation: 174 anchor-7149332335452569461		93 ns over NOP (18.6 times slower than assign)
 *	Map:		time to do 1000 000 assignments and 100 000 reads: 189520771 avg for each operation: 172 anchor-3846565576987744005		91 ns over NOP (18.5 times slower than assign)
 *
 *	100 000 set and 1000 000 get
 *
 *	NOP:		time to do 1000 000 assignments and 100 000 reads: 27101648 avg for each operation: 24 anchor1000000
 *	Assign:		time to do 1000 000 assignments and 100 000 reads: 31004658 avg for each operation: 28 anchor3283451791681994166		4 ns over NOP
 *	MapPrime:	time to do 1000 000 assignments and 100 000 reads: 47558555 avg for each operation: 43 anchor-2451373388643267596		15 ns over NOP (3 times slower than assign)
 *	Map:		time to do 1000 000 assignments and 100 000 reads: 59987451 avg for each operation: 54 anchor-5861777654442649388		26 ns over NOP (6.5 times slower than assign)
 *
 *	Read overhead for Map is ~22 ns 
 *	Write overhead for Map is ~86 ns
 *
 *	Looking at some paths over fairly complex calculation and object relations the field sets are ~ 7 - 18 and gets are 12 - 25
 *	This would mean a over head of 866 - 1032 ns.
 *	To put this in perspective. It is the half the time it takes to convert one double to string. 1/50 the latency of a context switch.
 *	plus it saves the overhead of creating messages between parts of the system.
 *
 *	There is not difference in performance weather we use Long's or Strings (constants) as Map Keys 
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
	
	static final  String ONE = "1";
	static final  String TWO = "2";
	static final  String THREE = "3";
	static final  String FOUR = "4";
	static final  String FIVE = "5";
	static final  String SIX = "6";
	static final  String SEVEN = "7";
	static final  String EIGHT = "8";
	static final  String NINE = "9";
	static final  String TEN = "10";
	
	class MapPerformance implements Performance
	{
		IPersistentMap<String, DataTypeLong> map = PersistentHashMap.emptyMap();
		
		public void assign( int inPlace, Long inVal )
		{
			if( inPlace == 1 )
				map = map.assoc(ONE, new DataTypeLong(inVal));
			else if( inPlace == 2 )
				map = map.assoc(TWO, new DataTypeLong(inVal));
			else if( inPlace == 3 )
				map = map.assoc(THREE, new DataTypeLong(inVal));
			else if( inPlace == 4 )
				map = map.assoc(FOUR, new DataTypeLong(inVal));
			else if( inPlace == 5 )
				map = map.assoc(FIVE, new DataTypeLong(inVal));
			else if( inPlace == 6 )
				map = map.assoc(SIX, new DataTypeLong(inVal));
			else if( inPlace == 7 )
				map = map.assoc(SEVEN, new DataTypeLong(inVal));
			else if( inPlace == 8 )
				map = map.assoc(EIGHT, new DataTypeLong(inVal));
			else if( inPlace == 9 )
				map = map.assoc(NINE, new DataTypeLong(inVal));
			else if( inPlace == 10 )
				map = map.assoc(TEN, new DataTypeLong(inVal));
		}
		
		public Long get( int inPlace )
		{
			DataTypeLong ret;
			
			if( inPlace == 1 )
				ret = map.valAt(ONE);
			else if( inPlace == 2 )
				ret = map.valAt(TWO);
			else if( inPlace == 3 )
				ret = map.valAt(THREE);
			else if( inPlace == 4 )
				ret = map.valAt(FOUR);
			else if( inPlace == 5 )
				ret = map.valAt(FIVE);
			else if( inPlace == 6 )
				ret = map.valAt(SIX);
			else if( inPlace == 7 )
				ret = map.valAt(SEVEN);
			else if( inPlace == 8 )
				ret = map.valAt(EIGHT);
			else if( inPlace == 9 )
				ret = map.valAt(NINE);
			else
				ret = map.valAt(TEN);
			
			if( ret == null )
				return null;
			
			return ret.get();
		}
	}
	
	class MapPrimePerformance implements Performance
	{
		IPersistentMap<String, Long> map = PersistentHashMap.emptyMap();
		
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

		for( int i = 0; i < 100000; i++ )
		{
			test( rand, obj, 1, 10 );
		}

		long start = System.nanoTime();
		Long anchor = 0l;
		
		for( int i = 0; i < 100000; i++ )
		{
			Long res = test( rand, obj, 1, 10 );
			if( res != null )
				anchor += res;
		}
		long end = System.nanoTime();
		long time = end - start;

		long each = time / 1100000;

		System.out.println("time to do 1000 000 assignments and 100 000 reads: "+time+" avg for each operation: "+each+" anchor"+anchor);
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
		for( int i = 0; i < inGetTimes; i++ )
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
