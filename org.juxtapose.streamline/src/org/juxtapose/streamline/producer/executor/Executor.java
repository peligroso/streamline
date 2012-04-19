package org.juxtapose.streamline.producer.executor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Pontus Jörgne
 * 9 okt 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class Executor implements IExecutor
{	
	ThreadPoolExecutor highThroughputExecutor;
	ThreadPoolExecutor highThroughputBlockingExecutor;
	ThreadPoolExecutor lowThroughputExecutor;
	ThreadPoolExecutor lowThroughputBlockingExecutor;
	
	ScheduledExecutorService highTPScheduledExecutorService;
	ScheduledExecutorService lowTPScheduledExecutorService;
	
	public static String HIGH_TP_EXECUTOR_THREAD = "High TP Executor Thread: ";
	public static int HIGH_TP_COUNT = 0;
	
	public static String HIGH_TP_BLOCKING_EXECUTOR_THREAD = "High TP Blocking Executor Thread: ";
	public static int HIGH_TP_BLOCKING_COUNT = 0;
	
	public static String SCHEDULED_HIGH_TP_EXECUTOR_THREAD = "Scheduled High TP Executor Thread";
	
	public static String LOW_TP_EXECUTOR_THREAD = "Low TP Executor Thread: ";
	public static int LOW_TP_COUNT = 0;
	
	public static String LOW_TP_BLOCKING_EXECUTOR_THREAD = "Low TP Blocking Executor Thread: ";
	public static int LOW_TP_BLOCKING_COUNT = 0;
	
	public static String SCHEDULED_LOW_TP_EXECUTOR_THREAD = "Scheduled High TP Executor Thread";
	
	public Executor( int inHighTPCorePoolSize, int inHighTPBlockingCorePoolSize, int inLowTPCorePoolSize, int inLowTPBlockingCorePoolSize )
	{
		ThreadFactory highTPFactory = new ThreadFactory()
		{
			@Override
			public Thread newThread(Runnable r)
			{
				Thread t = new Thread( r, HIGH_TP_EXECUTOR_THREAD+(HIGH_TP_COUNT++) );
				t.setPriority( Thread.MAX_PRIORITY );
				t.setDaemon( true );
				
				return t;
			}
		};
		
		ThreadFactory highTPBlockingFactory = new ThreadFactory()
		{
			@Override
			public Thread newThread(Runnable r)
			{
				Thread t = new Thread( r, HIGH_TP_BLOCKING_EXECUTOR_THREAD+(HIGH_TP_BLOCKING_COUNT++) );
				t.setPriority( Thread.MAX_PRIORITY );
				t.setDaemon( true );
				
				return t;
			}
		};
		
		ThreadFactory scheduledHighTPFactory = new ThreadFactory()
		{
			@Override
			public Thread newThread(Runnable r)
			{
				Thread t = new Thread( r, SCHEDULED_HIGH_TP_EXECUTOR_THREAD );
				t.setPriority( Thread.MAX_PRIORITY );
				t.setDaemon( true );
				
				return t;
			}
		};
		
		highThroughputExecutor = new ThreadPoolExecutor( inHighTPCorePoolSize, inHighTPCorePoolSize, 10000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), highTPFactory );
		highThroughputExecutor.prestartAllCoreThreads();
		highThroughputBlockingExecutor = new ThreadPoolExecutor( inHighTPBlockingCorePoolSize, Integer.MAX_VALUE, 10000, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), highTPBlockingFactory );
		highThroughputBlockingExecutor.prestartAllCoreThreads();
		highTPScheduledExecutorService = new ScheduledThreadPoolExecutor( 1, scheduledHighTPFactory );
		
		ThreadFactory lowTPFactory = new ThreadFactory()
		{
			@Override
			public Thread newThread(Runnable r)
			{
				Thread t = new Thread( r, LOW_TP_EXECUTOR_THREAD+(LOW_TP_COUNT++) );
				t.setPriority( Thread.MIN_PRIORITY );
				t.setDaemon( true );
				
				return t;
			}
		};
		
		ThreadFactory lowTPBlockingFactory = new ThreadFactory()
		{
			@Override
			public Thread newThread(Runnable r)
			{
				Thread t = new Thread( r, LOW_TP_BLOCKING_EXECUTOR_THREAD+(LOW_TP_BLOCKING_COUNT++) );
				t.setPriority( Thread.MIN_PRIORITY );
				t.setDaemon( true );
				
				return t;
			}
		};
		
		ThreadFactory scheduledLowTPFactory = new ThreadFactory()
		{
			@Override
			public Thread newThread(Runnable r)
			{
				Thread t = new Thread( r, SCHEDULED_LOW_TP_EXECUTOR_THREAD );
				t.setPriority( Thread.MIN_PRIORITY );
				t.setDaemon( true );
				
				return t;
			}
		};
		
		lowThroughputExecutor = new ThreadPoolExecutor( inLowTPCorePoolSize, inLowTPCorePoolSize, 10000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), lowTPFactory );
		lowThroughputExecutor.prestartAllCoreThreads();
		lowThroughputBlockingExecutor = new ThreadPoolExecutor( inLowTPBlockingCorePoolSize, Integer.MAX_VALUE, 10000, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), lowTPBlockingFactory );
		lowThroughputBlockingExecutor.prestartAllCoreThreads();
		lowTPScheduledExecutorService = new ScheduledThreadPoolExecutor( 1, scheduledLowTPFactory );	
		
	}
	
	
	public void execute( final Runnable inRunnable, int inPrio )
	{
		ThreadPoolExecutor executor = inPrio == IExecutor.HIGH ? highThroughputExecutor : lowThroughputExecutor;
		executor.execute( inRunnable );
	}
	
	/**
	 * @param inRunnable
	 * @param inSequenceKey
	 */
	public void execute( final Runnable inRunnable, int inPrio, final String inSequenceKey )
	{
		ThreadPoolExecutor executor = inPrio == IExecutor.HIGH ? highThroughputExecutor : lowThroughputExecutor;
		executor.execute( new Runnable(){

			@Override
			public void run()
			{
				synchronized (inSequenceKey.intern())
				{
					inRunnable.run();
				}
			}

		});
	}
	
	/**
	 * @param inRunnable
	 * @param inSequenceLock
	 */
	public void executeBlocking( final Runnable inRunnable, int inPrio, final ReentrantLock inSequenceLock )
	{
		ThreadPoolExecutor executor = inPrio == IExecutor.HIGH ? highThroughputExecutor : lowThroughputExecutor;
		executor.execute( new Runnable(){

			@Override
			public void run()
			{
				inSequenceLock.lock();
				{
					inRunnable.run();
				}
				inSequenceLock.unlock();
			}
		});
	}
	
	/**
	 * @param inRunnable
	 * @param inSequenceLock
	 */
	public void scheduleExecution( final Runnable inRunnable, int inPrio, long inTime, TimeUnit inTimeUnit )
	{
		ScheduledExecutorService executor = inPrio == IExecutor.HIGH ? highTPScheduledExecutorService : lowTPScheduledExecutorService;
		executor.schedule( inRunnable, inTime, inTimeUnit );
	}
}
