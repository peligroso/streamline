package org.juxtapose.fxtradingclient;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;
import org.juxtapose.streamline.util.IInputListener;
import org.juxtapose.streamline.util.ISTMContainerListener;
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
	
	ComboBoxCellEditor cellEditor;
	
	Display display;
	
	public DataEditingSupportEnum( ColumnViewer viewer, String inKey, final InputContainer inInputContainer, Display inDisplay, boolean inKeyField ) 
	{
		super( viewer, inKey, inKeyField );
		items = inInputContainer.getInputObjects();
		display = inDisplay;
		
		inInputContainer.addInputListener( new IInputListener()
		{
			@Override
			public void inputChanged()
			{
				items = inInputContainer.getInputObjects();
				updateItems( items );
			}
		} );
	}
	
	@Override
	protected CellEditor getCellEditor(Object element) 
	{
		cellEditor = new ComboBoxCellEditor(( (TableViewer) getViewer() ).getTable(), items);
		return cellEditor;
	}
	
	public void updateItems( final String[] inItems )
	{
		display.asyncExec( new Runnable()
		{
			@Override
			public void run() 
			{
				if( cellEditor != null)
					cellEditor.setItems( inItems );	
			}
			
		});
		
	}
	
	@Override
	protected Object getValue(Object element) 
	{
		IPersistentMap<String, DataType<?>> map = ((ViewDataObject)element).getData();
		DataType<?> dVal = map.valAt( key );
		
		if( dVal == null )
			return -1;
		
		String val = dVal.get().toString();
		
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
