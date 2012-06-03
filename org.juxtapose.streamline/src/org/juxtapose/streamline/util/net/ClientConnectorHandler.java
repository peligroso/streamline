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
import org.juxtapose.streamline.util.Status;

public class ClientConnectorHandler extends SimpleChannelUpstreamHandler 
{
	
	public ClientConnectorHandler() 
	{
		
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
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		sendMessage(e);
	}

	@Override
	public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) {
		sendMessage(e);
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
    		int tag = subMess.getTag();
    	}
	}


	private void sendMessage(ChannelStateEvent e) 
	{
		Channel channel = e.getChannel();
		
		Map<String, String> query = new HashMap<String, String>();
		query.put("CCY1", "EUR");
		query.put("CCY2", "SEK");
		query.put("INST", "SP");
		query.put("T", "PRICE");
		
		Message mess = PreMarshaller.createSubQuery( "PE", 1, query );
		
		channel.write(mess);
	}
}
