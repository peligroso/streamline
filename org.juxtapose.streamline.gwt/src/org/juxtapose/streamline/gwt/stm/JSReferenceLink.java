package org.juxtapose.streamline.gwt.stm;



/**
 * @author Pontus Jörgne
 * 20 apr 2015
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class JSReferenceLink implements IJSSTMEntrySubscriber
{
	private final JSSTM stm;
	private final IJSSTMEntryProducer producer;
	
	private final String fieldKey;
	private final JSDataTypeRef ref;
	
	/**
	 * @param inParent
	 * @param inSTM
	 * @param inHashKey
	 * @param inRef
	 */
	protected JSReferenceLink( IJSSTMEntryProducer inProducer, JSSTM inSTM, String inFieldKey, JSDataTypeRef inRef )
	{
		stm = inSTM;
		producer = inProducer; 
		ref = inRef;
		fieldKey = inFieldKey;
	}
	
	
	@Override
	public void updateData( String inKey, final JSSTMEntry inData, boolean inFirstUpdate )
	{
		//Notify producer about delivered Data. ON_Request Data is not interesting
		if( inData.getStatus() != JSSTMConstants.STATUS_ON_REQUEST )
		{
			producer.referencedDataUpdated( fieldKey, this, inData );
		}
	}
	
	public JSDataTypeRef getRef()
	{
		return ref;
	}
	
	protected void start()
	{
		stm.subscribeToData( ref.getReferenceKey(), this );
	}
	
	protected void stop()
	{
		stm.unsubscribeToData( ref.getReferenceKey(), this );
	}
	
}
