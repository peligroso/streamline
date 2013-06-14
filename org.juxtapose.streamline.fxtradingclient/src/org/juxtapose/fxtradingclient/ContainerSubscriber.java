package org.juxtapose.fxtradingclient;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.STMEntrySubscriber;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeLazyRef;
import org.juxtapose.streamline.util.data.DataTypeNull;

import com.trifork.clj_ds.IPersistentMap;

public class ContainerSubscriber extends STMEntrySubscriber
{
	HashMap<String, STMEntrySubscriber> entryToSubscribers = new HashMap<String, STMEntrySubscriber>();

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
				public void updateData( ISTMEntryKey inKey, ISTMEntry inData, boolean inFirstUpdate )
				{
					System.out.println("got reference data for "+inData.getDataMap());
				}
			};
			
			entryToSubscribers.put( lRef.get().toString(), subscriber );
			subscriber.initialize( stm, lRef.get() );
		}
	}
		
	private void doFirstUpdate( ISTMEntry inData )
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
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData, boolean inFirstUpdate )
	{	
		if( inFirstUpdate )
		{
			doFirstUpdate( inData );
		}
		else
		{
			doPartialUpdate( inData);
		}
	}

}
