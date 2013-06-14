package org.juxtapose.streamline.util.net;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.protocol.message.PostMarshaller;
import org.juxtapose.streamline.protocol.message.PreMarshaller;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.DataKey;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.DataMap;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.Message;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.SubQueryResponseMessage;
import org.juxtapose.streamline.protocol.message.StreamDataProtocol.UpdateMessage;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.util.ISTMRequestor;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;
import static org.juxtapose.streamline.tools.Preconditions.*;

import com.sun.istack.internal.NotNull;
import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.PersistentHashMap;

/**
 * @author Pontus Jörgne
 * 2 maj 2013
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class ClientConnectorHandler extends SimpleChannelUpstreamHandler 
{
	private final ISTM stm;
	
	RemoteServiceTracker serviceTracker;
	
	ReferenceStore refStore = new ClientReferenceStore();
	
	HashMap<Object, RemoteServiceProxy> tagToService = new HashMap<Object, RemoteServiceProxy>(); 
	
	Channel channel;
	
	HashMap<ISTMEntryKey, RemoteProxyEntryProducer> keyToSubscriber = new HashMap<ISTMEntryKey, RemoteProxyEntryProducer>();
	
	/**TICKET This needs to be handled more appropriately with a unique request id mapped to the individual clients request tag**/
	Integer tagInc = 0;
	
	HashMap<Integer, Object> tagRefToTag = new HashMap<Integer, Object>();
	
	HashMap<Integer, Object> requestToTag = new HashMap<Integer, Object>();
	
	
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
		channel = ctx.getChannel();
		
		serviceTracker = new RemoteServiceTracker( stm, this );
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
    		
    		int ref = subMess.getReference();
    		int tag = subMess.getTag();
    		
    		DataKey dataKey = subMess.getKey();
    		ISTMEntryKey key = null;
    		
    		if( dataKey != null )
    		{
    			key = PostMarshaller.parseKey( dataKey );
    		}
    		
    		DataMap data = subMess.getData();
    		IPersistentMap<String, DataType<?>> pData = null;
    		
    		if( data != null )
    		{
    			pData = PersistentHashMap.EMPTY;
    			pData = PostMarshaller.parseDataMap( data, pData );
    		}
    		
    		queryResponse( status, key, ref, tag, pData );
    	}
    	else if( message.getType() == Message.Type.UpdateMessage )
    	{
    		UpdateMessage update = message.getUpdateMessage();
    		int reference = update.getReference();
    		boolean fullUpdate = update.hasFullupdate() ? update.getFullupdate() : true;
    		
    		ISTMEntryKey key = refStore.getKeyFromRef( reference );
    		
    		if( key == null )
    		{
    			stm.logError( "Key for ref "+reference+" not found" );
    			return;
    		}
    		
    		DataMap data = update.getData();
    		
    		RemoteProxyEntryProducer producer = keyToSubscriber.get( key );
    		
    		if( producer == null )
    		{
    			stm.logError( "Remote entry Producer for key "+key+" not found" );
    			return;
    		}
    		
    		IPersistentMap<String, DataType<?>> map =  PersistentHashMap.emptyMap();
    		map = PostMarshaller.parseDataMap( data, map );
    		
    		producer.updateData( key, map, fullUpdate );
    	}
	}


	
	/**
	 * @param inSubscriber
	 * @param inService
	 * @param inQuery
	 * @param inTag
	 */
	public void requestKey( RemoteServiceProxy inProxy, String inService, Map<String, String> inQuery, Object inTag)
	{
		Integer tagRef = tagInc++;
		tagRefToTag.put( tagRef, inTag );
		
		tagToService.put( tagRef, inProxy );
		
		Message mess = PreMarshaller.createSubQuery( inService, tagRef, inQuery );
		
		channel.write(mess);
	}
	
	/**
	 * @param inStatus
	 * @param inKey
	 * @param inRef
	 * @param inTag
	 * @param inData
	 */
	public void queryResponse( Status inStatus, ISTMEntryKey inKey, Integer inRef, Integer inTag, IPersistentMap<String, DataType<?>> inData  )
	{
		Object tag = tagRefToTag.remove( inTag );
		
		if( tag == null )
		{
			stm.logError( "Tag for tagRef "+inTag+" could not be found");
			return;
		}
		
		RemoteServiceProxy service = tagToService.remove( inTag );
		
		notNull( service );
		
		refStore.addReference( inRef, inKey );
		
		service.remoteKeyDelivered( inKey, tag );
		
	}
	
	/**
	 * @param inSubscriber
	 * @param inKey
	 */
	public void subscribe( RemoteProxyEntryProducer inProducer, ISTMEntryKey inKey )
	{
		Integer ref = refStore.getRefFromKey( inKey );
		
		Message subMessage;
		
		if( ref == null )
		{
			//Optimistic request
			ref = refStore.addReference( inKey );
			subMessage = PreMarshaller.createSubscriptionMessage( ref, inKey );
		}
		else
		{
			subMessage = PreMarshaller.createSubscriptionMessage( ref );
		}
		keyToSubscriber.put( inKey, inProducer );
		
		
		channel.write( subMessage );
	}
	
	/**
	 * @param inTag
	 * @param inRequestor
	 * @param inVariable
	 * @param inData
	 */
	public void request( int inTag, ISTMRequestor inRequestor, String inService, String inVariable, IPersistentMap<String, DataType<?>> inData )
	{
		requestToTag.put( inTag, inRequestor );
		Message mess = PreMarshaller.createRequestMessage( inTag, inService, inVariable, inData );
		channel.write( mess );
	}
	
	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
	 */
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
	{   
	    stm.logError(e.toString(), e.getCause());
	}
	
}
