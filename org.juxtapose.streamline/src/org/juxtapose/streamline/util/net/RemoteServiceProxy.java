package org.juxtapose.streamline.util.net;

import java.util.HashMap;
import java.util.Map;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.producer.ISTMEntryProducerService;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.ISTMRequestor;
import org.juxtapose.streamline.util.Status;

import com.trifork.clj_ds.IPersistentMap;

/**
 * @author Pontus Jörgne
 * 25 apr 2013
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class RemoteServiceProxy implements ISTMEntryProducerService
{
	final String serviceID;
	
	Status status;
	final ISTM stm;
	
	ClientConnectorHandler clientConnector;
	
	Map<ISTMEntryKey, ISTMEntryProducer> keyToProducer = new HashMap<ISTMEntryKey, ISTMEntryProducer>();
	
	HashMap<Object, ISTMEntryRequestSubscriber> tagToSubscriber = new HashMap<Object, ISTMEntryRequestSubscriber>();
	
	/**
	 * @param inServiceID
	 * @param inSTM
	 * @param inStatus
	 */
	public RemoteServiceProxy( String inServiceID, ISTM inSTM, Status inStatus, ClientConnectorHandler inConnector ) 
	{
		serviceID = inServiceID;
		stm = inSTM;
		status = inStatus;
		clientConnector = inConnector;
		
		registerProducer();
	}
	
	public void registerProducer()
	{
		stm.registerProducer( this, status );
	}
	
	public void updateStatus( Status inStatus )
	{
		status = inStatus;
		stm.updateProducerStatus( this, inStatus );
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.producer.ISTMEntryProducerService#getServiceId()
	 */
	@Override
	public String getServiceId() 
	{
		return serviceID;
	}

	@Override
	public void getDataKey( ISTMEntryRequestSubscriber inSubscriber, Object inTag, Map<String, String> inQuery ) 
	{
		tagToSubscriber.put( inTag, inSubscriber );
		clientConnector.requestKey( this, serviceID, inQuery, inTag );
	}
	
	public void remoteKeyDelivered( ISTMEntryKey inKey, Object inTag )
	{
		ISTMEntryRequestSubscriber subscriber = tagToSubscriber.remove( inTag );
		
		if( subscriber == null )
		{
			stm.logError( "Subscriber for key "+inKey+" not found in remote proxy service" );
			return;
		}
		
		subscriber.deliverKey( inKey, inTag );
	}

	@Override
	public ISTMEntryProducer getDataProducer( ISTMEntryKey inDataKey )
	{
		ISTMEntryProducer producer = keyToProducer.get( inDataKey );
		
		if( producer == null )		{
			producer = new RemoteProxyEntryProducer( stm, inDataKey, clientConnector );
		}
		return producer;
	}
	
	public void request( int inTag, long inType, ISTMRequestor inRequestor, String inVariable, IPersistentMap<String, Object> inData )
	{
		clientConnector.request( inTag, inType, inRequestor, serviceID, inVariable, inData );
	}
}
	
