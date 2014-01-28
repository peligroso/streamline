package org.juxtapose.streamline.tools;

import static org.juxtapose.streamline.tools.DataConstants.FIELD_SINGLE_VALUE_DATA_KEY;

import java.util.HashMap;
import java.util.Map;

import javax.management.AttributeNotFoundException;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataTypeLazyRef;

import com.trifork.clj_ds.IPersistentMap;

public class STMUtil
{
	public static String AND = "&";
	public static String EQUALS = "=";
	public static String SERVICE_KEY_DELIM = ":";
	
	
	/**
	 * @param inServiceKey
	 * @param inKeyValues
	 * @return
	 */
	public static ISTMEntryKey createEntryKey( String inServiceKey, String inType, String[] inKeys, String[] inValues )
	{
		if( inKeys.length != inValues.length )
			throw new IllegalArgumentException("Key-value pairs must be even ");
		
		HashMap<String, String> map = new HashMap<String, String>();
		StringBuilder sb = new StringBuilder(inServiceKey.toString());
		sb.append(SERVICE_KEY_DELIM);
		sb.append(inType);
		sb.append(SERVICE_KEY_DELIM);
		
		for( int i = 0; i < inKeys.length; i++ )
		{
			String key = inKeys[i];
			String value = inValues[i];
			
			map.put( key, value );
			if( i != 0 )
				sb.append(AND);
			
			sb.append( key );
			sb.append( EQUALS );
			sb.append( value );
		}
		
		return new STMEntryKey( inServiceKey, inType, map, sb.toString() ); 
	}
	
	/**
	 * @param inSingleValue
	 * @return
	 */
	public static ISTMEntryKey createEntryKey( String inServiceKey, String inType, String inSingleValue )
	{
		String key = inServiceKey+SERVICE_KEY_DELIM+inType+SERVICE_KEY_DELIM+FIELD_SINGLE_VALUE_DATA_KEY+EQUALS+inSingleValue;
		
		return new STMEntryKey( inServiceKey, inType, inSingleValue, key ); 
	}
	
	/**
	 * @param inServiceKey
	 * @param inEntry
	 * @return
	 */
	public static boolean isServiceStatusUpdatedToOk( String inServiceKey, ISTMEntry inEntry )
	{
		Object dataValue = inEntry.getUpdatedValue( inServiceKey );
		if( dataValue == null )
			return false;
		
		return dataValue.equals( Status.OK.toString() );
	}
	
	/**
	 * @param inEntry
	 * @param inFullUpdate
	 * @return
	 */
	public static boolean isStatusUpdatedToOk( ISTMEntry inEntry, boolean inFullUpdate )
	{
		Object dataValue = inFullUpdate ? inEntry.getValue( DataConstants.FIELD_STATUS ) : inEntry.getUpdatedValue( DataConstants.FIELD_STATUS );
		if( dataValue == null )
			return false;
		
		return dataValue.equals( Status.OK );
	}
	
	public static boolean isStatusOk( ISTMEntry inEntry)
	{
		Object dataValue = inEntry.getValue( DataConstants.FIELD_STATUS );
		if( dataValue == null )
			return false;
		
		return dataValue.equals( Status.OK );
	}
	
	/**
	 * @param inServiceID
	 * @param inType
	 * @param inData
	 * @param inMap
	 * @return
	 * @throws AttributeNotFoundException
	 */
	public static ISTMEntryKey createEntryKey( String inServiceID, String inType, IPersistentMap<String, Object> inData, Map<String, Object> inMap )throws AttributeNotFoundException
	{
		int i = 0;
		
		String[] attr = new String[ inMap.size() ];
		String[] val = new String[ inMap.size() ];
		
		for( String key : inMap.keySet() )
		{
			Object oo = inMap.get( key );
			Object dataOb = inData.valAt( key );
			
			if( dataOb == null )
				throw new AttributeNotFoundException( );
			
			attr[i] = key;
			
			if( oo instanceof String && ((String)oo).isEmpty() )
			{
				val[i] = dataOb.toString();
			}
			else if( oo instanceof String )
			{
				val[i] = ((DataTypeLazyRef)dataOb).get().getSymbolicName();
			}
			
			i++;
		}
		
		if( val.length == 1 )
			return STMUtil.createEntryKey( inServiceID, inType, val[0] );
		else
			return STMUtil.createEntryKey( inServiceID, inType, attr, val);
	}
	
}
