package org.juxtapose.streamline.producer.executor;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.MultiThreadedClaimStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;

/**
 * @author Pontus
 *
 */
public class StickyHashRingBuffer 
{
	static int RING_SIZE = 32768;
	
	public final RingBuffer<RunnableEvent> ringBuffer;
	
	/**
	 * @param inEventHandlers
	 */
	public StickyHashRingBuffer( int inEventHandlers, Executor inExecutor, boolean inTurboBoost )
	{
		WaitStrategy waitStrategy = inTurboBoost ? new BusySpinWaitStrategy() : new YieldingWaitStrategy();
		
		Disruptor<RunnableEvent> disruptor = new Disruptor<RunnableEvent>(RunnableEvent.EVENT_FACTORY, new ScheduledThreadPoolExecutor( inEventHandlers ), new MultiThreadedClaimStrategy( RING_SIZE ), waitStrategy);
		EventHandler<RunnableEvent>[] eventHandlers = new RunnableEventHandler[inEventHandlers];
		
		for( int i = 0; i < inEventHandlers; i++ )
		{
			eventHandlers[i] = new RunnableEventHandler(i, inEventHandlers );
		}
		
		disruptor.handleEventsWith( eventHandlers );
		
		ringBuffer = disruptor.start();
	}
	
	public void execute( IExecutable inRunnable )
	{
		long sequence = ringBuffer.next();
		RunnableEvent event = ringBuffer.get(sequence);

		event.setRunnable( inRunnable );
		
		ringBuffer.publish(sequence);
	}
	
}
