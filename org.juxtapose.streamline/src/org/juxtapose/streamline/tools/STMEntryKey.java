package org.juxtapose.streamline.tools;

import java.util.HashMap;
import java.util.Set;

import org.juxtapose.streamline.producer.ISTMEntryKey;

/**
 * @author Pontus Jörgne
 * 7 aug 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class STMEntryKey implements ISTMEntryKey
{
	private final HashMap<String, String> keyMap;
	private final String producerServiceKey;
	private final String type;
	private final String key;
	private final String singleValue;
	
	/**
	 * @param inProducerServiceKey
	 * @param inMap
	 * @param inKey
	 * DataKey are created via ProducerUtil
	 */
	protected STMEntryKey( String inProducerServiceKey, String inType, HashMap<String, String> inMap, String inKey )
	{
		producerServiceKey = inProducerServiceKey;
		keyMap = inMap;
		key = inKey;
		type = inType;
		singleValue = null;
	}
	
	/**
	 * @param inProducerServiceKey
	 * @param inType
	 * @param inSingleValue
	 * @param inKey
	 */
	protected STMEntryKey( String inProducerServiceKey, String inType, String inSingleValue, String inKey )
	{
		producerServiceKey = inProducerServiceKey;
		HashMap<String, String> map = new HashMap<String, String>();
		map.put( DataConstants.FIELD_SINGLE_VALUE_DATA_KEY, inSingleValue );
		keyMap = map;
		key = inKey;
		type = inType;
		singleValue = inSingleValue;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return key.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.producer.ISTMEntryKey#getKey()
	 */
	public String getKey()
	{
		return key;
	}
	
	
	/**
	 * @param inKey
	 * @return
	 */
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.producer.ISTMEntryKey#getValue(java.lang.String)
	 */
	public String getValue( String inKey )
	{
		return keyMap.get( inKey );
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.producer.ISTMEntryKey#getKeys()
	 */
	public Set<String> getKeys()
	{
		return keyMap.keySet();
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals( Object inKey )
	{
		if( inKey == this )
			return true;
		
		if( ! (inKey instanceof ISTMEntryKey) )
			return false;
		
		return key.equals( ((ISTMEntryKey)inKey ).getKey() );
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return key.hashCode();
	}

	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.producer.ISTMEntryKey#getService()
	 */
	@Override
	public String getService()
	{
		return producerServiceKey;
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.producer.ISTMEntryKey#getType()
	 */
	public String getType()
	{
		return type;
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.producer.ISTMEntryKey#getSingleValue()
	 */
	public String getSingleValue()
	{
		return singleValue;
	}
	
	/**
	 * @return
	 */
	public String getSymbolicName()
	{
		if( singleValue != null )
			return singleValue;
		
		StringBuilder sb = new StringBuilder();
		for( String val : keyMap.values() )
		{
			sb.append( val );
		}
		
		return sb.toString();
	}
}
