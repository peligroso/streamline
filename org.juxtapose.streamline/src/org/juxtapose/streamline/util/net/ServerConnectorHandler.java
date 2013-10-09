package org.juxtapose.streamline.util.net;

import static org.juxtapose.streamline.tools.Preconditions.notNull;

import java.util.HashSet;
import java.util.Map;

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
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.DataMap;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.Message;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.RequestMessage;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.SubQueryMessage;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.SubscribeMessage;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.UnsubscribeMessage;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.ISTMRequestor;
import org.juxtapose.streamline.util.Status;

import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.PersistentHashMap;

/**
 * @author Pontus Jörgne
 * May 27, 2012
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public final class ServerConnectorHandler extends SimpleChannelUpstreamHandler implements ISTMEntryRequestSubscriber, ISTMRequestor
{	
	final ISTM stm;
	
	ReferenceStore refStore = new ServerReferenceStore();
	
	HashSet< ISTMEntryKey> fullUpdateSent = new HashSet<ISTMEntryKey>();
	
	Channel clientChannel;
	
	public ServerConnectorHandler( ISTM inSTM )
	{
		stm = inSTM;
	}
	
    /* (non-Javadoc)
     * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#handleUpstream(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelEvent)
     */
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
    		
    		if( subMess.hasKey() )
    		{
    			//Optimistic subscribe
    			DataKey dKey = subMess.getKey();
    			key = PostMarshaller.parseKey( dKey );
    			refStore.addReference( ref, key );
    		}
    		else
    		{
    			key = refStore.getKeyFromRef( ref );
    			notNull( key, "There is no key for reference "+ref );
    		}
    		
    		stm.subscribeToData( key, this );
    	}
    	else if( message.getType() == Message.Type.RequestMessage )
    	{
    		RequestMessage rm = message.getRequestMessage();
    		String service = rm.getService();
    		int tag = rm.getTag();
    		long type = rm.getType();
    		String variable = rm.getVariable();
    		
    		IPersistentMap<String, Object> data = PersistentHashMap.EMPTY; 
    		if( rm.hasData() )
    		{
    			DataMap dataMap = rm.getData();
    			data = PostMarshaller.parseDataMap( dataMap, data );
    		}
    		
    		stm.request( service, tag, type, this, variable, data );
    	}
    	else if( message.getType() == Message.Type.UnSubscribeMessage )
    	{
    		UnsubscribeMessage unSubMess = message.getUnsubscribeMessage();
    		
    		Integer ref = unSubMess.getReference();
    		
    		ISTMEntryKey key = refStore.getKeyFromRef( ref );
    		
    		if( key == null )
    		{
    			stm.logError( "Unknown message recieved: "+e.getMessage().getClass() );
    			return;
    		}
    		
    		stm.unsubscribeToData( key, this );
    	}
    	else
    	{
    		stm.logError( "Unknown message recieved: "+e.getMessage().getClass() );
    	}
    }
    
    /**
     * @param inService
     * @param inTag
     * @param inQuery
     */
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
   
    /* (non-Javadoc)
     * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
     */
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception 
    {
    	clientChannel = ctx.getChannel();
        stm.logInfo( "Client connected.." );
    }

    /* (non-Javadoc)
     * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelDisconnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
     */
    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception 
    {
    	stm.logInfo( "Client disconnected.." );
    	removeSubscriptions();
    	
    }

    /* (non-Javadoc)
     * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
     */
    @Override
    public void exceptionCaught( ChannelHandlerContext ctx, ExceptionEvent e) 
    {
    	stm.logError( e.toString(), e.getCause() );
        e.getChannel().close();
    }
    
    /**
     * 
     */
    private void removeSubscriptions()
    {
    	for( ISTMEntryKey key : refStore.getAllKeys() )
    	{
    		stm.unsubscribeToData( key, this );
    	}
    	
    	refStore.clear();
    }

	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.util.ISTMEntrySubscriber#updateData(org.juxtapose.streamline.producer.ISTMEntryKey, org.juxtapose.streamline.util.ISTMEntry, boolean)
	 */
	@Override
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData, boolean inFullUpdate ) 
	{
		//We dont care about initializing update since the client is already in state initializing
		if( inData.getStatus() == Status.ON_REQUEST )
			return;
		
		Integer ref = refStore.getRefFromKey( inKey );
		notNull( ref, "Reference for key : "+inKey+" not found" );
		
		boolean fullUpdate = fullUpdateSent.remove( inKey ) || inFullUpdate;
		Message mess = PreMarshaller.createUpdateMessage( ref, inData, fullUpdate );
		
		clientChannel.write( mess );
	}
	

	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.util.ISTMEntryRequestSubscriber#deliverKey(org.juxtapose.streamline.producer.ISTMEntryKey, java.lang.Object)
	 */
	@Override
	public void deliverKey( ISTMEntryKey inDataKey, Object inTag ) 
	{	
		int ref = refStore.addReference( inDataKey );
		
		fullUpdateSent.add( inDataKey );
		
		Message mess = PreMarshaller.createSubResponse( (Long)inTag, ref,Status.OK, inDataKey, null );
		clientChannel.write( mess );
	}

	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.util.ISTMEntryRequestSubscriber#queryNotAvailible(java.lang.Object)
	 */
	@Override
	public void queryNotAvailible( Object inTag ) 
	{
		Message mess = PreMarshaller.createSubResponse( (Long)inTag, -1, Status.NA );
		clientChannel.write( mess );
	}

	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.util.ISTMEntrySubscriber#getPriority()
	 */
	@Override
	public int getPriority() 
	{
		return IExecutor.LOW;
	}

	@Override
	public void reply( int inTag, long inType, String inMessage, IPersistentMap<String, Object> inData )
	{
		// TODO Auto-generated method stub
		
	}
	
}