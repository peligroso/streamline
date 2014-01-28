package org.juxtapose.streamline.declarativeservice;

import static org.juxtapose.streamline.tools.DataConstants.STATE_TYPE_CONTAINER;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.tools.ProducerServiceConstants;
import org.juxtapose.streamline.tools.STMUtil;
import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.producerservices.DataRefContainerProducer;

import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.PersistentHashMap;

/**
 * @author Pontus Jörgne
 * 26 dec 2013
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class MetaDataObjectContext 
{
	final Map<String, Object> primary;
	final String name;
	
	final ISTMEntryKey containerKey;
	final DataRefContainerProducer container;
	
	final IPersistentMap<String, Object> entryMap;
	
	/**
	 * @param jsObject
	 */
	public MetaDataObjectContext( String inServiceKey, String inKey, JSONObject jsObject, ISTM inSTM )
	{
		name = inKey;
		
		IPersistentMap<String, Object> map = PersistentHashMap.emptyMap();
		PersistentArrayList<String> keyList = new PersistentArrayList<String>( );
		Map<String, Object> keyMap = new HashMap<String, Object>();
		
		for( Object objKey : jsObject.keySet() )
		{
			Object object = jsObject.get( objKey );
			
			String keyStr = (String)objKey;
			
			if( ((String)objKey).startsWith( "@" ) )
			{
				keyStr = keyStr.substring( 1 );
				keyList = keyList.add( keyStr );
				keyMap.put( keyStr, object );
			}
			
			if( object instanceof JSONObject )
			{
				IPersistentMap<String, Object> entryMap = getEntryMap( (JSONObject)object );
				map = map.assoc( keyStr, entryMap );
			}
			else
			{
				map = map.assoc( keyStr, object );
			}
		}
		
		entryMap = map.assoc( DataConstants.FIELD_KEYS, keyList );
		
		containerKey = STMUtil.createEntryKey( inServiceKey, STATE_TYPE_CONTAINER, inKey );
		container = new DataRefContainerProducer( containerKey, inSTM );		
		primary = Collections.unmodifiableMap( keyMap );
	}
	
	/**
	 * @param inJsonObj
	 * @return
	 */
	private IPersistentMap<String, Object> getEntryMap( JSONObject inJsonObj )
	{
		IPersistentMap<String, Object> map = PersistentHashMap.emptyMap();
		
		for( Object objKey : inJsonObj.keySet() )
		{
			Object object = inJsonObj.get( objKey );
			
			String keyStr = (String)objKey;
			
			if( object instanceof JSONObject )
			{
				IPersistentMap<String, Object> entryMap = getEntryMap( (JSONObject)object );
				map = map.assoc( keyStr, (Long)object );
			}
			else
			{
				map = map.assoc( keyStr, object );
			}
		}
		
		return map;
	}
}
