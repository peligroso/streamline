package org.juxtapose.streamline.stm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.juxtapose.streamline.producer.IDataKey;
import org.juxtapose.streamline.producer.IDataProducer;

/**
 * @author Pontus Jörgne
 * Feb 26, 2012
 * Copyright (c) Pontus Jörgne. All rights reserved
 * Dependency transaction is a transaction class to add Temporary controllers to the producer object.
 * Temporary controllers is usually subscription type objects that needs synchronized start/stop.
 * To add STM dependencies, the class DataProducedDependencyController should be used.
 */
public abstract class DependencyTransaction extends STMTransaction
{
	private final HashMap<String, TemporaryController> addedDependencies;
	private final List<String> removedDependencies;
	
	public DependencyTransaction( IDataKey inDataKey, IDataProducer inProducer, int inAddedDependencies, int inRemovedDependencies )
	{
		super( inDataKey, inProducer, 8, 8 );
		addedDependencies = new HashMap<String, TemporaryController>(inAddedDependencies);
		removedDependencies = new ArrayList<String>( inRemovedDependencies );
	}

	
	
	public void addDependency( String inKey, TemporaryController inController )
	{
		assert !inController.isInitiated() : "Cannot add a temporary controller that is already initiated";
		assert !inController.isDisposed() : "Cannot add a temporary controller that is already disposed";
		
		addedDependencies.put( inKey, inController );
	}
	
	public void removeDependency( String inKey )
	{
		addedDependencies.remove( inKey );
		removedDependencies.add( inKey );
	}
	
	public HashMap<String, TemporaryController> getAddedDependencies()
	{
		return addedDependencies;
	}
	
	public List<String> getRemovedDependencies()
	{
		return removedDependencies;
	}
}
