package org.juxtapose.streamline.util.net;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.producer.executor.IExecutor;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.stm.ReferenceLink;
import org.juxtapose.streamline.stm.STMTransaction;
import org.juxtapose.streamline.stm.TemporaryController;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeNull;

import com.trifork.clj_ds.IPersistentMap;

public class RemoteProxyEntryProducer implements ISTMEntryProducer
{
	private final ISTM stm;
	private final ClientConnectorHandler clientConnector;
	private int priority = IExecutor.LOW;
	
	final ISTMEntryKey key;
	
	public RemoteProxyEntryProducer( ISTM inSTM, ISTMEntryKey inKey, ClientConnectorHandler inConnector )
	{
		stm = inSTM;
		clientConnector = inConnector;
		key = inKey;
	}
	
	
	/**
	 * @param inKey
	 * @param inData
	 * @param inFirstUpdate
	 */
	public void updateData( ISTMEntryKey inKey, final IPersistentMap<String, DataType<?>> inData, boolean inFirstUpdate )
	{
		stm.commit( new STMTransaction( inKey, this )
		{
			@Override
			public void execute()
			{
				Iterator<Map.Entry<String, DataType<?>>> iterator = inData.iterator();
				while( iterator.hasNext() )
				{
					Map.Entry<String, DataType<?>> entry = iterator.next();

					if( entry.getValue() instanceof DataTypeNull )
					{
						try
						{
							removeValue( entry.getKey() );
						}
						catch( Exception e )
						{
							stm.logError( e.getMessage(), e );
						}
					}
					else
					{
						DataType<?> data = entry.getValue();
						putValue( entry.getKey(), data );
					}
				}
			}
		} );
	}


	@Override
	public void init()
	{
		clientConnector.subscribe( this, key );
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


	@Override
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData,
			boolean inFirstUpdate ) {
		// TODO Auto-generated method stub
		
	}

}
