package org.juxtapose.streamline.util.net;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
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
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;

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
	
	HashMap<Integer, ISTMEntryKey> referenceToKey = new HashMap<Integer, ISTMEntryKey>();
	HashMap< ISTMEntryKey, Integer> keyToReference = new HashMap<ISTMEntryKey, Integer>();
	
	HashMap<Object, RemoteServiceProxy> tagToService = new HashMap<Object, RemoteServiceProxy>(); 
	
	Channel channel;
	
	HashMap<ISTMEntryKey, RemoteProxyEntryProducer> keyToSubscriber = new HashMap<ISTMEntryKey, RemoteProxyEntryProducer>();
	
	Integer tagInc = 0;
	HashMap<Integer, Object> tagRefToTag = new HashMap<Integer, Object>();
	
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
    		
    		ISTMEntryKey key = referenceToKey.get( reference );
    		
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
    		
    		producer.updateData( key, map, true );
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
		
		if( service == null )
		{
			stm.logError( "Service to handle request to tag "+inTag+" has been removed");
			return;
		}
		
		keyToReference.put( inKey,  inRef );
		referenceToKey.put( inRef, inKey );
		
		service.remoteKeyDelivered( inKey, tag );
		
	}
	
	/**
	 * @param inSubscriber
	 * @param inKey
	 */
	public void subscribe( RemoteProxyEntryProducer inProducer, ISTMEntryKey inKey )
	{
		Integer ref = keyToReference.get( inKey );
		
		if( ref == null )
		{
			stm.logError( "referens for "+inKey+" can not be found in client handler.");
			return;
		}
		
		keyToSubscriber.put( inKey, inProducer );
		
		Message subMessage = PreMarshaller.createSubscriptionMessage( ref );
		channel.write( subMessage );
	}
	
}
