package org.juxtapose.streamline.stm;

import org.juxtapose.streamline.producer.IDataKey;
import org.juxtapose.streamline.producer.IDataProducer;
import org.juxtapose.streamline.util.IDataSubscriber;
import org.juxtapose.streamline.util.IPublishedData;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataTypeRef;

/**
 * @author Pontus Jörgne
 * Dec 30, 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 * 
 * Class to holds subscription between the referenced publishedData and the data reference.
 */
public class ReferenceLink extends DataProducerDependencyController implements IDataSubscriber
{
	private final String fieldKey;
	private final DataTypeRef ref;
	
	/**
	 * @param inParent
	 * @param inSTM
	 * @param inHashKey
	 * @param inRef
	 */
	public ReferenceLink( IDataProducer inProducer, ISTM inSTM, String inFieldKey, DataTypeRef inRef )
	{
		super( inProducer, inSTM, inRef.get() );
		fieldKey = inFieldKey;
		ref = inRef;
	}
	
	
	@Override
	public void updateData( IDataKey inKey, final IPublishedData inData, boolean inFirstUpdate )
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
			
}

