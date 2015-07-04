package org.juxtapose.streamline.gwt.stm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * @author Pontus Jörgne
 * 20 apr 2015
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class JSSTMEntry {

	private HashMap<String, Object> dataMap;
	private Set<String> deltaSet; 
	
	final ArrayList<IJSSTMEntrySubscriber> subscribers;
	
	final IJSSTMEntryProducer producer;
	
	long sequenceID;
	
	boolean completeVersion;
	
	/**
	 * @param inData
	 * @param inChanges
	 * @param inSubscribers
	 * @param inProducer
	 * @param inSequenceID
	 * @param inCompleteUpdate
	 */
	protected JSSTMEntry( HashMap<String, Object> inData, Set<String> inChanges, ArrayList<IJSSTMEntrySubscriber> inSubscribers, IJSSTMEntryProducer inProducer, long inSequenceID, boolean inCompleteUpdate ) 
	{
		dataMap = inData;
		deltaSet = Collections.unmodifiableSet( inChanges );
		subscribers = inSubscribers;
		producer = inProducer;
		sequenceID = inSequenceID;
		completeVersion = inCompleteUpdate;
	}
	
	/**
	 * @param inKey
	 * @param inValue
	 * @throws Exception
	 */
	public void putDataValue( String inKey, Object inValue )throws Exception
	{	
		if( inValue instanceof JSDataTypeNull )
			dataMap.remove( inKey );
		else
			dataMap.put( inKey, inValue );
		
		deltaSet.add( inKey );
	}
	
	public void setUpdatedData( HashMap<String, Object> inDataMap, Set<String> inDelta, boolean inIsComplete )
	{
		dataMap = inDataMap;
		deltaSet = inDelta;
		completeVersion = inIsComplete;		
	}
	
	public void updateSubscribers( String inKey, boolean inFullUpdate )
	{
		for( int i = 0; i < subscribers.size(); i++ )
		{
			IJSSTMEntrySubscriber subscriber = subscribers.get( i );
			subscriber.updateData( inKey, this, inFullUpdate );
		}
	}
	
	/**
	 * @return
	 */
	public String getStatus()
	{
		return (String)dataMap.get( JSSTMConstants.FIELD_STATUS );
	}
	
	/**
	 * @param inKey
	 * @return
	 */
	public Object getUpdatedValue( String inKey ) 
	{
		if( deltaSet.contains( inKey ))
			return dataMap.get( inKey );
		
		return null;
	}
	
	/**
	 * @param inSubscriber
	 */
	public void addSubscriber( IJSSTMEntrySubscriber inSubscriber )
	{
		subscribers.add( inSubscriber );
	}
	
	public void removeSubscriber( IJSSTMEntrySubscriber inSubscriber )
	{
		subscribers.remove( inSubscriber );
	}
	
	public boolean hasSubscribers( )
	{
		return !subscribers.isEmpty();
	}
	
	public IJSSTMEntryProducer getProducer()
	{
		return producer;
	}
	
	public HashMap<String, Object> getDataMap()
	{
		return dataMap;
	}
	
	public Set<String> getDeltaSet()
	{
		return deltaSet;
	}
	
	public boolean isCompleteVersion()
	{
		return completeVersion;
	}
}
