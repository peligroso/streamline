package org.juxtapose.streamline.tools;

import static org.juxtapose.streamline.tools.DataConstants.FIELD_SINGLE_VALUE_DATA_KEY;

import java.util.HashMap;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;

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
		DataType<?> dataValue = inEntry.getUpdatedValue( inServiceKey );
		if( dataValue == null )
			return false;
		
		return dataValue.get().equals( Status.OK.toString() );
	}
	
	/**
	 * @param inEntry
	 * @param inFullUpdate
	 * @return
	 */
	public static boolean isStatusUpdatedToOk( ISTMEntry inEntry, boolean inFullUpdate )
	{
		DataType<?> dataValue = inFullUpdate ? inEntry.getValue( DataConstants.FIELD_STATUS ) : inEntry.getUpdatedValue( DataConstants.FIELD_STATUS );
		if( dataValue == null )
			return false;
		
		return dataValue.get().equals( Status.OK );
	}
}
