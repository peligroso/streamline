package org.juxtapose.streamline.stm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.tools.Preconditions;
import org.juxtapose.streamline.tools.STMAssertionUtil;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeRef;
import org.juxtapose.streamline.util.data.DataTypeStatus;

import com.trifork.clj_ds.IPersistentMap;

/**
 * @author Pontus Jörgne
 * Jan 29, 2012
 * Copyright (c) Pontus Jörgne. All rights reserved
 * 
 * Transaction represents a set of instructions that will take a published data from ¨
 * one state to another in an atomic operation.
 * The commit method will be implemented by the programmer to create the new state instructions
 * Transaction should only exist in a single thread context
 * The data key will be write -locked or CAS referenced during execute
 * 
 * Transactions only live inside one thread and should always be declared anonymous. 
 */
public abstract class STMTransaction
{
	private final ISTMEntryKey m_dataKey;	
	private IPersistentMap<String, DataType<?>> m_stateInstruction;
	
	private final Set<String> m_deltaState = new HashSet<String>();
	
	private final Map<String, DataTypeRef> addedDataReferences;
	private final List<String> removedDataReferences;
	
	private ISTMEntryProducer m_producer = null;
	
	private Status status;
	
	private boolean disposed = false;
	private boolean m_inCompleteStateTransition = false;
	
	private boolean containesReferenceInstructions = false;
	
	/**
	 * @param inDataKey
	 */
	public STMTransaction( ISTMEntryKey inDataKey, int inAddedRefenrence, int inRemovedReferences ) 
	{
		m_dataKey = Preconditions.notNull( inDataKey );
		addedDataReferences = inAddedRefenrence == 0 ? null : new HashMap<String, DataTypeRef>( inAddedRefenrence );
		removedDataReferences = inRemovedReferences == 0 ? null : new ArrayList<String>( inRemovedReferences );
	}
	
	public STMTransaction( ISTMEntryKey inDataKey ) 
	{
		m_dataKey = Preconditions.notNull( inDataKey );
		addedDataReferences = new HashMap<String, DataTypeRef>( 8 );
		removedDataReferences = new ArrayList<String>( 8 );
	}
	
	/**
	 * @param inDataKey
	 * @param inProducer
	 */
	public STMTransaction( ISTMEntryKey inDataKey, ISTMEntryProducer inProducer, int inAddedRefenrence, int inRemovedReferences ) 
	{
		m_dataKey = Preconditions.notNull( inDataKey );
		m_producer = inProducer;
		
		addedDataReferences = inAddedRefenrence == 0 ? null : new HashMap<String, DataTypeRef>( inAddedRefenrence );
		removedDataReferences = inRemovedReferences == 0 ? null : new ArrayList<String>( inRemovedReferences );
	}
	
	/**
	 * @param inDataKey
	 * @param inProducer
	 */
	public STMTransaction( ISTMEntryKey inDataKey, ISTMEntryProducer inProducer ) 
	{
		m_dataKey = Preconditions.notNull( inDataKey );
		m_producer = inProducer;
		
		addedDataReferences = new HashMap<String, DataTypeRef>( 8 );
		removedDataReferences = new ArrayList<String>( 8 );
	}
	
	/**
	 * @param inMap
	 */
	public void putInitDataState( IPersistentMap<String, DataType<?>> inMap, Status inStatus )
	{
		m_stateInstruction = inMap;
		status = inStatus;
	}
	
	
	public abstract void execute();
	
	/**
	 * @param inKey
	 * @param inData
	 */
	public void putValue( String inKey, DataType<?> inData )
	{
		assert STMAssertionUtil.validateTransactionStack() : "Transaction.addValue was not from called from within a STM commit as required";
		assert !( inData instanceof DataTypeRef ) : "Reference values should be added via addReference method";
		
		m_stateInstruction = m_stateInstruction.assoc( inKey, inData );
		m_deltaState.add(inKey);
	}
	
	/**
	 * @param inKey
	 * @param inDataTypeRef
	 */
	public void updateReferenceValue( String inKey, DataTypeRef inDataTypeRef )
	{
		assert STMAssertionUtil.validateTransactionStack() : "Transaction.updateReferenceValue was not from called from within a STM commit as required";
		assert m_stateInstruction.valAt( inKey) != null : "Tried to update non existing Reference";
		assert m_stateInstruction.valAt( inKey ) instanceof DataTypeRef : "Tried to update Reference that was not of reference type";
		
		m_stateInstruction = m_stateInstruction.assoc( inKey, inDataTypeRef );
		m_deltaState.add(inKey);
	}
	
	/**
	 * @param inKey
	 * @param inDataRef
	 */
	public void addReference( String inKey, DataTypeRef inDataRef )
	{
		assert STMAssertionUtil.validateTransactionStack() : "Transaction.addValue was not from called from within a STM commit as required";
		
		addedDataReferences.put( inKey, inDataRef );
		
		m_stateInstruction = m_stateInstruction.assoc( inKey, inDataRef );
		m_deltaState.add(inKey);
		
		containesReferenceInstructions = true;
	}

	/**
	 * @param inKey
	 * @throws Exception
	 */
	public void removeValue( String inKey )throws Exception
	{
		assert STMAssertionUtil.validateTransactionStack() : "Transaction.removeValue was not from called from within a STM commit as required";
		assert m_deltaState.contains( inKey ) : "Transaction may not add and remove the same field value: "+inKey;
		
		DataType<?> removedData = m_stateInstruction.valAt( inKey );
		m_stateInstruction = m_stateInstruction.without( inKey );
		
		m_deltaState.add(inKey);
		
		
		if( removedData instanceof DataTypeRef )
		{
			removedDataReferences.add( inKey );
			containesReferenceInstructions = true;
		}
		
	}
	
	/**
	 * @return
	 */
	protected IPersistentMap<String, DataType<?>> getStateInstruction()
	{
		return m_stateInstruction;
	}
	
	/**
	 * @return
	 */
	protected Set<String> getDeltaState()
	{
		return m_deltaState;
	}
	
	/**
	 * @return
	 */
	protected ISTMEntryKey getDataKey()
	{
		return m_dataKey;
	}
	
	public ISTMEntryProducer producedBy()
	{
		return m_producer;
	}
	
	/**
	 * @return
	 */
	public Map<String, DataTypeRef> getAddedReferences()
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
	
	public void setStatus( Status inStatus )
	{
		putValue( DataConstants.FIELD_STATUS, new DataTypeStatus( inStatus ) );
		status = inStatus;
	}
	
	public Status getStatus()
	{
		return status;
	}
	
	public DataType<?> get( String inFieldKey )
	{
		return m_stateInstruction.valAt( inFieldKey );
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
		m_inCompleteStateTransition = true;
	}
	
	public boolean isCompleteStateTransition( )
	{
		return !m_inCompleteStateTransition;
	}
	
	public boolean containesReferenceInstructions()
	{
		return containesReferenceInstructions;
	}
}
