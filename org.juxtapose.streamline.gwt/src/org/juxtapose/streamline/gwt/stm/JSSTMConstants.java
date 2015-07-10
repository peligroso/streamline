package org.juxtapose.streamline.gwt.stm;


public class JSSTMConstants {

	public static final String PRODUCER_SERVICE_KEY = "STM:PRODUCER_SERVICES:#V=PRODUCER_SERVICES";
	
	public static final String STATUS_OK = "OK";
	public static final String STATUS_ON_REQUEST = "ON_REQUEST";
	public static final String STATUS_STALE = "STALE";
	public static final String STATUS_ERROR = "ERROR";
	public static final String STATUS_NA = "NA";
	
	public static String SERVICE_CONFIG = "C";
	
	public static String PRODUCER_SERVICES = "PRODUCER_SERVICES";
	
	
	public static final String FIELD_SINGLE_VALUE_DATA_KEY 	= "#V";
	public static final String FIELD_QUERY_KEY 				= "#Q";
	
	public static final String FIELD_STATUS 				= "#S";
	
	public static final String FIELD_TYPE 					= "#T";
	
	public static final String FIELD_TIMESTAMP 				= "#TS";
	
	public static final String FIELD_KEYS 				= "#K";
	
	
	public static final String STATE_TYPE_META 				= "%M";
	
	public static final String STATE_TYPE_CONTAINER 		= "%C";
	
	public static boolean isServiceStatusUpdatedToOk( String inServiceKey, JSSTMEntry inEntry )
	{
		Object dataValue = inEntry.getUpdatedValue( inServiceKey );
		if( dataValue == null )
			return false;
		
		return dataValue.equals( STATUS_OK );
	}
}
