package org.juxtapose.streamline.gwt.stm;


/**
 * @author Pontus J�rgne
 * 20 apr 2015
 * Copyright (c) Pontus J�rgne. All rights reserved
 */
public interface IJSSTMEntrySubscriber 
{
	public void updateData( String inKey, JSSTMEntry inData, boolean inFullUpdate );
}
