package org.juxtapose.streamline.stm.osgi;

import static org.juxtapose.streamline.tools.STMMessageConstants.REQUEST_NOT_SUPPORTED;

import org.juxtapose.streamline.producer.ISTMEntryProducerService;
import org.juxtapose.streamline.producer.executor.Executable;
import org.juxtapose.streamline.producer.executor.IExecutor;
import org.juxtapose.streamline.producer.executor.StickyRunnable;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.tools.KeyConstants;
import org.juxtapose.streamline.util.ISTMEntrySubscriber;
import org.juxtapose.streamline.util.ISTMRequestor;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.producerservices.DataInitializer;
import org.juxtapose.streamline.util.producerservices.IDataInitializerListener;
import org.osgi.service.component.ComponentContext;

import com.trifork.clj_ds.IPersistentMap;

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
			stm.registerProducer( this, Status.ON_REQUEST );
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
		}, IExecutor.LOW );
	}
	
	public DataInitializer createDataInitializer( )
	{
		return null;
	}
	
	public void dataInitialized()
	{
		stm.updateProducerStatus( this, Status.OK );
		stm.subscribeToData( KeyConstants.PRODUCER_SERVICE_KEY, this);
	}

	@Override
	public abstract String getServiceId();
	
	public int getPriority()
	{
		return IExecutor.LOW;
	}
	
	public void request( int inTag, long inType, ISTMRequestor inRequestor, String inVariable, IPersistentMap<String, DataType<?>> inData  )
	{
		inRequestor.reply( inTag, DataConstants.RESPONSE_TYPE_ERROR, REQUEST_NOT_SUPPORTED, null );
	}
}
