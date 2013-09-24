package org.juxtapose.streamline.swt.spl;

import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.swt.datatable.DataEditingSupport;
import org.juxtapose.streamline.swt.datatable.DataEditingSupportEnum;
import org.juxtapose.streamline.swt.datatable.DataEditingSupportLong;
import org.juxtapose.streamline.swt.datatable.DataEditingSupportRef;
import org.juxtapose.streamline.swt.datatable.InputContainer;
import org.juxtapose.streamline.swt.datatable.ReferenceInput;
import static org.juxtapose.streamline.tools.CollectionMethods.*;

import org.juxtapose.streamline.tools.CollectionMethods;
import org.juxtapose.streamline.tools.STMUtil;
import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.data.DataTypeLazyRef;

import com.trifork.clj_ds.IPersistentMap;

public class ClientViewMethods
{
	/**
	 * @param inService
	 * @param inType
	 * @param inKeyList
	 * @param inData
	 * @return
	 */
	public static ISTMEntryKey createEntryKey( String inService, String inType, PersistentArrayList<Object> inKeyList, IPersistentMap<String, Object> inData)
	{
		String[] fields = new String[inKeyList.size()];
		String[] vals = new String[inKeyList.size()];
		
		for( int i = 0; i < inKeyList.size(); i++ )
		{
			String field = (String)inKeyList.get( i );
			
			Object val = inData.valAt( field );
			
			if( val == null )
				return null;
			
			fields[i] = field;
			if( val instanceof DataTypeLazyRef )
			{
				vals[i] = ((DataTypeLazyRef)val).get().getSymbolicName();
			}
			else
			{
				vals[i] = val.toString();
			}
		}
		
		if( vals.length == 1 )
			return STMUtil.createEntryKey( inService, inType, vals[0] );
		else
			return STMUtil.createEntryKey( inService, inType, fields, vals );
	}
	
	/**
	 * Decide how data should be edited based on input value and container type
	 * @param inputContainer
	 * @param inKeyList
	 * @param inKey
	 * @param inDisplay
	 * @param inViewer
	 * @return EditingSupport
	 */
	public final static EditingSupport getEditingSupportStr( InputContainer inputContainer, PersistentArrayList<Object> inKeyList, String inKey, Display inDisplay, TableViewer inViewer )
	{
		
		 if( inputContainer instanceof ReferenceInput )
		 {
			 return new DataEditingSupportRef( inViewer, inKey, inputContainer, inDisplay, contains( inKeyList, inKey ) );
		 }
		 else
		 {
			 return new DataEditingSupportEnum( inViewer, inKey, inputContainer, inDisplay, contains( inKeyList, inKey ) );
		 }
	}
	/**
	 * Decide how data should be edited based on input value and container type
	 * @param inputValue
	 * @param inputContainer
	 * @param inKeyList
	 * @param inKey
	 * @param inDisplay
	 * @param inViewer
	 * @return
	 */
	public final static EditingSupport getEditingSupport( Object inputValue, InputContainer inputContainer, PersistentArrayList<Object> inKeyList, String inKey, Display inDisplay, TableViewer inViewer )
	{
		if( inputValue instanceof String )
		{
			if( ((String)inputValue).isEmpty() )
			{
				return new DataEditingSupport( inViewer, inKey, CollectionMethods.contains( inKeyList, inKey ) );
			}
			else
			{
				return getEditingSupportStr( inputContainer, inKeyList, inKey, inDisplay, inViewer );
			}
		}
		else if( inputValue instanceof Long )
		{
			return new DataEditingSupportLong( inViewer, inKey, CollectionMethods.contains( inKeyList, inKey ) );
		}
		else if( inputValue instanceof PersistentArrayList<?> )
		{
			return new DataEditingSupportEnum( inViewer, inKey, inputContainer, inDisplay, CollectionMethods.contains( inKeyList, inKey ) );
		}
		return null;
	}
	
	/**
	 * @param inData
	 * @return
	 */
	public static final String getDataLabel( Object inData )
	{
		if( inData instanceof DataTypeLazyRef )
			return ((DataTypeLazyRef)inData).get().getSymbolicName();
		else
			return inData.toString();
	}
	
}
