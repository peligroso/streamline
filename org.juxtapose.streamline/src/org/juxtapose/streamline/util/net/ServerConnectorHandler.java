package org.juxtapose.streamline.util.net;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.executor.Executable;
import org.juxtapose.streamline.producer.executor.IExecutor;
import org.juxtapose.streamline.protocol.message.PostMarshaller;
import org.juxtapose.streamline.protocol.message.PreMarshaller;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.Message;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.SubQueryMessage;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.Status;

/**
 * @author Pontus Jörgne
 * May 27, 2012
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public final class ServerConnectorHandler extends SimpleChannelUpstreamHandler implements ISTMEntryRequestSubscriber
{	
	final ISTM stm;
	
	HashMap<Integer, ISTMEntryKey> referenceToKey = new HashMap<Integer, ISTMEntryKey>();
	
	AtomicInteger referenceIncrement = new AtomicInteger( 0 );
	
	HashMap< ISTMEntryKey, Long > keyPendingReply = new HashMap< ISTMEntryKey, Long >();
	
	Channel clientChannel;
	
	public ServerConnectorHandler( ISTM inSTM )
	{
		stm = inSTM;
	}
	
    @Override
    public final void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception 
    {
        super.handleUpstream(ctx, e);
    }

    @Override
    public final void messageReceived(ChannelHandlerContext ctx, MessageEvent e) 
    {
    	Message message = (Message)e.getMessage();
    	if( message.getType() == Message.Type.SubQueryMessage )
    	{
    		SubQueryMessage subMess = message.getSubQueryMessage();
    		String service = subMess.getService();
    		Map<String, String> queryMap = PostMarshaller.parseQueryMap( subMess );
    		int tag = subMess.getTag();
    		
    		Channel ch = ctx.getChannel();
    		
    		postSubQuery( service, (long)tag, queryMap );
    	}
    	else
    	{
    		stm.logError( "Unknown message recieved: "+e.getMessage().getClass() );
    	}
    }
    
    public final void postSubQuery( final String inService, final long inTag, final Map<String, String> inQuery )
    {
    	stm.execute( new Executable() {
			
			@Override
			public void run() 
			{
				stm.getDataKey( inService, ServerConnectorHandler.this, inTag, inQuery );
			}
		}, IExecutor.LOW );
    }
   
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception 
    {
    	clientChannel = ctx.getChannel();
        stm.logInfo( "Client connected.." );
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception 
    {
    	stm.logInfo( "Client disconnected.." );
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext ctx, ExceptionEvent e) 
    {
        e.getChannel().close();
    }

	@Override
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData, boolean inFirstUpdate ) 
	{
		Long tag = keyPendingReply.remove( inKey );
		
		if( tag != null )
		{
			int ref = referenceIncrement.incrementAndGet();
			referenceToKey.put( ref, inKey );
			Message mess = PreMarshaller.createSubResponse( tag, ref, inData.getStatus(), inData );
			
			clientChannel.write( mess );
		}
	}

	@Override
	public void deliverKey( ISTMEntryKey inDataKey, Object inTag ) 
	{	
		keyPendingReply.put( inDataKey, (Long)inTag );
		
		stm.subscribeToData( inDataKey, this );
	}

	@Override
	public void queryNotAvailible( Object inTag ) 
	{
		Message mess = PreMarshaller.createSubResponse( (Long)inTag, -1, Status.NA );
		clientChannel.write( mess );
	}

	@Override
	public int getPriority() 
	{
		return IExecutor.LOW;
	}
}