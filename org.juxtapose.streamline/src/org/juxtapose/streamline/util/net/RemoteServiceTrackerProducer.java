package org.juxtapose.streamline.util.net;

import java.util.Iterator;
import java.util.Map.Entry;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.util.Status;

import com.trifork.clj_ds.IPersistentMap;

/**
 * @author Pontus J�rgne
 * 2 maj 2013
 * Copyright (c) Pontus J�rgne. All rights reserved
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
	public void updateData( ISTMEntryKey inKey, final IPersistentMap<String, Object> inData, boolean inFirstUpdate )
	{
		Iterator<Entry<String, Object>> iterator = inData.iterator();
		
		while( iterator.hasNext() )
		{
			Entry<String, Object> entry = iterator.next();
			
			if( entry.getValue() instanceof String )
			{
				String strStatus = (String)entry.getValue();
				Status serviceStatus = Status.valueOf( strStatus );
			
				tracker.statusUpdated( entry.getKey(), serviceStatus );
			}
		}  
	}

}
