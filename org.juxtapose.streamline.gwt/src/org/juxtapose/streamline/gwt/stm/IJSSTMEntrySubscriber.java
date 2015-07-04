package org.juxtapose.streamline.gwt.stm;


/**
 * @author Pontus Jörgne
 * 20 apr 2015
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public interface IJSSTMEntrySubscriber 
{
	public void updateData( String inKey, JSSTMEntry inData, boolean inFullUpdate );
}
