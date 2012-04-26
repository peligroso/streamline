package org.juxtapose.streamline.producer.executor;

import com.lmax.disruptor.EventHandler;

public class RunnableEventHandler implements EventHandler<RunnableEvent> {

	private final long ordinal;
	private final long numberOfConsumers;

	public RunnableEventHandler( int inOrdinal, int inNumberOfComsumenrs )
	{
		ordinal = inOrdinal;
		numberOfConsumers = inNumberOfComsumenrs;
	}
	
	@Override
	public void onEvent(RunnableEvent event, long sequence, boolean endOfBatch)throws Exception 
	{
		if( event.getRunnable() == null )
			return;
		
		if ( !((event.getHash() % numberOfConsumers) == ordinal))
			 return;
		
		event.getRunnable().run();
	}

}
