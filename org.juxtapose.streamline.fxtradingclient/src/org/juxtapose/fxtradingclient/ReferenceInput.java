package org.juxtapose.fxtradingclient;

import java.util.ArrayList;

import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryListener;

public class ReferenceInput implements InputContainer, ISTMEntryListener
{
	ContainerSubscriber subscriber;
	
	ArrayList<String> values = new ArrayList<String>();
	
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
	public void STMEntryUpdated( ISTMEntry inEntry, boolean inFullUpdate )
	{
		for( String updatedValue : inEntry.getDeltaSet() )
		{
			values.add( updatedValue );
			System.out.println("Got me a reference value "+updatedValue );
		}
	}
}
