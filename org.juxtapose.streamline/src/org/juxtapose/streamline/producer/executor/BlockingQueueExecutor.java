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
	
	
	public void execute( final IExecutable inExecutable, int inPrio )
	{
		ThreadPoolExecutor executor = inPrio == IExecutor.HIGH ? highThroughputExecutor : lowThroughputExecutor;
		executor.execute( inExecutable );
	}
	
	/**
	 * @param inRunnable
	 * @param inSequenceKey
	 */
	public void execute( final IExecutable inExecutable, int inPrio, final String inSequenceKey )
	{
		ThreadPoolExecutor executor = inPrio == IExecutor.HIGH ? highThroughputExecutor : lowThroughputExecutor;
		executor.execute( new Runnable(){

			@Override
			public void run()
			{
				synchronized (inSequenceKey.intern())
				{
					inExecutable.run();
				}
			}

		});
	}
	
	/**
	 * @param inRunnable
	 * @param inSequenceLock
	 */
	public void executeBlocking( final IExecutable inExecutable, int inPrio, final ReentrantLock inSequenceLock )
	{
		ThreadPoolExecutor executor = inPrio == IExecutor.HIGH ? highThroughputExecutor : lowThroughputExecutor;
		executor.execute( new Runnable(){

			@Override
			public void run()
			{
				inSequenceLock.lock();
				{
					inExecutable.run();
				}
				inSequenceLock.unlock();
			}
		});
	}
	
	/**
	 * @param inRunnable
	 * @param inSequenceLock
	 */
	public void scheduleExecution( final IExecutable inExecutable, int inPrio, long inTime, TimeUnit inTimeUnit )
	{
		ScheduledExecutorService executor = inPrio == IExecutor.HIGH ? highTPScheduledExecutorService : lowTPScheduledExecutorService;
		executor.schedule( inExecutable, inTime, inTimeUnit );
	}
}
