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
	public void execute(IExecutable inExecutable, int inPrio) 
	{
		StickyHashRingBuffer ringBuffer = inPrio == IExecutor.HIGH ? highTP_ringBuffer: lowTP_ringBuffer;
		ringBuffer.execute( inExecutable );
	}

	@Override
	public void execute(IExecutable inExecutable, int inPrio, String inSequenceKey) 
	{
		execute( inExecutable, inPrio );
	}

	@Override
	public void executeBlocking(IExecutable inExecutable, int inPrio, ReentrantLock inSequenceLock) 
	{
		StickyHashRingBuffer ringBuffer = inPrio == IExecutor.HIGH ? blockingHTP_ringBuffer : blockingHTP_ringBuffer;
		ringBuffer.execute( inExecutable );
	}

	@Override
	public void scheduleExecution(IExecutable inExecutable, int inPrio, long inTime, TimeUnit inTimeUnit) 
	{
		
		//TODO implement a scheduledThreadPoolExecutor
	}

}
