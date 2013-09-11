package org.juxtapose.fxtradingclient.tools;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.tools.STMUtil;
import org.juxtapose.streamline.util.PersistentArrayList;

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
			vals[i] = val.toString();
		}
		
		if( vals.length == 1 )
			return STMUtil.createEntryKey( inService, inType, vals[0] );
		else
			return STMUtil.createEntryKey( inService, inType, fields, vals );
	}
}
