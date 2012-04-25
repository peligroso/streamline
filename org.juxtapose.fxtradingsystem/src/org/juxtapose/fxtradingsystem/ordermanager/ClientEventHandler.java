package org.juxtapose.fxtradingsystem.ordermanager;

import com.lmax.disruptor.EventHandler;

public class ClientEventHandler implements EventHandler<ClientEvent>
{
	private final long ordinal;
	private final long numberOfConsumers;

	public ClientEventHandler( int inOrdinal, int inNumberOfComsumenrs )
	{
		ordinal = inOrdinal;
		numberOfConsumers = inNumberOfComsumenrs;
	}
	@Override
	public void onEvent(ClientEvent event, long sequence, boolean endOfBatch)throws Exception 
	{
		if ( !((sequence % numberOfConsumers) == ordinal))
			 return;
		 
		RFQMessage inCommingMess = event.message;

		if( inCommingMess.messageType == RFQMessage.TYPE_PRICING )
		{
			if( inCommingMess.firstTakeTime != null )
			{
				System.out.println( "FirstTakeTime for rfq "+inCommingMess.tag+" = "+inCommingMess.firstTakeTime+" with price "+inCommingMess.bidPrice+" / "+inCommingMess.askPrice+" sequence "+inCommingMess.sequence+" processed by Thread: "+Thread.currentThread().getName()  );
			}
			else
			{
				System.out.println( "Price "+inCommingMess.ccy1+inCommingMess.ccy2+" is "+inCommingMess.bidPrice+" / "+inCommingMess.askPrice+" sequence "+inCommingMess.sequence+" updatetime: "+inCommingMess.updateTime+" processed by Thread: "+Thread.currentThread().getName() );
			}
		}
	}
}
