package org.juxtapose.streamline.producer.executor;

import com.lmax.disruptor.EventFactory;

/**
 * @author Pontus
 *
 */
public class RunnableEvent 
{
	private IExecutable runnable;
	
	/**
	 * @param inRun
	 * @param inHash
	 */
	public void setRunnable( IExecutable inRun )
	{
		runnable = inRun;
	}
	
	/**
	 * @return
	 */
	public IExecutable getExecutable()
	{
		return runnable;
	}
	
	public int getHash()
	{
		return runnable.getHash();
	}

	public final static EventFactory<RunnableEvent> EVENT_FACTORY = new EventFactory<RunnableEvent>()
	{
		public RunnableEvent newInstance()
		{
			return new RunnableEvent();
		}
	};
}
