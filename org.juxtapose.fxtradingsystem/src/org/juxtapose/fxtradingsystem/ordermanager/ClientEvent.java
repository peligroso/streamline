package org.juxtapose.fxtradingsystem.ordermanager;

import com.lmax.disruptor.EventFactory;

public class ClientEvent
{
	RFQMessage message;
	
	public void setMessage( RFQMessage inMessage )
	{
		message = inMessage;
	}
	
	public RFQMessage getMessage()
	{
		return message;
	}

	public final static EventFactory<ClientEvent> EVENT_FACTORY = new EventFactory<ClientEvent>()
	{
		public ClientEvent newInstance()
		{
			return new ClientEvent();
		}
	};

}
