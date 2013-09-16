package org.juxtapose.streamline.swt.datatable;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.widgets.Display;
import org.juxtapose.streamline.util.data.DataTypeLazyRef;

import com.trifork.clj_ds.IPersistentMap;

public class DataEditingSupportRef extends DataEditingSupportEnum
{

	/**
	 * @param viewer
	 * @param inKey
	 * @param inInputContainer
	 * @param inDisplay
	 * @param inKeyField
	 */
	public DataEditingSupportRef( ColumnViewer viewer, String inKey, InputContainer inInputContainer, Display inDisplay, boolean inKeyField )
	{
		super( viewer, inKey, inInputContainer, inDisplay, inKeyField );
	}

	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.swt.datatable.DataEditingSupportEnum#setValue(java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void setValue(Object element, Object value) 
	{
		Integer index = (Integer)value;
		if( index < 0 || index > items.length-1 )
			return;
		
		String val = items[index];
		
		IPersistentMap<String, Object> map = ((ViewDataObject)element).getData();
		map = map.assoc( key, getDataType( val ) );
		((ViewDataObject)element).updateData(map, key);
		getViewer().update( element, null );
		
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.swt.datatable.DataEditingSupport#getDataType(java.lang.Object)
	 */
	protected Object getDataType( Object inValue )
	{
		DataTypeLazyRef ref = ((ReferenceInput)inputContainer).getReference( inValue.toString() );
		
		return ref;
	}
}
