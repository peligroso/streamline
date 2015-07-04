package org.juxtapose.streamline.gwt.gui;

import java.util.HashMap;

import org.juxtapose.streamline.gwt.stm.IJSSTMEntrySubscriber;
import org.juxtapose.streamline.gwt.stm.JSSTM;
import org.juxtapose.streamline.gwt.stm.JSSTMConstants;
import org.juxtapose.streamline.gwt.stm.JSSTMEntry;

public class JSGenericEditor implements IJSSTMEntrySubscriber{
	
	JSSTM stm;
	private final String serviceKey;
	
	boolean subscribedMetaData;
	
	public JSGenericEditor(JSSTM inSTM, String inServiceKey)
	{
		stm = inSTM;
		
		stm.subscribeToData( JSSTMConstants.PRODUCER_SERVICE_KEY, this);
		
		serviceKey = inServiceKey;
	}

	@Override
	public void updateData( String inKey, JSSTMEntry inData,boolean inFullUpdate ) 
	{
		if( inKey.equals( JSSTMConstants.PRODUCER_SERVICE_KEY ) && !subscribedMetaData )
		{
			if( JSSTMConstants.isServiceStatusUpdatedToOk( serviceKey, inData ))
			{
				stm.logInfo( "requesting key for config metadata" );
//				HashMap<String, String> queryMap = new HashMap<String, String>();
//				queryMap.put( JSSTMConstants.FIELD_QUERY_KEY, DataConstants.STATE_TYPE_META );
//				stm.getDataKey( serviceKey, this, serviceKey, queryMap );
				
			}
		}
	}
	
	

}
