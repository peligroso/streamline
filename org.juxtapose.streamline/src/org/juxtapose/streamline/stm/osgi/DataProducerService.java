package org.juxtapose.streamline.stm.osgi;

import org.juxtapose.streamline.producer.ISTMEntryProducerService;
import org.juxtapose.streamline.producer.executor.Executable;
import org.juxtapose.streamline.producer.executor.IExecutor;
import org.juxtapose.streamline.producer.executor.StickyRunnable;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.util.ISTMEntrySubscriber;
import org.juxtapose.streamline.util.KeyConstants;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.producerservices.DataInitializer;
import org.juxtapose.streamline.util.producerservices.IDataInitializerListener;
import org.osgi.service.component.ComponentContext;

/**
 * @author Pontus Jörgne
 * 3 okt 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public abstract class DataProducerService implements ISTMEntryProducerService, ISTMEntrySubscriber, IDataInitializerListener
{
	protected ISTM stm;
	
	protected DataInitializer initializer;
	
	public void activate( ComponentContext inContext )
	{
		init();
	}
	
	public void bindSTM( ISTM inSTM )
	{
		stm = inSTM;
	}
	
	private void doInit()
	{
		initializer = createDataInitializer();
		
		if( initializer != null )
		{
			stm.registerProducer( this, Status.INITIALIZING );
			initializer.init();
		}
		else
		{
			stm.registerProducer( this, Status.OK );
			stm.subscribeToData( KeyConstants.PRODUCER_SERVICE_KEY, this);
		}
	}
	
	protected void init()
	{
		stm.execute( new Executable(){

			@Override
			public void run()
			{
				doInit();
			}
		}, IExecutor.HIGH );
	}
	
	public DataInitializer createDataInitializer( )
	{
		return null;
	}
	
	public void dataInitialized()
	{
		stm.registerProducer( this, Status.OK );
		stm.subscribeToData( KeyConstants.PRODUCER_SERVICE_KEY, this);
	}

	@Override
	public abstract String getServiceId();
}
