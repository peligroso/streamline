package org.juxtapose.streamline.stm;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.util.ISTMEntrySubscriber;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataTypeRef;

/**
 * @author Pontus Jörgne
 * Dec 30, 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 * 
 * Class to holds subscription between the referenced publishedData and the data reference.
 */
public class ReferenceLink extends DataProducerDependencyController implements ISTMEntrySubscriber
{
	private final String fieldKey;
	private final DataTypeRef ref;
	
	/**
	 * @param inParent
	 * @param inSTM
	 * @param inHashKey
	 * @param inRef
	 */
	protected ReferenceLink( ISTMEntryProducer inProducer, ISTM inSTM, String inFieldKey, DataTypeRef inRef )
	{
		super( inProducer, inSTM, inRef.get() );
		fieldKey = inFieldKey;
		ref = inRef;
	}
	
	
	@Override
	public void updateData( ISTMEntryKey inKey, final ISTMEntry inData, boolean inFirstUpdate )
	{
		//Notify producer about delivered Data. ON_Request Data is not interesting
		if( inData.getStatus() != Status.ON_REQUEST )
		{
			parentProducer.referencedDataUpdated( fieldKey, this, inData );
		}
	}
	
	public DataTypeRef getRef()
	{
		return ref;
	}
	
	public void priorityUpdated( int inPriority )
	{
		stm.updateSubscriberPriority( ref.get(), this );
	}
			
}

