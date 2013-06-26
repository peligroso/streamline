package org.juxtapose.fxtradingclient;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeString;

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
		IPersistentMap<String, DataType<?>> map = ((ViewDataObject)element).getData();
		DataType<?> val = map.valAt( key );
		if( val == null )
			return "";
		
		return val.get().toString();
	}

	@Override
	protected void setValue(Object element, Object value) 
	{
		IPersistentMap<String, DataType<?>> map = ((ViewDataObject)element).getData();
		map = map.assoc( key, getDataType( value ) );
		((ViewDataObject)element).updateData(map, key);
		getViewer().update( element, null );
	}
	
	protected DataType<?> getDataType( Object inValue )
	{
		return new DataTypeString( inValue.toString() );
	}

}
