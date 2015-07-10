package org.juxtapose.streamline.gwt.stm;

import java.util.HashMap;
import java.util.Map;


public class JSRemoteServiceTracker extends JSRemoteServiceProxy{

	final static int SERVICE_TAG =  0;
	
	JSClientConnector clientConnector;
	
	HashMap<String, JSRemoteServiceProxy> serviceProxies = new HashMap<String, JSRemoteServiceProxy>();
	
	JSRemoteServiceTracker( JSSTM inSTM, JSClientConnector inConnector )
	{
		super( JSSTM.SERVICE_ID, inSTM, JSSTMConstants.STATUS_ON_REQUEST, inConnector );
		
		clientConnector = inConnector;
		
		Map<String, String> query = new HashMap<String, String>();
		query.put(JSSTMConstants.FIELD_QUERY_KEY, JSSTMConstants.PRODUCER_SERVICES );
		
		clientConnector.requestKey( this, JSSTM.SERVICE_ID, query, SERVICE_TAG);
	}
	
	public void registerProducer()
	{
		//Do not register since it is a mirror of STM
	}
	
	public void statusUpdated( String inService, String inStatus )
	{
		if( JSSTM.SERVICE_ID.equals( inService ) )
			return;
		
		JSRemoteServiceProxy serviceProxy = serviceProxies.get( inService );
		
		if( serviceProxy == null )
		{
			serviceProxy = new JSRemoteServiceProxy( inService, stm, inStatus, clientConnector );
		}
		else
			serviceProxy.updateStatus( inStatus );
		
	}
	
	public void remoteKeyDelivered( String inKey, Object inTag )
	{
//		producer = new RemoteServiceTrackerProducer( stm, inKey, clientConnector, this );
//		clientConnector.subscribe( producer, inKey );
	}
}
