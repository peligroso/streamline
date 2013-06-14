package org.juxtapose.fxtradingclient;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.juxtapose.streamline.util.data.DataType;

import com.trifork.clj_ds.IPersistentMap;

/**
 * @author Pontus Jörgne
 * 3 jun 2013
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class DataEditingSupportEnum extends DataEditingSupport
{
	String[] items;
	
	public DataEditingSupportEnum( ColumnViewer viewer, String inKey, String[] inItems ) 
	{
		super( viewer, inKey );
		items = inItems;
	}
	
	@Override
	protected CellEditor getCellEditor(Object element) 
	{
		return new ComboBoxCellEditor(( (TableViewer) getViewer() ).getTable(), items);
	}
	
	@Override
	protected Object getValue(Object element) 
	{
		IPersistentMap<String, DataType<?>> map = ((ViewDataObject)element).getData();
		String val = map.valAt( key ).get().toString();
		
		for( int i = 0; i < items.length; i++ )
		{
			if( val.equals( items[i] ) )
				return i;
		}
		return -1;
	}

	@Override
	protected void setValue(Object element, Object value) 
	{
		Integer index = (Integer)value;
		if( index < 0 || index > items.length-1 )
			return;
		
		String val = items[index];
		
		IPersistentMap<String, DataType<?>> map = ((ViewDataObject)element).getData();
		map = map.assoc( key, getDataType( val ) );
		((ViewDataObject)element).updateData(map);
		getViewer().update( element, null );
		
	}

}
