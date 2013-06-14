package org.juxtapose.streamline.util;

import java.util.ArrayList;
import java.util.HashMap;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.executor.IExecutor;
import org.juxtapose.streamline.stm.ISTM;

/**
 * @author Pontus
 *
 */
public abstract class STMEntrySubscriber implements ISTMEntryRequestSubscriber
{
	protected ISTM stm;
	protected String service;
	protected HashMap<String, String> query;
	
	Short tag;
	
	protected ISTMEntryKey key;
	
	protected ArrayList<ISTMEntryListener> listeners = new ArrayList<ISTMEntryListener>();
	
	public void initialize(  ISTM inSTM, HashMap<String, String> inQuery, String inService )
	{
		tag = 1;
		stm = inSTM;
		query = inQuery;
		service = inService;
		stm.getDataKey( inService, this, tag, inQuery );
	}
	
	public void initialize(  ISTM inSTM, ISTMEntryKey inKey )
	{
		stm = inSTM;
		service = inKey.getService();
		key = inKey;
		stm.subscribeToData( key, this );
	}
	

	@Override
	public int getPriority()
	{
		return IExecutor.LOW;
	}

	@Override
	public void deliverKey( ISTMEntryKey inDataKey, Object inTag )
	{
		key = inDataKey;
		stm.subscribeToData( key, this );
	}
	
	public void addListener( ISTMEntryListener inListener )
	{
		listeners.add( inListener );
	}
	
	public void removeListener( ISTMEntryListener inListener )
	{
		listeners.remove( inListener );
	}

	public void updateListeners( ISTMEntry inEntry, boolean inFullUpdate )
	{
		for( ISTMEntryListener listener : listeners )
		{
			listener.STMEntryUpdated( inEntry, inFullUpdate );
		}
	}
}