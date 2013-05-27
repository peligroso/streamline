package org.juxtapose.streamline.util.net;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.producer.STMEntryProducer;
import org.juxtapose.streamline.producer.executor.IExecutor;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.stm.ReferenceLink;
import org.juxtapose.streamline.stm.STMTransaction;
import org.juxtapose.streamline.stm.TemporaryController;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeNull;
import org.juxtapose.streamline.util.data.DataTypeRef;

import com.trifork.clj_ds.IPersistentMap;

public class RemoteProxyEntryProducer extends STMEntryProducer
{
	private final ClientConnectorHandler clientConnector;
	
	
	public RemoteProxyEntryProducer( ISTM inSTM, ISTMEntryKey inKey, ClientConnectorHandler inConnector )
	{
		super( inKey, inSTM );
		clientConnector = inConnector;
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
						if( data instanceof DataTypeRef )
						{
							addReference( entry.getKey(), (DataTypeRef)data );
						}
						else
						{
							putValue( entry.getKey(), data );
						}
					}
				}
			}
		} );
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


	@Override
	protected void start() 
	{
		clientConnector.subscribe( this, dataKey );
	}
	
	protected void stop()
	{
//		clientConnector.unSubscribe( this, dataKey );
	}

}
