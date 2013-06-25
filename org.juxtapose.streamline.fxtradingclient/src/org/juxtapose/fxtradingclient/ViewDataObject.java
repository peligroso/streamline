package org.juxtapose.fxtradingclient;

import java.sql.DatabaseMetaData;
import java.util.Iterator;
import java.util.Map.Entry;

import org.juxtapose.fxtradingclient.tools.ClientViewMethods;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.tools.STMUtil;
import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeArrayList;

import com.trifork.clj_ds.IPersistentMap;

/**
 * @author Pontus Jörgne
 * 3 jun 2013
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class ViewDataObject 
{
	static int idInc;
	
	int id;
	IPersistentMap<String, DataType<?>> metaData;
	IPersistentMap<String, DataType<?>> data;
	
	String service;
	String type;
	
	PersistentArrayList<DataType<?>> keyList;
	
	ISTMEntryKey entryKey;
	
	public ViewDataObject( String inService, String inType, IPersistentMap<String, DataType<?>> inData, IPersistentMap<String, DataType<?>> inMetaData )
	{
		id = idInc++;
		data = inData;
		metaData = inMetaData;
		
		service = inService;
		type = inType;
		
		DataTypeArrayList listType = (DataTypeArrayList)inMetaData.valAt( DataConstants.FIELD_KEYS );
		if( listType != null )
			keyList = (PersistentArrayList<DataType<?>>) listType.get();
		
		entryKey = ClientViewMethods.createEntryKey( service, type, keyList, inData );
	}
	
	public void updateData( IPersistentMap<String, DataType<?>> inData )
	{
		data = inData;
		
		entryKey = ClientViewMethods.createEntryKey( service, type, keyList, inData );
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
		
		entryKey = ClientViewMethods.createEntryKey( service, type, keyList, inData );
	}
	
	public ISTMEntryKey getKey()
	{
		return entryKey;
	}
	
}
