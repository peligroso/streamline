package org.juxtapose.streamline.producer.executor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Executor {

	ThreadPoolExecutor highThroughputExecutor;
	ThreadPoolExecutor highThroughputBlockingExecutor;
	ThreadPoolExecutor lowThroughputExecutor;
	ThreadPoolExecutor lowThroughputBlockingExecutor;
	
	ScheduledExecutorService highTPScheduledExecutorService;
	ScheduledExecutorService lowTPScheduledExecutorService;
	
	public Executor( int inHighTPCorePoolSize, int inHighTPBlockingCorePoolSize, int inLowTPCorePoolSize, int inLowTPBlockingCorePoolSize )
	{
		highThroughputExecutor = new ThreadPoolExecutor( inHighTPCorePoolSize, inHighTPCorePoolSize, 10000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), HighTPThreadFactory.factory );
		highThroughputExecutor.prestartAllCoreThreads();
		highThroughputBlockingExecutor = new ThreadPoolExecutor( inHighTPBlockingCorePoolSize, Integer.MAX_VALUE, 10000, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), HighTPBlockingThreadFactory.factory );
		highThroughputBlockingExecutor.prestartAllCoreThreads();
		highTPScheduledExecutorService = new ScheduledThreadPoolExecutor( 1, HighTPThreadFactory.factory );


		lowThroughputExecutor = new ThreadPoolExecutor( inLowTPCorePoolSize, inLowTPCorePoolSize, 10000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), LowTPThreadFactory.factory );
		lowThroughputExecutor.prestartAllCoreThreads();
		lowThroughputBlockingExecutor = new ThreadPoolExecutor( inLowTPBlockingCorePoolSize, Integer.MAX_VALUE, 10000, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), LowTPBlockingThreadFactory.factory );
		lowThroughputBlockingExecutor.prestartAllCoreThreads();
		lowTPScheduledExecutorService = new ScheduledThreadPoolExecutor( 1, LowTPThreadFactory.factory );	
	}
}
