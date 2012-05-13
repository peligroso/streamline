package org.juxtapose.streamline.util.net;

import java.math.BigInteger;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.juxtapose.streamline.util.message.SubQuery;

public class ServerConnectorHandler extends SimpleChannelUpstreamHandler 
{	  
    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception 
    {
        super.handleUpstream(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) 
    {
        // Calculate the cumulative factorial and send it to the client.
        if (e.getMessage() instanceof SubQuery) {
        	System.out.println("got query");
        } else 
        {
        
        }
        
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception 
    {
        //TODO
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext ctx, ExceptionEvent e) 
    {
        e.getChannel().close();
    }
}