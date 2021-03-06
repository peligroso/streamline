package org.juxtapose.fxtradingclient;

import org.eclipse.jface.viewers.ColumnViewer;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeLong;

public class DataEditingSupportLong extends DataEditingSupport {

	public DataEditingSupportLong( ColumnViewer viewer, String inKey, boolean inKeyField ) 
	{
		super( viewer, inKey, inKeyField );
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.fxtradingclient.DataEditingSupport#getDataType(java.lang.Object)
	 */
	protected DataType<?> getDataType( Object inValue )
	{
		if( inValue == null || "".equals( inValue.toString() ) )
			return new DataTypeLong( 0l );
		
		Long l = Long.parseLong( inValue.toString() );
		return new DataTypeLong( l );
	}

}
