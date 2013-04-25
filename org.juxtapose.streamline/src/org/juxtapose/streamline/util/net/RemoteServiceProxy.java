package org.juxtapose.streamline.util.net;

import java.util.Map;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.producer.ISTMEntryProducerService;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.Status;

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
	
	/**
	 * @param inServiceID
	 * @param inSTM
	 * @param inStatus
	 */
	public RemoteServiceProxy( String inServiceID, ISTM inSTM, Status inStatus ) 
	{
		serviceID = inServiceID;
		stm = inSTM;
		status = inStatus;
		
		stm.registerProducer( this, status );
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
		
	}

	@Override
	public ISTMEntryProducer getDataProducer( ISTMEntryKey inDataKey ) 
	{
		return null;
	}}
