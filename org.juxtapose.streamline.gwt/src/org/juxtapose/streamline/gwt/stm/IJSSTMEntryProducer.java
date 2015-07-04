package org.juxtapose.streamline.gwt.stm;



/**
 * @author Pontus J�rgne
 * 20 apr 2015
 * Copyright (c) Pontus J�rgne. All rights reserved
 */
public interface IJSSTMEntryProducer {
	
	void referencedDataUpdated( final String inFieldKey, final JSReferenceLink inLink, final JSSTMEntry inData );
	public void init();
	
	public void addDataReferences( String inFieldKey, JSReferenceLink inLink );
	public JSReferenceLink removeReferenceLink( String inField );
	
	public void dispose();

}
