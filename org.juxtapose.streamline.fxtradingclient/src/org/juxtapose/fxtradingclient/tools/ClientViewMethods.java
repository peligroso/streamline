package org.juxtapose.fxtradingclient.tools;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.tools.STMUtil;
import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeString;

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
	public static ISTMEntryKey createEntryKey( String inService, String inType, PersistentArrayList<DataType<?>> inKeyList, IPersistentMap<String, DataType<?>> inData)
	{
		String[] fields = new String[inKeyList.size()];
		String[] vals = new String[inKeyList.size()];
		
		for( int i = 0; i < inKeyList.size(); i++ )
		{
			DataTypeString field = (DataTypeString)inKeyList.get( i );
			
			DataType<?> val = inData.valAt( field.get() );
			
			if( val == null )
				return null;
			
			fields[i] = field.get();
			vals[i] = val.get().toString();
		}
		
		if( vals.length == 1 )
			return STMUtil.createEntryKey( inService, inType, vals[0] );
		else
			return STMUtil.createEntryKey( inService, inType, fields, vals );
	}
}
