package org.juxtapose.streamline.swt.datatable;

import static org.juxtapose.streamline.swt.datatable.ViewDataObjectState.*;
import static org.juxtapose.streamline.swt.spl.ClientViewMethods.createEntryKey;
import static org.juxtapose.streamline.tools.DataConstants.FIELD_KEYS;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Vector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.swt.dataeditor.GenericEditor;
import org.juxtapose.streamline.tools.STMUtil;
import org.juxtapose.streamline.util.BucketMap;
import org.juxtapose.streamline.util.PersistentArrayList;

import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.PersistentHashMap;

/**
 * @author Pontus Jörgne
 * 3 jun 2013
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class ViewDataObject implements IViewDataObjectContainer
{
	private ViewDataObjectState state;
	
	private final IPersistentMap<String, Object> metaData;
	private IPersistentMap<String, Object> data;
	
	private final String service;
	private final String type;
	
	private PersistentArrayList<Object> keyList;
	
	private ISTMEntryKey entryKey;
	
	private HashSet<String> updatedKeys = new HashSet<String>();
	
	private final IViewDataObjectContainer parent;
	
	BucketMap< String, ViewDataObject> viewObjects = new BucketMap< String, ViewDataObject>();
	
	/**
	 * @param inService
	 * @param inType
	 * @param inData
	 * @param inMetaData
	 * @param inParent
	 */
	public ViewDataObject( String inService, String inType, IPersistentMap<String, Object> inData, IPersistentMap<String, Object> inMetaData, IViewDataObjectContainer inParent )
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
	
	/**
	 * @param inService
	 * @param inType
	 * @param inData
	 * @param inMetaData
	 * @param inEntryKey
	 * @param inParent
	 */
	public ViewDataObject( String inService, String inType, IPersistentMap<String, Object> inData, IPersistentMap<String, Object> inMetaData, ISTMEntryKey inEntryKey, IViewDataObjectContainer inParent )
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
	
	/**
	 * @param inData
	 * @param inKey
	 */
	public void updateData( IPersistentMap<String, Object> inData, String inKey )
	{
		if( entryKey == null )
		{
			ISTMEntryKey newKey = createEntryKey( service, type, keyList, inData );
			if( newKey != null )
			{
				if( !parent.validateKey( type, newKey ) )
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
		
		if( parent != null )
		{
			if( entryKey != null )
			{
				String keyVal = entryKey.getSymbolicName();
				parent.updateChild( inData, type, keyVal);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.swt.datatable.IViewDataObjectContainer#updateChild(com.trifork.clj_ds.IPersistentMap, java.lang.String)
	 */
	public void updateChild( IPersistentMap<String, Object> inData, String inType, String inKey )
	{
		Object val = data.valAt( inType );
		
		IPersistentMap<String, Object> submap = (PersistentHashMap<String, Object>)val;
		
		if( val == null )
			submap = PersistentHashMap.EMPTY;
		
		submap = submap.assoc( inKey, inData );
		data = data.assoc( inType, submap );
		
		updateData( data, inType );
		
		parent.updateViewObject( this );
	}
	
	/**
	 * @return
	 */
	public IPersistentMap<String, Object> getData()
	{
		return data;
	}
	
	/**
	 * @return
	 */
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
	
	/**
	 * @param inData
	 */
	public void setData( IPersistentMap<String, Object> inData )
	{
		data = inData;
		entryKey = createEntryKey( service, type, keyList, inData );
		
		updatedKeys.clear();
		state = MIRROR;
		
		for( String key : viewObjects.keySet() )
		{
			Object subObj = inData.valAt( key );
			
			if( subObj instanceof IPersistentMap )
			{
				viewObjects.clear();
				
				IPersistentMap<String, Object> subMap = (IPersistentMap)subObj;
				
				Iterator<Entry<String, Object>> iter = subMap.iterator();
				
				while( iter.hasNext() )
				{
					Entry<String, Object> entry = iter.next();
					ISTMEntryKey entryKey = STMUtil.createEntryKey( service, key, entry.getKey() );
					
					ViewDataObject subObject = new ViewDataObject( service, key, (IPersistentMap<String, Object>)entry.getValue(), (IPersistentMap<String, Object>)metaData.valAt( key ), entryKey, this );
					viewObjects.put( key, subObject );
				}
			}
		}
			
		
	}
	
	public ISTMEntryKey getKey()
	{
		return entryKey;
	}
	
	public ViewDataObjectState getState()
	{
		return state;
	}
	
	public void setDeleted()
	{
		state = DELETED;
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

	@Override
	public ViewDataObject addEntry( String inKey)
	{
		Object obj = metaData.valAt( inKey );
		
		if( obj instanceof IPersistentMap )
		{
			ViewDataObject viewObject = new ViewDataObject( null, inKey, PersistentHashMap.EMPTY, (IPersistentMap<String, Object>)obj, this );
			viewObjects.put( inKey, viewObject );
			state = state == MIRROR ? UPDATED : state;
			return viewObject;
		}
		
		return null;
	}

	@Override
	public Shell getShell()
	{
		return parent.getShell();
	}

	@Override
	public boolean validateKey( String inKey, ISTMEntryKey inEntryKey )
	{
		for( ViewDataObject viewObj : viewObjects.get( inKey ) )
		{
			if( viewObj.getKey() != null && viewObj.getKey().equals( inEntryKey ))
				return false;	
		}

		return true;
	}
	
	public Vector<ViewDataObject> getViewDataObjects()
	{
		return viewObjects.values();
	}

	@Override
	public void updateViewObject( ViewDataObject inObject ) 
	{
		
	}
}