package org.juxtapose.streamline.protocol.message;

import java.util.Map;

import org.json.simple.JSONObject;

public class PreMarshallerJson {

	public static String SERVICE = "1";
	public static String TAG = "2";
	public static String MAP = "3";
	
	public static JSONObject createSubQuery( String inService, int inTag, Map<String, String> inQuery )
	{
		JSONObject rootObject = new JSONObject();
		rootObject.put( SERVICE, inService );
		rootObject.put( TAG, inTag );

		JSONObject mapObject = new JSONObject(inQuery);
		
		rootObject.put( MAP, inQuery );
		
		return rootObject;
	}
}
