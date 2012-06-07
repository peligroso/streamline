package org.juxtapose.streamline.producer;

import java.util.HashMap;

import static org.juxtapose.streamline.util.DataConstants.*;

/**
 * @author Pontus Jörgne
 * 7 aug 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class ProducerUtil
{
	public static String AND = "&";
	public static String EQUALS = "=";
	public static String SERVICE_KEY_DELIM = ":";
	
	
	/**
	 * @param inServiceKey
	 * @param inKeyValues
	 * @return
	 */
	public static ISTMEntryKey createDataKey( String inServiceKey, String inType, String[] inKeys, String[] inValues )
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
	public static ISTMEntryKey createDataKey( String inServiceKey, String inType, String inSingleValue )
	{
		String key = inServiceKey+SERVICE_KEY_DELIM+inType+SERVICE_KEY_DELIM+FIELD_SINGLE_VALUE_DATA_KEY+EQUALS+inSingleValue;
		
		return new STMEntryKey( inServiceKey, inType, inSingleValue, key ); 
	}
}
