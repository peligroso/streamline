package org.juxtapose.streamline.swt.datatable;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.util.ContainerSubscriber;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryListener;
import org.juxtapose.streamline.util.data.DataTypeLazyRef;
import org.juxtapose.streamline.util.data.DataTypeNull;

public class ReferenceInput implements InputContainer, ISTMEntryListener
{
	ContainerSubscriber subscriber;
	
	ArrayList<Map.Entry<String, DataTypeLazyRef>> values = new ArrayList<Map.Entry<String, DataTypeLazyRef>>();
	
	ArrayList<IInputListener> inputListeners = new ArrayList<IInputListener>(); 
	
	ReferenceInput( ContainerSubscriber inSubscriber )
	{
		subscriber = inSubscriber;
		subscriber.addListener( this );
	}
	
	
	@Override
	public String[] getInputObjects()
	{
		String[] strings = new String[values.size()];
		
		for( int i = 0; i < strings.length; i++ )
		{
			strings[i] = values.get( i ).getKey().toString();
		}
		return strings;
	}


	@Override
	public void STMEntryUpdated( ISTMEntryKey inKey, ISTMEntry inEntry, boolean inFullUpdate )
	{
		for( String updatedValue : inEntry.getDeltaSet() )
		{
			Object value = inEntry.getValue( updatedValue );
			if( value instanceof DataTypeLazyRef )
			{
				DataTypeLazyRef ref = (DataTypeLazyRef)value;
				String keyString = ref.get().getSymbolicName();
				
				Map.Entry<String, DataTypeLazyRef> entry = new AbstractMap.SimpleEntry<String, DataTypeLazyRef>(keyString, ref);
				
				if( !values.contains( entry ) )
				{
					values.add( entry );
					updateListeners();
				}
			}
			if( value == null || value instanceof DataTypeNull )
			{
				Iterator<Map.Entry<String, DataTypeLazyRef>> iter = values.iterator();
				
				while( iter.hasNext() )
				{
					Map.Entry<String, DataTypeLazyRef> val = iter.next();
					if( val.getValue().get().getKey().equals( updatedValue ) )
					{
						iter.remove();
						updateListeners();
						break;
					}
				}
			}
		}
	}
	
	public void addInputListener( IInputListener inInputListener )
	{
		assert !inputListeners.contains( inInputListener ) : "Listener is already added to input";
		
		inputListeners.add( inInputListener );
	}
	
	public void removeContainerListener( IInputListener inputListener )
	{
		assert inputListeners.contains( inputListener ) : "Listener is not attached to input";
		
		inputListeners.remove( inputListener );
	}
	
	public void updateListeners()
	{
		for( IInputListener listener : inputListeners )
		{
			listener.inputChanged();
		}
	}
	
	public DataTypeLazyRef getReference( String inKey )
	{
		for( Map.Entry<String, DataTypeLazyRef> entry : values.toArray( new Map.Entry[]{} ) )
		{
			if( entry.getKey().equals( inKey ) )
				return entry.getValue();
		}
		
		return null;
	}
}
