package org.juxtapose.streamline.tools;

import java.util.HashMap;
import java.util.Set;

import org.juxtapose.streamline.producer.ISTMEntryKey;

/**
 * @author Pontus J�rgne
 * 7 aug 2011
 * Copyright (c) Pontus J�rgne. All rights reserved
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
	
	public String getKey()
	{
		return key;
	}
	
	
	/**
	 * @param inKey
	 * @return
	 */
	public String getValue( String inKey )
	{
		return keyMap.get( inKey );
	}
	
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

	@Override
	public String getService()
	{
		return producerServiceKey;
	}
	
	public String getType()
	{
		return type;
	}
	
	public String getSingleValue()
	{
		return singleValue;
	}
}
