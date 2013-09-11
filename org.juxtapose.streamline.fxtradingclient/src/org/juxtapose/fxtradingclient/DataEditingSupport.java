package org.juxtapose.fxtradingclient;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

import com.trifork.clj_ds.IPersistentMap;

public class DataEditingSupport extends EditingSupport 
{
	protected final String key;
	boolean keyField;
	
	public DataEditingSupport( ColumnViewer viewer, String inKey, boolean inKeyField ) 
	{
		super( viewer );
		key = inKey;
		keyField = inKeyField;
	}
	
	@Override
	protected CellEditor getCellEditor(Object element) {
		return new TextCellEditor(( (TableViewer) getViewer() ).getTable());
	}

	@Override
	protected boolean canEdit(Object element) 
	{
		return !keyField || ((ViewDataObject)element).getKey() == null;
	}

	@Override
	protected Object getValue(Object element) 
	{
		IPersistentMap<String, Object> map = ((ViewDataObject)element).getData();
		Object val = map.valAt( key );
		if( val == null )
			return "";
		
		return val.toString();
	}

	@Override
	protected void setValue(Object element, Object value) 
	{
		IPersistentMap<String, Object> map = ((ViewDataObject)element).getData();
		map = map.assoc( key, getDataType( value ) );
		((ViewDataObject)element).updateData(map, key);
		getViewer().update( element, null );
	}
	
	protected Object getDataType( Object inValue )
	{
		return inValue.toString();
	}

}
