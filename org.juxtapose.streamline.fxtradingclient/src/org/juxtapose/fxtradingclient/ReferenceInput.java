package org.juxtapose.fxtradingclient;

import java.util.ArrayList;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.util.IInputListener;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryListener;
import org.juxtapose.streamline.util.data.DataTypeLazyRef;

public class ReferenceInput implements InputContainer, ISTMEntryListener
{
	ContainerSubscriber subscriber;
	
	ArrayList<String> values = new ArrayList<String>();
	
	ArrayList<IInputListener> inputListeners = new ArrayList<IInputListener>(); 
	
	ReferenceInput( ContainerSubscriber inSubscriber )
	{
		subscriber = inSubscriber;
		subscriber.addListener( this );
	}
	
	
	@Override
	public String[] getInputObjects()
	{
		return values.toArray( new String[]{} );
	}


	@Override
	public void STMEntryUpdated( ISTMEntryKey inKey, ISTMEntry inEntry, boolean inFullUpdate )
	{
		for( String updatedValue : inEntry.getDeltaSet() )
		{
			Object value = inEntry.getValue( updatedValue );
			if( value instanceof DataTypeLazyRef )
			{
				ISTMEntryKey key = ((DataTypeLazyRef)value).get();
				String keyVal = key.getSingleValue();
				
				if( !values.contains( keyVal ) )
				{
					values.add( keyVal );
					System.out.println("Got me a reference value "+keyVal );
					updateListeners();
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
}
