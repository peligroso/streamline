package org.juxtapose.streamline.gwt.stm;


public class JSSTMConstants {

	public static final String PRODUCER_SERVICE_KEY = "STM:PRODUCER_SERVICES:#V=PRODUCER_SERVICES";
	public static final String FIELD_STATUS 				= "#S";
	
	public static final String STATUS_OK = "OK";
	public static final String STATUS_ON_REQUEST = "ON_REQUEST";
	public static final String STATUS_STALE = "STALE";
	public static final String STATUS_ERROR = "ERROR";
	public static final String STATUS_NA = "NA";
	
	public static String SERVICE_CONFIG = "C";
	
	public static boolean isServiceStatusUpdatedToOk( String inServiceKey, JSSTMEntry inEntry )
	{
		Object dataValue = inEntry.getUpdatedValue( inServiceKey );
		if( dataValue == null )
			return false;
		
		return dataValue.equals( STATUS_OK );
	}
}
