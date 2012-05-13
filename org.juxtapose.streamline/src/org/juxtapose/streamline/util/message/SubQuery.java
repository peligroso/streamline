package org.juxtapose.streamline.util.message;

import java.util.Map;

public class SubQuery extends Message 
{
	public final String service;
	public final Map<String, String> queryMap;
	
	public SubQuery( String inService, Map<String, String> inQueryMap )
	{
		service = inService;
		queryMap = inQueryMap;
	}
}
