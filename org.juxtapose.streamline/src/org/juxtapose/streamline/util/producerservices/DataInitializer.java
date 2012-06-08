package org.juxtapose.streamline.util.producerservices;

import java.util.HashSet;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.executor.IExecutor;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.util.ISTMEntrySubscriber;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.Status;

/**
 * @author Pontus Jörgne
 * Jan 7, 2012
 * Copyright (c) Pontus Jörgne. All rights reserved
 * 
 * Class to initiate subscription and hence creation of predefined data, effectively making them cached and available throughout the life of the producer.
 * Running during startup in single thread context and does not need sync.
 * DataInitializer should only be used to initialize services.
 */
public class DataInitializer implements ISTMEntrySubscriber
{
	private final HashSet<ISTMEntryKey> keys = new HashSet<ISTMEntryKey>();
	private final ISTM stm;
	private volatile boolean allOK = false;
	private final IDataInitializerListener listener;
	private final int priority;
	
	/**
	 * @param inSTM
	 * @param inListener
	 * @param inPriority
	 */
	public DataInitializer( ISTM inSTM, IDataInitializerListener inListener, int inPriority )
	{
		stm = inSTM;
		listener = inListener;
		priority = inPriority;
	}
	
	public DataInitializer( ISTM inSTM, IDataInitializerListener inListener )
	{
		this(inSTM, inListener, IExecutor.LOW );
	}
	
	/**
	 * @param inSTM
	 * @param inListener
	 * @param inPriority
	 * @param inKeys
	 */
	public DataInitializer( ISTM inSTM, IDataInitializerListener inListener, int inPriority, ISTMEntryKey... inKeys )
	{
		stm = inSTM;
		listener = inListener;
		priority = inPriority;
		
		for( ISTMEntryKey key : inKeys )
		{
			addDataKey( key );
		}
	}
	
	public DataInitializer( ISTM inSTM, IDataInitializerListener inListener, ISTMEntryKey... inKeys )
	{
		this(inSTM, inListener, IExecutor.LOW, inKeys );
	}
	
	/**
	 * @param inKey
	 */
	public void addDataKey( ISTMEntryKey inKey )
	{
		keys.add( inKey );
	}
	
	public void init()
	{
		for( ISTMEntryKey key : keys )
		{
			stm.subscribeToData( key, this );
		}
	}
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.util.ISTMEntrySubscriber#updateData(org.juxtapose.streamline.producer.ISTMEntryKey, org.juxtapose.streamline.util.ISTMEntry, boolean)
	 */
	@Override
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData, boolean inFirstUpdate )
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

	@Override
	public int getPriority() 
	{
		return priority;
	}
}
