package org.juxtapose.streamline.util.net;

import java.util.HashMap;
import java.util.Map;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.stm.STMUtil;
import org.juxtapose.streamline.util.DataConstants;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.Status;

/**
 * @author Pontus Jörgne
 * 25 apr 2013
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class RemoteServiceTracker implements ISTMEntryRequestSubscriber
{
	HashMap<String, RemoteServiceProxy> serviceProxies = new HashMap<String, RemoteServiceProxy>();

	ISTM stm;
	ClientConnectorHandler clientConnector;
	
	final static int SERVICE_TAG =  0;
	private ISTMEntryKey serviceKey;
	
	public RemoteServiceTracker( ISTM inSTM, ClientConnectorHandler inClientConnector )
	{
		stm = inSTM;
		clientConnector = inClientConnector;
		
		Map<String, String> query = new HashMap<String, String>();
		query.put(DataConstants.FIELD_QUERY_KEY, STMUtil.PRODUCER_SERVICES );
		
		clientConnector.requestKey( this, STMUtil.PRODUCER_SERVICES, query, SERVICE_TAG);
	}
	
	/**
	 * @param inService
	 * @param inStatus
	 */
	public void statusUpdated( String inService, Status inStatus )
	{
		RemoteServiceProxy serviceProxy = serviceProxies.get( inService );
		
		if( serviceProxy == null )
		{
			serviceProxy = new RemoteServiceProxy( inService, stm, inStatus );
		}
		
	}

	@Override
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData, boolean inFirstUpdate ) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getPriority() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void deliverKey( ISTMEntryKey inDataKey, Object inTag ) 
	{
		clientConnector.subscribe( this, inDataKey );
	}

	@Override
	public void queryNotAvailible( Object inTag ) 
	{
		
	}
	
}
