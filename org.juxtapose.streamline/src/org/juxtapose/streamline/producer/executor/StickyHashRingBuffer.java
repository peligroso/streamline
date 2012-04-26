package org.juxtapose.streamline.producer.executor;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.MultiThreadedClaimStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
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
	public StickyHashRingBuffer( int inEventHandlers, Executor inExecutor )
	{
		Disruptor<RunnableEvent> disruptor = new Disruptor<RunnableEvent>(RunnableEvent.EVENT_FACTORY, inExecutor, new MultiThreadedClaimStrategy( RING_SIZE ), new SleepingWaitStrategy());
		EventHandler<RunnableEvent>[] eventHandlers = new RunnableEventHandler[inEventHandlers];
		
		for( int i = 0; i < inEventHandlers; i++ )
		{
			eventHandlers[i] = new RunnableEventHandler(i, inEventHandlers );
		}
		
		disruptor.handleEventsWith( eventHandlers );
		
		ringBuffer = disruptor.getRingBuffer();
	}
	
	public void execute( StickyRunnable inRunnable )
	{
		
		long sequence = ringBuffer.next();
		RunnableEvent event = ringBuffer.get(sequence);

		event.setRunnable( inRunnable );
		
		ringBuffer.publish(sequence);
	}
	
}
