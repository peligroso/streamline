package org.juxtapose.streamline.producer.executor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Pontus Jörgne
 * 9 okt 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class BlockingQueueExecutor extends Executor implements IExecutor
{	
	public BlockingQueueExecutor( int inHighTPCorePoolSize, int inHighTPBlockingCorePoolSize, int inLowTPCorePoolSize, int inLowTPBlockingCorePoolSize )
	{	
		super(inHighTPCorePoolSize, inHighTPBlockingCorePoolSize, inLowTPCorePoolSize, inLowTPBlockingCorePoolSize);
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
