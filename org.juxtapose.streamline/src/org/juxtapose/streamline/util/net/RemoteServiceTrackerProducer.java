package org.juxtapose.streamline.util.net;

import java.util.Iterator;
import java.util.Map.Entry;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeString;

import com.trifork.clj_ds.IPersistentMap;

/**
 * @author Pontus Jörgne
 * 2 maj 2013
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class RemoteServiceTrackerProducer extends RemoteProxyEntryProducer {

	RemoteServiceTracker tracker;
	
	/**
	 * @param inSTM
	 * @param inKey
	 * @param inConnector
	 * @param inTracker
	 */
	public RemoteServiceTrackerProducer( ISTM inSTM, ISTMEntryKey inKey, ClientConnectorHandler inConnector, RemoteServiceTracker inTracker ) 
	{
		super( inSTM, inKey, inConnector );
		
		tracker = inTracker;
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.util.net.RemoteProxyEntryProducer#updateData(org.juxtapose.streamline.producer.ISTMEntryKey, com.trifork.clj_ds.IPersistentMap, boolean)
	 */
	public void updateData( ISTMEntryKey inKey, final IPersistentMap<String, DataType<?>> inData, boolean inFirstUpdate )
	{
		Iterator<Entry<String, DataType<?>>> iterator = inData.iterator();
		
		while( iterator.hasNext() )
		{
			Entry<String, DataType<?>> entry = iterator.next();
			
			if( entry.getValue() instanceof DataTypeString )
			{
				String strStatus = (String)entry.getValue().get();
				Status serviceStatus = Status.valueOf( strStatus );
			
				tracker.statusUpdated( entry.getKey(), serviceStatus );
			}
		}  
	}

}
