package org.juxtapose.streamline.laboration.netty;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;


public class TimeServerHandler extends SimpleChannelHandler {

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) 
	{
		DiscardServer.allChannels.add(e.getChannel());
		UnixTime time = new UnixTime( (int)(System.currentTimeMillis() / 1000) );
		ChannelFuture f = e.getChannel().write(time);
		f.addListener(ChannelFutureListener.CLOSE);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		e.getCause().printStackTrace();
		e.getChannel().close();
	}
}
