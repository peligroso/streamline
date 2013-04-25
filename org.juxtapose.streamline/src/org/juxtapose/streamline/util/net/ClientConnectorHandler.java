package org.juxtapose.streamline.util.net;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.juxtapose.streamline.protocol.message.PreMarshaller;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.Message;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.SubQueryResponseMessage;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.stm.STMUtil;
import org.juxtapose.streamline.util.DataConstants;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.producerservices.ProducerServiceConstants;

public class ClientConnectorHandler extends SimpleChannelUpstreamHandler 
{
	private final ISTM stm;
	
	public ClientConnectorHandler( ISTM inSTM ) 
	{
		stm = inSTM;
	}

	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception 
	{
		if (e instanceof ChannelStateEvent) 
		{
			
		}
		super.handleUpstream(ctx, e);
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) 
	{
		stm.logInfo("Connection recieved..");
		sendStatusMessage(e);
	}

	@Override
	public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) 
	{
		
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) 
	{
		Message message = (Message)e.getMessage();
    	if( message.getType() == Message.Type.SubQueryResponseMessage )
    	{
    		SubQueryResponseMessage subMess = message.getSubQueryResponseMessage();
    		int statusInt = subMess.getStatus();
    		Status status = Status.values()[statusInt];
    		
    		if( status != Status.OK )
    			return;
    		
    		int ref = subMess.getReference();
    		
    		
    				
    		int tag = subMess.getTag();
    		
    	}
	}


	private void sendStatusMessage(ChannelStateEvent e) 
	{
		Channel channel = e.getChannel();
		
		Map<String, String> query = new HashMap<String, String>();
		query.put(DataConstants.FIELD_QUERY_KEY, STMUtil.PRODUCER_SERVICES );
		
		Message mess = PreMarshaller.createSubQuery( ProducerServiceConstants.STM_SERVICE_KEY, 1, query );
		
		channel.write(mess);
	}
}
