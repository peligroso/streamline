package org.juxtapose.streamline.util.producerservices;

import java.util.HashSet;

import org.juxtapose.streamline.producer.IDataKey;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.util.IDataSubscriber;
import org.juxtapose.streamline.util.IPublishedData;
import org.juxtapose.streamline.util.Status;

/**
 * @author Pontus Jörgne
 * Jan 7, 2012
 * Copyright (c) Pontus Jörgne. All rights reserved
 * 
 * Class to initiate subscription and hence creation of predefined data, effectively making them available throughout the life of the producer.
 * Running during startup in single thread context and does not need sync.
 * DataInitializer should only be used to initialize services.
 */
public class DataInitializer implements IDataSubscriber
{
	private final HashSet<IDataKey> keys = new HashSet<IDataKey>();
	private final ISTM stm;
	private volatile boolean allOK = false;
	private final IDataInitializerListener listener;
	
	public DataInitializer( ISTM inSTM, IDataInitializerListener inListener )
	{
		stm = inSTM;
		listener = inListener;
	}
	
	public DataInitializer( ISTM inSTM, IDataInitializerListener inListener, IDataKey... inKeys )
	{
		stm = inSTM;
		listener = inListener;
		
		for( IDataKey key : inKeys )
		{
			addDataKey( key );
		}
	}
	
	public void addDataKey( IDataKey inKey )
	{
		keys.add( inKey );
	}
	
	public void init()
	{
		for( IDataKey key : keys )
		{
			stm.subscribeToData( key, this );
		}
	}
	@Override
	public void updateData( IDataKey inKey, IPublishedData inData, boolean inFirstUpdate )
	{
		if( allOK )
			return;
		
		synchronized( keys )
		{
			if( inData.getStatus() == Status.OK )
			{
				keys.remove( inKey );
			}
			if( keys.isEmpty() )
			{
				allOK = true;
			}
		}
		
		if( allOK )
		{
			listener.dataInitialized();
		}
	}
}
