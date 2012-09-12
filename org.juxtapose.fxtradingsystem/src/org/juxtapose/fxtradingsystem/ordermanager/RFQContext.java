package org.juxtapose.fxtradingsystem.ordermanager;

import org.juxtapose.streamline.util.subscriber.DataSequencer;

public class RFQContext
{
	public final DataSequencer sequencer;
	public final RFQLiquidityProducer producer;
	public final long startTime;
	
	public RFQContext( DataSequencer inSequencer, RFQLiquidityProducer inProducer, long inStartTime )
	{
		sequencer = inSequencer;
		producer = inProducer;
		startTime = inStartTime;
		
	}
}
