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
	
	public DataEditingSupport( ColumnViewer viewer, String inKey ) 
	{
		super( viewer );
		key = inKey;
	}
	
	@Override
	protected CellEditor getCellEditor(Object element) {
		return new TextCellEditor(( (TableViewer) getViewer() ).getTable());
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) 
	{
		IPersistentMap<String, DataType<?>> map = ((ViewDataObject)element).getData();
		return map.valAt( key ).get().toString();
	}

	@Override
	protected void setValue(Object element, Object value) 
	{
		IPersistentMap<String, DataType<?>> map = ((ViewDataObject)element).getData();
		map = map.assoc( key, getDataType( value ) );
		((ViewDataObject)element).updateData(map);
		getViewer().update( element, null );
	}
	
	protected DataType<?> getDataType( Object inValue )
	{
		return new DataTypeString( inValue.toString() );
	}

}
