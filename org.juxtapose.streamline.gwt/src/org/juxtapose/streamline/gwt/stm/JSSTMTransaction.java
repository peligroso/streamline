package org.juxtapose.streamline.gwt.stm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Pontus Jörgne
 * 20 apr 2015
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public abstract class JSSTMTransaction 
{
	private final String entryKey;
	private final boolean isFullUpdate;
	
	private HashMap<String, Object> stateInstruction;
	private final Set<String> deltaState = new HashSet<String>();
	
	private final Map<String, JSDataTypeRef> addedDataReferences;
	private final List<String> removedDataReferences;
	
	private final IJSSTMEntryProducer producer;
	
	private String status;
	
	private boolean containesReferenceInstructions = false;
	
	private boolean disposed = false;
	private boolean inCompleteStateTransition = false;
	
	
	/**
	 * @param inDataKey
	 * @param inProducer
	 */
	public JSSTMTransaction( String inEntryKey, IJSSTMEntryProducer inProducer, int inAddedRefenrence, int inRemovedReferences, boolean inIsFullUpdate ) 
	{
		entryKey = inEntryKey;
		producer = inProducer;
		isFullUpdate = inIsFullUpdate;
		
		addedDataReferences = inAddedRefenrence == 0 ? null : new HashMap<String, JSDataTypeRef>( inAddedRefenrence );
		removedDataReferences = inRemovedReferences == 0 ? null : new ArrayList<String>( inRemovedReferences );
	}
	
	public void putInitDataState( HashMap<String, Object> inMap, String inStatus )
	{
		stateInstruction = inMap;
		status = inStatus;
	}
	
	public abstract void execute();
	
	public void putValue( String inKey, Object inData )
	{
		if( inData instanceof JSDataTypeRef )
		{
			addedDataReferences.put( inKey, (JSDataTypeRef)inData );
			containesReferenceInstructions = true;
		}
		stateInstruction.put( inKey, inData );
		deltaState.add(inKey);
	}
	
	public void removeValue( String inKey )throws Exception
	{
		Object removedData = stateInstruction.remove( inKey );
		deltaState.add(inKey);
		
		if( removedData instanceof JSDataTypeRef )
		{
			removedDataReferences.add( inKey );
			containesReferenceInstructions = true;
		}
	}
	
	protected HashMap<String, Object> getStateInstruction()
	{
		return stateInstruction;
	}
	
	/**
	 * @return
	 */
	protected Set<String> getDeltaState()
	{
		return deltaState;
	}
	
	/**
	 * @return
	 */
	protected String getDataKey()
	{
		return entryKey;
	}
	
	public IJSSTMEntryProducer producedBy()
	{
		return producer;
	}
	
	/**
	 * @return
	 */
	public Map<String, JSDataTypeRef> getAddedReferences()
	{
		return addedDataReferences;
	}
	
	/**
	 * @return
	 */
	public List<String> getRemovedReferences()
	{
		return removedDataReferences;
	}
	
	/**
	 * @param inStatus
	 */
	public void setStatus( String inStatus )
	{
		putValue( JSSTMConstants.FIELD_STATUS, inStatus );
		status = inStatus;
	}
	
	public String getStatus()
	{
		return status;
	}
	
	public Object get( String inFieldKey )
	{
		return stateInstruction.get( inFieldKey );
	}
	
	public void dispose()
	{
		disposed = true;
	}
	
	public boolean isDisposed()
	{
		return disposed == true;
	}
	
	public void setIncompleteStateTransition( )
	{
		inCompleteStateTransition = true;
	}
	
	public boolean isCompleteStateTransition( )
	{
		return !inCompleteStateTransition;
	}
	
	public boolean containesReferenceInstructions()
	{
		return containesReferenceInstructions;
	}
	
	public boolean isFullUpdate()
	{
		return isFullUpdate;
	}
	
}
