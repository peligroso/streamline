package org.juxtapose.streamline.laboration.locktest;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class LockTest
{
	/**
	 * One thread counts to 5 million = 70 ms. This is 14 ns per iteration or 42 clock cycles.
	 * One thread counts to 5 million with synchronized increment = 1140 ms. This is 228 ns per iteration or 684 clock cycles.
	 * Two thread counts to 5 million with synchronized increment = 2527 ms. This is 505 ns per iteration or (1515 clock cycles).
	 * One thread counts to 5 million with CAS = 500 ms. This is 100 ns per iteration or (300 clock cycles).
	 * Two thread counts to 5 million with CAS = 979 ms. This is 195 ns per iteration or (587 clock cycles).
	 * One thread counts to 5 million with volatile = 357 ms. This is 71 ns per iteration or (214 clock cycles).
	 * One thread counts to 5 million with contextswitch = 14221 ms. This is 2844 ns per iteration or (8532 clock cycles).
	 * Two thread counts to 5 million with contextswitch = 10221 ms. This is 2044 ns per iteration or (6132 clock cycles).
	 * **/
	static long startTime;
	static long counter = 0l;
	static Object mutex = new Object();
	
	static ExecutorService execService = new ThreadPoolExecutor( 1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(5048576) );
	public static void main( String[] args )
	{
//		Thread t1 = getUpdateThread();
//		Thread t2 = getUpdateThread();
		startTime = System.nanoTime();
//		t1.start();
//		t2.start();
		
		while( counter < 5000000 )
		{
			synchronized( mutex )
			{
				counter++;
			}
		}
		
		finish();
	}
	
	public static Thread getUpdateThread()
	{
		Runnable run = new Runnable(){

			@Override
			public void run()
			{
//				while( counter.incrementAndGet() < 5000000 )
//				{
//					
//				}
				while( true )
				{
					post( new Runnable()
					{
						@Override
						public void run()
						{
//							System.out.println("executing "+counter);
							counter++;
							if( counter == 5000000 )
								finish();
							
						}
					});
//					synchronized (mutex)
//					{
//					}
				}
			}
		};
		
		Thread t = new Thread( run );
		
		return t;
	}
	
	public static void finish()
	{
		long endTime = System.nanoTime();
		
		System.out.println("It took: "+((endTime-startTime)/100000)+" ms");
		System.exit( 1 );
	}
	
	public static void post(Runnable inRunnable)
	{
		execService.execute( inRunnable );
	}
}
