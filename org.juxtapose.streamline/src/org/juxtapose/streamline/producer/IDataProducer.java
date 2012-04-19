package org.juxtapose.streamline.producer;

import org.juxtapose.streamline.stm.ReferenceLink;
import org.juxtapose.streamline.stm.TemporaryController;
import org.juxtapose.streamline.util.IDataSubscriber;
import org.juxtapose.streamline.util.IPublishedData;

/**
 * @author Pontus Jörgne
 * Feb 26, 2012
 * Copyright (c) Pontus Jörgne. All rights reserved
 * A producer is associated with a published data. The producer is based on a temporary controller and is started after the initial transaction.
 * The producer is stopped when the published data is disposed. If the same published data is requested again, a new instance of the producer is instantiated and started.
 */
public interface IDataProducer extends IDataSubscriber
{
	public void init();
	public void dispose();
	
	public void addDataReferences( Integer inFieldKey, ReferenceLink inLink );
	public ReferenceLink removeReferenceLink( Integer inField );
	void referencedDataUpdated( final Integer inFieldKey, final ReferenceLink inLink, final IPublishedData inData );
	
	public void addDependency( String inKey, TemporaryController inController );
	public TemporaryController removeDependency( String inDataKey );
}
