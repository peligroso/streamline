package org.juxtapose.streamline.swt.datatable;

import static org.juxtapose.streamline.swt.datatable.ViewDataObjectState.CREATED;
import static org.juxtapose.streamline.swt.datatable.ViewDataObjectState.MIRROR;
import static org.juxtapose.streamline.swt.datatable.ViewDataObjectState.UPDATED;
import static org.juxtapose.streamline.swt.spl.ClientViewMethods.createEntryKey;
import static org.juxtapose.streamline.tools.DataConstants.FIELD_KEYS;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.MessageDialog;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.swt.dataeditor.GenericEditor;
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
	private ViewDataObjectState state;
	
	private final IPersistentMap<String, Object> metaData;
	private IPersistentMap<String, Object> data;
	
	private final String service;
	private final String type;
	
	private PersistentArrayList<Object> keyList;
	
	private ISTMEntryKey entryKey;
	
	private HashSet<String> updatedKeys = new HashSet<String>();
	
	private final DataViewer parent;
	
	public ViewDataObject( String inService, String inType, IPersistentMap<String, Object> inData, IPersistentMap<String, Object> inMetaData, DataViewer inParent )
	{
		data = inData;
		metaData = inMetaData;
		
		service = inService;
		type = inType;
		
		PersistentArrayList<Object> listType = (PersistentArrayList<Object>)inMetaData.valAt( FIELD_KEYS );
		if( listType != null )
			keyList = (PersistentArrayList<Object>) listType;
		
		entryKey = createEntryKey( service, type, keyList, inData );
		
		state = CREATED;
		updatedKeys.clear();
		
		parent = inParent;
	}
	
	public ViewDataObject( String inService, String inType, IPersistentMap<String, Object> inData, IPersistentMap<String, Object> inMetaData, ISTMEntryKey inEntryKey, DataViewer inParent )
	{
		data = inData;
		metaData = inMetaData;
		
		service = inService;
		type = inType;
		
		PersistentArrayList<Object> listType = (PersistentArrayList<Object>)inMetaData.valAt( FIELD_KEYS );
		if( listType != null )
			keyList = (PersistentArrayList<Object>) listType;
		
		entryKey = inEntryKey;
		
		state = MIRROR;
		updatedKeys.clear();
		
		parent = inParent;
	}
	
	public void updateData( IPersistentMap<String, Object> inData, String inKey )
	{
		if( entryKey == null )
		{
			ISTMEntryKey newKey = createEntryKey( service, type, keyList, inData );
			if( newKey != null )
			{
				if( !parent.validateKey( newKey ) )
				{
					MessageDialog.openError( parent.getShell(), "Illegal key", "En entry with this key alrweady exists" );
					return;
				}
			}
			entryKey = newKey;
		}
		
		data = inData;
		
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
			
			if( !FIELD_KEYS.equals( entry.getKey() ))
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
