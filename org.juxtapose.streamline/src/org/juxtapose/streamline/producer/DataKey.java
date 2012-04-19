package org.juxtapose.streamline.producer;

import java.util.HashMap;

import org.juxtapose.streamline.util.DataConstants;

/**
 * @author Pontus Jörgne
 * 7 aug 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class DataKey implements IDataKey
{
	private final HashMap<Integer, String> keyMap;
	private final Integer producerServiceKey;
	private final String type;
	private final String key;
	private final String singleValue;
	
	/**
	 * @param inProducerServiceKey
	 * @param inMap
	 * @param inKey
	 * DataKey are created via ProducerUtil
	 */
	protected DataKey( Integer inProducerServiceKey, String inType, HashMap<Integer, String> inMap, String inKey )
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
	protected DataKey( Integer inProducerServiceKey, String inType, String inSingleValue, String inKey )
	{
		producerServiceKey = inProducerServiceKey;
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		map.put( DataConstants.FIELD_SINGLE_VALUE_DATA_KEY, inSingleValue );
		keyMap = map;
		key = inKey;
		type = inType;
		singleValue = inSingleValue;
	}
	
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
	public String getValue( Integer inKey )
	{
		return keyMap.get( inKey );
	}
	
	
	/**
	 * @param inKey
	 * @return
	 */
	public boolean equals( Object inKey )
	{
		if( inKey == this )
			return true;
		
		if( ! (inKey instanceof IDataKey) )
			return false;
		
		return key.equals( ((IDataKey)inKey ).getKey() );
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return key.hashCode();
	}

	@Override
	public Integer getService()
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
