package org.juxtapose.fxtradingclient;

import java.awt.event.ContainerListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.tools.STMUtil;
import org.juxtapose.streamline.util.ISTMContainerListener;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryListener;
import org.juxtapose.streamline.util.STMEntrySubscriber;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeLazyRef;
import org.juxtapose.streamline.util.data.DataTypeNull;

import com.trifork.clj_ds.IPersistentMap;

public class ContainerSubscriber extends STMEntrySubscriber implements ISTMEntryListener
{
	HashMap<String, STMEntrySubscriber> entryToSubscribers = new HashMap<String, STMEntrySubscriber>();
	
	ArrayList<ISTMContainerListener> containerListeners = new ArrayList<ISTMContainerListener>();

	@Override
	public void queryNotAvailible( Object inTag )
	{
		// TODO Auto-generated method stub
		
	}
	
	private void fieldUpdated( DataType<?> inValue )
	{
		if( inValue instanceof DataTypeNull )
		{
			System.out.println("Should delete data ");
		}
		else if( inValue instanceof DataTypeLazyRef )
		{
			DataTypeLazyRef lRef = (DataTypeLazyRef)inValue;
			if( entryToSubscribers.get( lRef.get().toString() ) != null )
				return;
			
			STMEntrySubscriber subscriber = new STMEntrySubscriber()
			{
				@Override
				public void queryNotAvailible( Object inTag )
				{
					
				}

				@Override
				public void updateData( ISTMEntryKey inKey, ISTMEntry inData, boolean inFullUpdate )
				{
					super.updateData( inKey, inData, inFullUpdate );
					System.out.println("got reference data for "+inData.getDataMap());
					updateListeners( inKey, inData, inFullUpdate );
				}
			};
			
			entryToSubscribers.put( lRef.get().toString(), subscriber );
			subscriber.addListener( this );
			subscriber.initialize( stm, lRef.get() );
		}
	}
		
	private void doFullUpdate( ISTMEntry inData )
	{
		Iterator<Entry<String, DataType<?>>> iter = inData.getDataMap().iterator();
		
		while( iter.hasNext() )
		{
			Entry<String, DataType<?>> entry = iter.next();
			fieldUpdated( entry.getValue() );
		}
	}
	
	private void doPartialUpdate( ISTMEntry inData )
	{
		IPersistentMap<String, DataType<?>> data = inData.getDataMap();
		Set<String> deltaSet = inData.getDeltaSet();
		
		for( String s : deltaSet )
		{
			DataType<?> value = data.valAt( s );
			fieldUpdated( value );
		}
	}

	@Override
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData, boolean inFullUpdate )
	{	
		if( inFullUpdate )
		{
			doFullUpdate( inData );
		}
		else
		{
			doPartialUpdate( inData);
		}
		
	}
	
	public void addContainerListener( ISTMContainerListener inContainerListener )
	{
		assert !containerListeners.contains( inContainerListener ) : "Listener is already added to container";
		
		containerListeners.add( inContainerListener );
		
		for( STMEntrySubscriber subscriber : entryToSubscribers.values() )
		{
			ISTMEntry entry = subscriber.getLastUpdate();
			if( STMUtil.isStatusOk( entry ))
			{
				inContainerListener.onContainerRefAdded( subscriber.getEntryKey(), entry );
			}
		}
	}
	
	public void removeContainerListener( ISTMContainerListener inContainerListener )
	{
		assert containerListeners.contains( inContainerListener ) : "Listener is not attached to container";
		
		containerListeners.remove( inContainerListener );
	}

	@Override
	public void STMEntryUpdated( ISTMEntryKey inKey, ISTMEntry inEntry, boolean inFullUpdate )
	{
		if( STMUtil.isStatusUpdatedToOk( inEntry, inFullUpdate ))
			updateListeners( inKey,  inEntry );
	}
	
	/**
	 * @param inKey
	 * @param inEntry
	 */
	private void updateListeners( ISTMEntryKey inKey, ISTMEntry inEntry )
	{
		System.out.println("updatign reference listenrs");
		for( ISTMContainerListener listener : containerListeners )
		{
			listener.onContainerRefAdded( inKey, inEntry );
		}
	}
	
	

}
