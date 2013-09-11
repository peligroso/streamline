package org.juxtapose.fxtradingclient;

import org.eclipse.jface.viewers.ColumnViewer;

public class DataEditingSupportLong extends DataEditingSupport {

	public DataEditingSupportLong( ColumnViewer viewer, String inKey, boolean inKeyField ) 
	{
		super( viewer, inKey, inKeyField );
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.fxtradingclient.DataEditingSupport#getDataType(java.lang.Object)
	 */
	protected Object getDataType( Object inValue )
	{
		if( inValue == null || "".equals( inValue.toString() ) )
			return new Long( 0l );
		
		Long l = Long.parseLong( inValue.toString() );
		return new Long( l );
	}

}
