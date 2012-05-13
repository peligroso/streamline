package org.juxtapose.streamline.util.net;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.juxtapose.streamline.util.message.SubQuery;

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
//		if (receivedMessages == count) {
//			// Offer the answer after closing the connection.
//			e.getChannel().close().addListener(new ChannelFutureListener() {
//				public void operationComplete(ChannelFuture future) {
//					boolean offered = answer.offer((BigInteger) e.getMessage());
//					assert offered;
//				}
//			});
//		}
	}


	private void sendMessage(ChannelStateEvent e) 
	{
		Channel channel = e.getChannel();
		
		Map<String, String> query = new HashMap<String, String>();
		query.put("CCY1", "EUR");
		query.put("CCY2", "SEK");
		query.put("INST", "SP");
		
		SubQuery sq = new SubQuery("PE", query);
		
		channel.write(sq);
	}
}
