package org.juxtapose.streamline.util.net;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.juxtapose.streamline.stm.STMEntryFactory;
import org.juxtapose.streamline.stm.STMUtil;
import org.juxtapose.streamline.util.BucketMap;
import org.juxtapose.streamline.util.DataConstants;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.ISTMEntrySubscriber;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.producerservices.ProducerServiceConstants;

import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.PersistentHashMap;

/**
 * 
 * @author Pontus
 *NOTE TO SELF.. HANDLERS ARE NOT!! THREADSAFE
 */
public class ClientConnectorHandler extends SimpleChannelUpstreamHandler 
{
	private final ISTM stm;
	
	RemoteServiceTracker serviceTracker;
	
	HashMap<Integer, ISTMEntryKey> referenceToKey = new HashMap<Integer, ISTMEntryKey>();
	HashMap< ISTMEntryKey, Integer> keyToReference = new HashMap<ISTMEntryKey, Integer>();
	
	HashMap<Integer, RemoteServiceProxy> tagToService = new HashMap<Integer, RemoteServiceProxy>(); 
	
	Channel channel;
	
	HashMap<ISTMEntryKey, RemoteProxyEntryProducer> keyTosubscribers = new HashMap<ISTMEntryKey, RemoteProxyEntryProducer>();
	
	public ClientConnectorHandler( ISTM inSTM ) 
	{
		stm = inSTM;
		serviceTracker = new RemoteServiceTracker( stm, this );
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
		
		sendRemoteServiceRequest(e, serviceTracker);
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
    		
//    		IPersistentMap<String, DataType<?>> map =  PersistentHashMap.emptyMap();
//    		map = PostMarshaller.parseDataMap( data, map );
//    		
//    		STMEntryFactory.
//    			
//    			if( serviceKey.equals( key ) )
//        		{
//    				Iterator<Entry<String, DataType<?>>> iterator = map.iterator();
//    				
//    				while( iterator.hasNext() )
//    				{
//    					Entry<String, DataType<?>> entry = iterator.next();
//    					
//    					String strStatus = (String)entry.getValue().get();
//    					Status serviceStatus = Status.valueOf( strStatus );
//    					
//    					if( !ProducerServiceConstants.STM_SERVICE_KEY.equals( entry.getKey() ) && !ProducerServiceConstants.DE_SERVICE_KEY.equals( entry.getKey() ) )
//    						serviceTracker.statusUpdated( entry.getKey(), serviceStatus );
//    				}        			
//        		}
//    		}
    	}
	}


	/**
	 * @param e
	 * @param inSubscriber
	 */
	private void sendRemoteServiceRequest( ChannelStateEvent e, ISTMEntryRequestSubscriber inSubscriber ) 
	{
		Channel channel = e.getChannel();
		
		Map<String, String> query = new HashMap<String, String>();
		query.put(DataConstants.FIELD_QUERY_KEY, STMUtil.PRODUCER_SERVICES );
		
		requestKey( inSubscriber, STMUtil.PRODUCER_SERVICES, query, SERVICE_TAG);
		
		Message mess = PreMarshaller.createSubQuery( ProducerServiceConstants.STM_SERVICE_KEY, SERVICE_TAG, query );
		
		channel.write(mess);
	}
	
	/**
	 * @param inSubscriber
	 * @param inService
	 * @param inQuery
	 * @param inTag
	 */
	public void requestKey( RemoteServiceProxy inProxy, String inService, Map<String, String> inQuery, Integer inTag)
	{
		tagToService.put( inTag, inProxy );
		
		Message mess = PreMarshaller.createSubQuery( inService, inTag, inQuery );
		
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
		RemoteServiceProxy service = tagToService.remove( inTag );
		
		if( service == null )
		{
			stm.logError( "Service to handle request to tag "+inTag+" has been removed");
			return;
		}
		
		keyToReference.put( inKey,  inRef );
		referenceToKey.put( inRef, inKey );
		
		service.remoteKeyDelivered( inKey, inTag );
		
	}
	
	/**
	 * @param inSubscriber
	 * @param inKey
	 */
	public void subscribe( ISTMEntrySubscriber inSubscriber, ISTMEntryKey inKey )
	{
		Integer ref = keyToReference.get( inKey );
		
		if( ref == null )
		{
			stm.logError( "referens for "+inKey+" can not be found in client handler.");
			return;
		}
		
		Message subMessage = PreMarshaller.createSubscriptionMessage( ref );
		channel.write( subMessage );
	}
	
	public void dataUpdated( ISTMEntryKey inKey, ISTMEntry inData )
	{
		Set<ISTMEntrySubscriber> subscribers = keyTosubscribers.get( inKey );
		
		if( subscribers != null )
		{
			for( ISTMEntrySubscriber subscriber : subscribers )
			{
				subscriber.updateData( inKey, inData, true );
			}
		}
	}
}
