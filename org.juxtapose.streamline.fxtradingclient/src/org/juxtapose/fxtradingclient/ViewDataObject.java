package org.juxtapose.fxtradingclient;

import org.juxtapose.streamline.util.data.DataType;

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
	IPersistentMap<String, DataType<?>> data;
	
	public ViewDataObject( IPersistentMap<String, DataType<?>> inData )
	{
		id = idInc++;
		data = inData;
	}
	
	public void updateData( IPersistentMap<String, DataType<?>> inData )
	{
		data = inData;
	}
	
	public IPersistentMap<String, DataType<?>> getData()
	{
		return data;
	}
	
}
