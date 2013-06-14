package org.juxtapose.fxtradingclient;

import org.eclipse.jface.viewers.ColumnViewer;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeLong;

public class DataEditingSupportLong extends DataEditingSupport {

	public DataEditingSupportLong( ColumnViewer viewer, String inKey ) 
	{
		super( viewer, inKey );
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.fxtradingclient.DataEditingSupport#getDataType(java.lang.Object)
	 */
	protected DataType<?> getDataType( Object inValue )
	{
		Long l = Long.parseLong( inValue.toString() );
		return new DataTypeLong( l );
	}

}
