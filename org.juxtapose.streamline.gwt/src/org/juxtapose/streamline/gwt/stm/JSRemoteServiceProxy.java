package org.juxtapose.streamline.gwt.stm;

import java.util.HashMap;
import java.util.Map;



public class JSRemoteServiceProxy implements IJSSTMEntryProducerService 
{
	String serviceID;
	JSClientConnector clientConnector;
	JSSTM stm;
	String status;
	
	HashMap<String, IJSSTMEntrySubscriber> tagToSubscriber = new HashMap<String, IJSSTMEntrySubscriber>();
	
	Map<String, IJSSTMEntryProducer> keyToProducer = new HashMap<String, IJSSTMEntryProducer>();
	
	public JSRemoteServiceProxy( String inServiceID, JSSTM inSTM, String inStatus, JSClientConnector inConnector ) 
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

	@Override
	public String getServiceId() 
	{
		return serviceID;
	}
	
	/**
	 * @param inStatus
	 */
	public void updateStatus( String inStatus )
	{
		status = inStatus;
	}


	@Override
	public void getDataKey( IJSSTMEntrySubscriber inSubscriber, String inTag, Map<String, String> inQuery )
	{
		tagToSubscriber.put( inTag, inSubscriber );
		clientConnector.requestKey( this, serviceID, inQuery, inTag );
		
	}

	@Override
	public IJSSTMEntryProducer getDataProducer( String inDataKey )
	{
		IJSSTMEntryProducer producer = keyToProducer.get( inDataKey );
		
		if( producer == null )		{
//			producer = new RemoteProxyEntryProducer( stm, inDataKey, clientConnector );
		}
		return producer;
	}

}
