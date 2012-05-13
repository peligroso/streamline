package org.juxtapose.streamline.laboration.netty;

import static org.jboss.netty.buffer.ChannelBuffers.dynamicBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class TimeClientHandler extends SimpleChannelHandler 
{
	private final ChannelBuffer buf = dynamicBuffer();
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) 
	{
		UnixTime ut = (UnixTime) e.getMessage();
		System.out.println(ut);
		e.getChannel().close();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		e.getCause().printStackTrace();
		e.getChannel().close();
	}
}
