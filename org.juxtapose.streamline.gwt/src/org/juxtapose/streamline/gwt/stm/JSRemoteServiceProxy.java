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

	@Override
	public void getDataKey( IJSSTMEntrySubscriber inSubscriber, Object inTag,
			Map<String, String> inQuery ) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IJSSTMEntryProducer getDataProducer( String inDataKey ) {
		// TODO Auto-generated method stub
		return null;
	}

}
