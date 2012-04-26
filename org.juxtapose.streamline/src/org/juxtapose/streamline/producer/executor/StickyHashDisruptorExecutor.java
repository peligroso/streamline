package org.juxtapose.streamline.producer.executor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class StickyHashDisruptorExecutor extends Executor implements IExecutor {

	static int RING_SIZE = 32768;
	
	StickyHashRingBuffer highTP_ringBuffer;
	StickyHashRingBuffer lowTP_ringBuffer;
	StickyHashRingBuffer blockingHTP_ringBuffer;
	StickyHashRingBuffer blockingLTP_ringBuffer;
	
	public StickyHashDisruptorExecutor( int inHighTPCorePoolSize, int inHighTPBlockingCorePoolSize, int inLowTPCorePoolSize, int inLowTPBlockingCorePoolSize )
	{
		super(inHighTPCorePoolSize, inHighTPBlockingCorePoolSize, inLowTPCorePoolSize, inLowTPBlockingCorePoolSize);
		
		highTP_ringBuffer = new StickyHashRingBuffer( inHighTPCorePoolSize, highThroughputExecutor );
		lowTP_ringBuffer = new StickyHashRingBuffer( inHighTPBlockingCorePoolSize, lowThroughputExecutor );
		blockingHTP_ringBuffer = new StickyHashRingBuffer( inLowTPCorePoolSize, highThroughputBlockingExecutor );
		blockingLTP_ringBuffer = new StickyHashRingBuffer( inLowTPBlockingCorePoolSize, lowThroughputBlockingExecutor );
	}
	
	
	@Override
	public void execute(Runnable inRunnable, int inPrio) 
	{
		StickyRunnable sr = (StickyRunnable)inRunnable;
		
		StickyHashRingBuffer ringBuffer = inPrio == IExecutor.HIGH ? highTP_ringBuffer: lowTP_ringBuffer;
		ringBuffer.execute( sr );
	}

	@Override
	public void execute(Runnable inRunnable, int inPrio, String inSequenceKey) 
	{
		execute( inRunnable, inPrio );
	}

	@Override
	public void executeBlocking(Runnable inRunnable, int inPrio, ReentrantLock inSequenceLock) 
	{
		StickyRunnable sr = (StickyRunnable)inRunnable;
		StickyHashRingBuffer ringBuffer = inPrio == IExecutor.HIGH ? blockingHTP_ringBuffer : blockingHTP_ringBuffer;
		ringBuffer.execute( sr );
	}

	@Override
	public void scheduleExecution(Runnable inRunnable, int inPrio, long inTime, TimeUnit inTimeUnit) 
	{
		
		//TODO implement a scheduledThreadPoolExecutor
	}

}
