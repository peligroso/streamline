package org.juxtapose.streamline.util.net;

import java.util.HashSet;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.producer.executor.IExecutor;
import org.juxtapose.streamline.stm.ReferenceLink;
import org.juxtapose.streamline.stm.TemporaryController;
import org.juxtapose.streamline.util.ISTMEntry;

public class RemoteProxyEntryProducer implements ISTMEntryProducer
{
	private final ClientConnectorHandler clientConnector;
	private int priority = IExecutor.LOW;
	
	final ISTMEntryKey key;
	
	public RemoteProxyEntryProducer( ISTMEntryKey inKey, ClientConnectorHandler inConnector )
	{
		clientConnector = inConnector;
		key = inKey;
	}
	
	@Override
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData, boolean inFirstUpdate )
	{
		throw new IllegalAccessError( "This method is not accesible for remote entry");
	}

	@Override
	public void init()
	{
		clientConnector.subscribe( inSubscriber, inKey );
	}

	@Override
	public void dispose()
	{
		throw new IllegalAccessError( "This method is not accesible for remote entry");
	}

	@Override
	public boolean isDisposed()
	{
		throw new IllegalAccessError( "This method is not accesible for remote entry");
	}

	@Override
	public void addDataReferences( String inFieldKey, ReferenceLink inLink )
	{
		throw new IllegalAccessError( "This method is not accesible for remote entry");
	}

	@Override
	public ReferenceLink removeReferenceLink( String inField )
	{
		throw new IllegalAccessError( "This method is not accesible for remote entry");
	}

	@Override
	public void referencedDataUpdated( String inFieldKey, ReferenceLink inLink, ISTMEntry inData )
	{
		throw new IllegalAccessError( "This method is not accesible for remote entry");
	}

	@Override
	public void addDependency( String inKey, TemporaryController inController )
	{
		throw new IllegalAccessError( "This method is not accesible for remote entry");
	}

	@Override
	public TemporaryController removeDependency( String inDataKey )
	{
		throw new IllegalAccessError( "This method is not accesible for remote entry");
	}

	@Override
	public void setPriority( int inPriority )
	{
		priority = inPriority;
	}

	@Override
	public int getPriority()
	{
		return priority;
	}

	@Override
	public HashSet<TemporaryController> getDependencyControllers()
	{
		return null;
	}

}
