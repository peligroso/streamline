package org.juxtapose.fxtradingclient;

import static org.juxtapose.fxtradingclient.ViewDataObjectState.*;
import static org.juxtapose.fxtradingclient.tools.ClientViewMethods.createEntryKey;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeArrayList;

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
	
	IPersistentMap<String, DataType<?>> metaData;
	IPersistentMap<String, DataType<?>> data;
	
	String service;
	String type;
	
	PersistentArrayList<DataType<?>> keyList;
	
	ISTMEntryKey entryKey;
	
	HashSet<String> updatedKeys = new HashSet<String>();
	
	public ViewDataObject( String inService, String inType, IPersistentMap<String, DataType<?>> inData, IPersistentMap<String, DataType<?>> inMetaData )
	{
		data = inData;
		metaData = inMetaData;
		
		service = inService;
		type = inType;
		
		DataTypeArrayList listType = (DataTypeArrayList)inMetaData.valAt( DataConstants.FIELD_KEYS );
		if( listType != null )
			keyList = (PersistentArrayList<DataType<?>>) listType.get();
		
		entryKey = createEntryKey( service, type, keyList, inData );
		
		state = CREATED;
		updatedKeys.clear();
	}
	
	public ViewDataObject( String inService, String inType, IPersistentMap<String, DataType<?>> inData, IPersistentMap<String, DataType<?>> inMetaData, ISTMEntryKey inEntryKey )
	{
		data = inData;
		metaData = inMetaData;
		
		service = inService;
		type = inType;
		
		DataTypeArrayList listType = (DataTypeArrayList)inMetaData.valAt( DataConstants.FIELD_KEYS );
		if( listType != null )
			keyList = (PersistentArrayList<DataType<?>>) listType.get();
		
		entryKey = inEntryKey;
		
		state = MIRROR;
		updatedKeys.clear();
	}
	
	public void updateData( IPersistentMap<String, DataType<?>> inData, String inKey )
	{
		data = inData;
		entryKey = createEntryKey( service, type, keyList, inData );
		
		state = state == MIRROR ? UPDATED : state;
		updatedKeys.add( inKey );
	}
	
	public IPersistentMap<String, DataType<?>> getData()
	{
		return data;
	}
	
	public String validate()
	{
		Iterator<Entry<String, DataType<?>>> iter = metaData.iterator();
		while( iter.hasNext() )
		{
			Entry<String, DataType<?>> entry = iter.next();
			
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
	
	public void setData( IPersistentMap<String, DataType<?>> inData )
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
	
	public IPersistentMap<String, DataType<?>> getUpdateData()
	{
		IPersistentMap<String, DataType<?>> updateData = PersistentHashMap.EMPTY;
		
		for( String key : updatedKeys )
		{
			DataType<?> dt = data.valAt( key );
			if( dt != null )
				updateData = updateData.assoc( key, dt );
		}
		
		return updateData;
	}
	
}
