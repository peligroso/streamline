package org.juxtapose.streamline.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.tools.STMUtil;
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
	
	private void fieldUpdated( Object inValue )
	{
		if( inValue instanceof DataTypeLazyRef )
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
		Iterator<Entry<String, Object>> iter = inData.getDataMap().iterator();
		
		while( iter.hasNext() )
		{
			Entry<String, Object> entry = iter.next();
			fieldUpdated( entry.getValue() );
		}
	}
	
	private void doPartialUpdate( ISTMEntry inData )
	{
		IPersistentMap<String, Object> data = inData.getDataMap();
		Set<String> deltaSet = inData.getDeltaSet();
		
		for( String s : deltaSet )
		{
			Object value = data.valAt( s );
			if( value == null )
			{
				STMEntrySubscriber subscriber = entryToSubscribers.remove( s );
				if( subscriber != null )
					subscriber.dispose();
				
				ISTMEntryKey entryKey = subscriber.getEntryKey();
				
				entryRemoved( entryKey );
			}
			else
			{
				fieldUpdated( value );
			}
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
		
		updateListeners( inKey, inData, inFullUpdate );
		
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
		for( ISTMContainerListener listener : containerListeners )
		{
			listener.onContainerRefAdded( inKey, inEntry );
		}
	}
	
	private void entryRemoved( ISTMEntryKey inKey )
	{
		for( ISTMContainerListener listener : containerListeners )
		{
			listener.onContainerRefRemoved( inKey );
		}
	}
	
	

}
