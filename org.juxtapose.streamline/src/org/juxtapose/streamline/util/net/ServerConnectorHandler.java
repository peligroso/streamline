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
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.DataKey;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.Message;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.SubQueryMessage;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.SubscribeMessage;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.Status;
import static org.juxtapose.streamline.tools.Preconditions.*;

/**
 * @author Pontus Jörgne
 * May 27, 2012
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public final class ServerConnectorHandler extends SimpleChannelUpstreamHandler implements ISTMEntryRequestSubscriber
{	
	static int OPTIMISTIC_REF = -1;
	
	final ISTM stm;
	
	ReferenceStore refStore = new ServerReferenceStore();
	
	HashSet< ISTMEntryKey> fullUpdateSent = new HashSet<ISTMEntryKey>();
	
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

    /* (non-Javadoc)
     * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
     */
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
    		
    		postSubQuery( service, (long)tag, queryMap );
    	}
    	else if( message.getType() == Message.Type.SubscribeMessage )
    	{
    		SubscribeMessage subMess = message.getSubscribeMessage();
    		
    		Integer ref = subMess.getReference();
    		
    		ISTMEntryKey key;
    		
    		if( ref != null )
    		{
    			key = refStore.getKeyFromRef( subMess.getReference() );
    			notNull( key, "Key for reference : "+subMess.getReference()+" not found" );
    		}
    		else
    		{
    			//optimistic subscribe witout previous query
    			DataKey dataKey = subMess.getKey();
    			notNull( dataKey, "Both key and Ref is missing from subMessage" );
    			
    			key = PostMarshaller.parseKey( dataKey );
    			refStore.addReference( OPTIMISTIC_REF, key );
    		}
    		
    		stm.subscribeToData( key, this );
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
    	removeSubscriptions();
    	
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext ctx, ExceptionEvent e) 
    {
        e.getChannel().close();
    }
    
    private void removeSubscriptions()
    {
    	for( ISTMEntryKey key : refStore.getAllKeys() )
    	{
    		stm.unsubscribeToData( key, this );
    	}
    	
    	refStore.clear();
    }

	@Override
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData, boolean inFirstUpdate ) 
	{
		//We dont care about initializing update since the client is already in state initializing
		if( inData.getStatus() == Status.ON_REQUEST )
			return;
		
		Integer ref = refStore.getRefFromKey( inKey );
		notNull( ref, "Reference for key : "+inKey+" not found" );
		
		Message mess;
		if( ref == OPTIMISTIC_REF )
		{
			ref = refStore.addReference( inKey );
			mess = PreMarshaller.createUpdateMessage( ref, inData, true );
		}
		else
		{	
			boolean fullUpdate = fullUpdateSent.remove( inKey );
			mess = PreMarshaller.createUpdateMessage( ref, inData, fullUpdate );
		}
		
			
		clientChannel.write( mess );
	}
	

	@Override
	public void deliverKey( ISTMEntryKey inDataKey, Object inTag ) 
	{	
		int ref = refStore.addReference( inDataKey );
		
		fullUpdateSent.add( inDataKey );
		
		Message mess = PreMarshaller.createSubResponse( (Long)inTag, ref,Status.OK, inDataKey, null );
		clientChannel.write( mess );
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