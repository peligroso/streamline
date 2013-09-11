package org.juxtapose.fxtradingclient;

import static org.juxtapose.fxtradingclient.ViewDataObjectState.CREATED;
import static org.juxtapose.fxtradingclient.ViewDataObjectState.MIRROR;
import static org.juxtapose.fxtradingclient.ViewDataObjectState.UPDATED;
import static org.juxtapose.fxtradingclient.tools.ClientViewMethods.createEntryKey;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.util.PersistentArrayList;

import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.PersistentHashMap;

/**
 * @author Pontus Jörgne
 * 3 jun 2013
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class ViewDataObject 
{
	ViewDataObjectState state;
	
	IPersistentMap<String, Object> metaData;
	IPersistentMap<String, Object> data;
	
	String service;
	String type;
	
	PersistentArrayList<Object> keyList;
	
	ISTMEntryKey entryKey;
	
	HashSet<String> updatedKeys = new HashSet<String>();
	
	public ViewDataObject( String inService, String inType, IPersistentMap<String, Object> inData, IPersistentMap<String, Object> inMetaData )
	{
		data = inData;
		metaData = inMetaData;
		
		service = inService;
		type = inType;
		
		PersistentArrayList<Object> listType = (PersistentArrayList<Object>)inMetaData.valAt( DataConstants.FIELD_KEYS );
		if( listType != null )
			keyList = (PersistentArrayList<Object>) listType;
		
		entryKey = createEntryKey( service, type, keyList, inData );
		
		state = CREATED;
		updatedKeys.clear();
	}
	
	public ViewDataObject( String inService, String inType, IPersistentMap<String, Object> inData, IPersistentMap<String, Object> inMetaData, ISTMEntryKey inEntryKey )
	{
		data = inData;
		metaData = inMetaData;
		
		service = inService;
		type = inType;
		
		PersistentArrayList<Object> listType = (PersistentArrayList<Object>)inMetaData.valAt( DataConstants.FIELD_KEYS );
		if( listType != null )
			keyList = (PersistentArrayList<Object>) listType;
		
		entryKey = inEntryKey;
		
		state = MIRROR;
		updatedKeys.clear();
	}
	
	public void updateData( IPersistentMap<String, Object> inData, String inKey )
	{
		data = inData;
		entryKey = createEntryKey( service, type, keyList, inData );
		
		state = state == MIRROR ? UPDATED : state;
		updatedKeys.add( inKey );
	}
	
	public IPersistentMap<String, Object> getData()
	{
		return data;
	}
	
	public String validate()
	{
		Iterator<Entry<String, Object>> iter = metaData.iterator();
		while( iter.hasNext() )
		{
			Entry<String, Object> entry = iter.next();
			
			if( !DataConstants.FIELD_KEYS.equals( entry.getKey() ))
			{
				if( data.valAt( entry.getKey() ) == null )
				{
					return "Required field "+entry.getKey()+" is missing ";
				}
			}
		}
		
		if( entryKey == null )
			return "Entry key could not be created";
		return null;
	}
	
	public void setData( IPersistentMap<String, Object> inData )
	{
		data = inData;
		entryKey = createEntryKey( service, type, keyList, inData );
		
		updatedKeys.clear();
		state = MIRROR;
	}
	
	public ISTMEntryKey getKey()
	{
		return entryKey;
	}
	
	public ViewDataObjectState getState()
	{
		return state;
	}
	
	public IPersistentMap<String, Object> getUpdateData()
	{
		IPersistentMap<String, Object> updateData = PersistentHashMap.EMPTY;
		
		for( String key : updatedKeys )
		{
			Object dt = data.valAt( key );
			if( dt != null )
				updateData = updateData.assoc( key, dt );
		}
		
		return updateData;
	}
	
}
