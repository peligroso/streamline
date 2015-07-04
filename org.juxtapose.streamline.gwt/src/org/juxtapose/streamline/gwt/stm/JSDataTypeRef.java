package org.juxtapose.streamline.gwt.stm;


/**
 * @author Pontus Jörgne
 * 20 apr 2015
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class JSDataTypeRef {

	final String entryKey;
	final JSSTMEntry referenceData;
	
	public JSDataTypeRef(String inValue)
	{
		entryKey = inValue;
		referenceData = null;
	}
	
	public JSDataTypeRef(String inValue, JSSTMEntry inData )
	{
		entryKey = inValue;
		referenceData = inData;
	}
	
	public JSSTMEntry getReferenceData()
	{
		return referenceData;
	}
	
	public String getReferenceKey()
	{
		return entryKey;
	}
}
