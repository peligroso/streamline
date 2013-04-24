package org.juxtapose.bundle.objectpooltest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import org.juxtapose.streamline.util.data.DataTypeBigDecimal;

public class ObjectPoolTest
{
	static int SIZE = 100000;
	
	static int ITER = 1000000;
	
	static DataTypeBigDecimal bDec[] = new DataTypeBigDecimal[SIZE];
	
	static Random rand = new Random();
	
	public static void fillPool()
	{
		for( int i = 0; i < SIZE; i++ )
		{
			DataTypeBigDecimal bd = new DataTypeBigDecimal( new BigDecimal( i ) );
			bDec[i] = bd;
		}
	}
	
	public static BigDecimal doPoolCount()
	{
		BigDecimal anchor = BigDecimal.ZERO;
		
		for( int i= 0; i < ITER; i++ )
		{
			int val1 = rand.nextInt( SIZE-1 );
			val1++;
			int val2 = rand.nextInt( SIZE-1 );
			val2++;
			BigDecimal bd = bDec[val1].get();
			BigDecimal bd2 = bDec[val2].get();
			
			BigDecimal div = bd.divide( bd2, 4, RoundingMode.HALF_UP );
			
			anchor = anchor.add( div );
		}
		
		return anchor;
	}
	
	public static BigDecimal doHeapCount()
	{
		BigDecimal anchor = BigDecimal.ZERO;
		
		for( int i= 0; i < ITER; i++ )
		{
			int val1 = rand.nextInt( SIZE-1 );
			val1++;
			int val2 = rand.nextInt( SIZE-1 );
			val2++;
			DataTypeBigDecimal bd = new DataTypeBigDecimal( new BigDecimal(val1) );
			DataTypeBigDecimal bd2 = new DataTypeBigDecimal( new BigDecimal(val2) );
			
			BigDecimal div = bd.get().divide( bd2.get(), 4, RoundingMode.HALF_UP );
			
			anchor = anchor.add( div );
		}
		
		return anchor;
	}
	
	public static void main( String inArgs[] )
	{
		fillPool();
		
		long start = System.currentTimeMillis();
		BigDecimal anc = doHeapCount();
		long time = System.currentTimeMillis() - start;
		System.out.println("warmup heap: "+time+" anchor: "+anc);
		
		start = System.currentTimeMillis();
		anc = doHeapCount();
		time = System.currentTimeMillis() - start;
		System.out.println("go heap: "+time+" anchor: "+anc);
		
		start = System.currentTimeMillis();
		anc = doHeapCount();
		time = System.currentTimeMillis() - start;
		System.out.println("go heap: "+time+" anchor: "+anc);
		
		start = System.currentTimeMillis();
		anc = doPoolCount();
		time = System.currentTimeMillis() - start;
		System.out.println("warmup pool: "+time+" anchor: "+anc);
		
		start = System.currentTimeMillis();
		anc = doPoolCount();
		time = System.currentTimeMillis() - start;
		System.out.println("go pool: "+time+" anchor: "+anc);
		
		start = System.currentTimeMillis();
		anc = doPoolCount();
		time = System.currentTimeMillis() - start;
		System.out.println("go pool: "+time+" anchor: "+anc);
		
	}
}
