package org.juxtapose.streamline.util.net;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.producer.ISTMEntryProducerService;
import org.juxtapose.streamline.producer.STMEntryProducer;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.stm.STMTransaction;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeNull;

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
	
	public void remoteKeyDelivered( ISTMEntryKey inKey, Object inTag )
	{
		//TODO pass on to subscriber
	}

	@Override
	public ISTMEntryProducer getDataProducer( ISTMEntryKey inDataKey ) 
	{
		ISTMEntryProducer producer = keyToProducer.get( inDataKey );
		
		if( producer == null )
		{
			producer = new RemoteProxyEntryProducer();
		}
		return producer;
	}
	
	protected void dataUpdated( ISTMEntryKey inKey, final IPersistentMap<String, DataType<?>> inData, boolean inFullUpdate )
	{
		stm.commit( new STMTransaction( inKey )
		{
			@Override
			public void execute()
			{
				Iterator<Map.Entry<String, DataType<?>>> iterator = inData.iterator();
				while( iterator.hasNext() )
				{
					Map.Entry<String, DataType<?>> entry = iterator.next();
					
					if( entry.getValue() instanceof DataTypeNull )
					{
						try
						{
							removeValue( entry.getKey() );
						}
						catch( Exception e )
						{
							stm.logError( e.getMessage(), e );
						}
					}
					else
					{
						DataType<?> data = entry.getValue();
						putValue( entry.getKey(), data );
					}
				}
			}
		} );
	}
}
	
