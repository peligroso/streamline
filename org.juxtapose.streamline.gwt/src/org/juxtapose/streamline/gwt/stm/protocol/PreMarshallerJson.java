package org.juxtapose.streamline.gwt.stm.protocol;

import java.util.Map;

import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;


public class PreMarshallerJson {

	public static String SERVICE = "1";
	public static String TAG = "2";
	public static String MAP = "3";
	
	public static JSONObject createSubQuery( String inService, int inTag, Map<String, String> inQuery )
	{
		JSONObject rootObject = new JSONObject();
		rootObject.put( SERVICE, new JSONString( inService ) );
		rootObject.put( TAG, new JSONNumber( inTag ) );

		JSONObject mapObject = new JSONObject();
		for( Map.Entry<String, String> entry : inQuery.entrySet() )
		{
			mapObject.put( entry.getKey(), new JSONString( entry.getValue() ) );
		}
		
		rootObject.put( MAP, mapObject );
		
		return rootObject;
	}
}
