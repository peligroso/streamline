package org.juxtapose.streamline.swt.datatable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.swt.dataeditor.GenericEditor;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.tools.STMUtil;
import org.juxtapose.streamline.util.ContainerSubscriber;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.STMEntrySubscriber;

import com.trifork.clj_ds.IPersistentMap;

/**
 * @author Pontus Jörgne
 * 15 jun 2013
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class MetaDataControl extends STMEntrySubscriber
{
	boolean metaDataInitiated = false;
	
	final GenericEditor editor;
	
	HashMap<String, ContainerSubscriber> typeToContainer = new HashMap<String, ContainerSubscriber>();
	HashMap<String, InputContainer> typeToInput = new HashMap<String, InputContainer>();
	
	String serviceKey;
	
	/**
	 * @param inView
	 */
	public MetaDataControl( GenericEditor inView, String inServiceKey )
	{
		editor = inView;
		serviceKey = inServiceKey;
	}
	
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.util.STMEntrySubscriber#updateData(org.juxtapose.streamline.producer.ISTMEntryKey, org.juxtapose.streamline.util.ISTMEntry, boolean)
	 */
	@Override
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData, boolean inFullUpdate )
	{
		if( !STMUtil.isStatusUpdatedToOk( inData, inFullUpdate ) || metaDataInitiated )
			return;
		
		stm.logInfo( "Config data recieved for meta data "+inData.getDataMap() );
		
		Iterator<Entry<String, Object>> iter = inData.getDataMap().iterator();
		
		HashMap<String, IPersistentMap<String, Object>> viewsToBeCreated = new HashMap<String, IPersistentMap<String, Object>>();
		
		while( iter.hasNext() )
		{
			Entry<String, Object> entry = iter.next();
			Object value = entry.getValue();
			
			if( value instanceof PersistentArrayList<?> )
			{
				//Enums
				PersistentArrayList<?> list = (PersistentArrayList<?>)value;
				ArrayList<String> enumValues = new ArrayList<String>();
				
				for( int i = 0; i < list.size(); i++ )
				{
					String data = (String)list.get(i);
					enumValues.add( data );
				}
				
				EnumInput enumInput = new EnumInput( enumValues.toArray( new String[]{} ) );
				
				typeToInput.put( entry.getKey(), enumInput );
			}
			else if( value instanceof IPersistentMap<?, ?> )
			{
				//Container value
				final String fieldKey = entry.getKey();
				ISTMEntryKey containerEntryKey = STMUtil.createEntryKey( serviceKey, DataConstants.STATE_TYPE_CONTAINER, fieldKey );
				ContainerSubscriber containerSub = new ContainerSubscriber();
				
				ReferenceInput refInput = new ReferenceInput( containerSub );
				containerSub.initialize( stm, containerEntryKey );
				
				typeToContainer.put( fieldKey, containerSub );
		
				typeToInput.put( entry.getKey(), refInput );
				
				viewsToBeCreated.put( entry.getKey(), ((IPersistentMap<String, Object>)value) );
//				parent.getDisplay().asyncExec( new Runnable()
//				{
//					@Override
//					public void run() 
//					{
//						viewer = new DataViewer( parent, SWT.NONE, inData.getDataMap(), fieldKey );
//						parent.layout();
//						parent.update();
//					}
//				});
			}
		}
		
		metaDataInitiated = true;
		
		for( Entry<String, IPersistentMap<String, Object>> viewInstruction : viewsToBeCreated.entrySet() )
		{
			String fieldKey = viewInstruction.getKey();
			IPersistentMap<String, Object> value = viewInstruction.getValue();
			editor.addViewer( fieldKey, value, this );
		}
	}
	
	public InputContainer getInputContainer( String inType )
	{
		return typeToInput.get( inType );
	}
	
	public ContainerSubscriber getContainerSubscriber( String inType )
	{
		return typeToContainer.get( inType );
	}

	@Override
	public void queryNotAvailible( Object inTag )
	{
		// TODO Auto-generated method stub
		
	}


}
