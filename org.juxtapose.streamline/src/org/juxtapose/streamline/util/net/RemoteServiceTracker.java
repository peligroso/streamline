package org.juxtapose.streamline.util.net;

import java.util.HashMap;
import java.util.Map;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.executor.IExecutor;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.stm.STMUtil;
import org.juxtapose.streamline.util.DataConstants;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.producerservices.ProducerServiceConstants;

/**
 * @author Pontus Jörgne
 * 25 apr 2013
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class RemoteServiceTracker extends RemoteServiceProxy implements ISTMEntryRequestSubscriber
{
	HashMap<String, RemoteServiceProxy> serviceProxies = new HashMap<String, RemoteServiceProxy>();

	final static int SERVICE_TAG =  0;
	
	RemoteServiceTrackerProducer producer;
	
	public RemoteServiceTracker( ISTM inSTM, ClientConnectorHandler inClientConnector )
	{
		super( ProducerServiceConstants.STM_SERVICE_KEY, inSTM, Status.ON_REQUEST, inClientConnector );
		
		Map<String, String> query = new HashMap<String, String>();
		query.put(DataConstants.FIELD_QUERY_KEY, STMUtil.PRODUCER_SERVICES );
		
		clientConnector.requestKey( this, ProducerServiceConstants.STM_SERVICE_KEY, query, SERVICE_TAG);
	}
	
	public void registerProducer()
	{
		//Do not register since it is a mirror of STM
	}
	
	/**
	 * @param inService
	 * @param inStatus
	 */
	public void statusUpdated( String inService, Status inStatus )
	{
		if( ProducerServiceConstants.STM_SERVICE_KEY.equals( inService ) || ProducerServiceConstants.DE_SERVICE_KEY.equals( inService ))
			return;
		
		RemoteServiceProxy serviceProxy = serviceProxies.get( inService );
		
		if( serviceProxy == null )
		{
			serviceProxy = new RemoteServiceProxy( inService, stm, inStatus, clientConnector );
		}
		else
			serviceProxy.updateStatus( inStatus );
		
	}
	
	public void remoteKeyDelivered( ISTMEntryKey inKey, Object inTag )
	{
		producer = new RemoteServiceTrackerProducer( stm, inKey, clientConnector, this );
		clientConnector.subscribe( producer, inKey );
	}

	@Override
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData, boolean inFirstUpdate ) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getPriority() 
	{
		return IExecutor.LOW;
	}

	@Override
	public void deliverKey( ISTMEntryKey inDataKey, Object inTag ) 
	{
		clientConnector.subscribe( producer, inDataKey );
	}

	@Override
	public void queryNotAvailible( Object inTag ) 
	{
		
	}
	
}
