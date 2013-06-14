package org.juxtapose.streamline.util.producerservices;

import java.util.HashSet;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.STMEntryProducer;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.stm.STMTransaction;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeLazyRef;

import com.trifork.clj_ds.IPersistentMap;

public class DataRefContainerProducer extends STMEntryProducer
{
	final ISTMEntryKey[] startKeys;
	
	public DataRefContainerProducer( ISTMEntryKey inKey, ISTM inSTM, ISTMEntryKey... inKeys )
	{
		super( inKey, inSTM );
		startKeys = inKeys;
	}

	@Override
	protected void start()
	{
		stm.commit( new STMTransaction( entryKey, this, 0, 0 )
		{
			@Override
			public void execute()
			{
				setStatus( Status.OK );
				
				if( startKeys != null )
				{
					for( ISTMEntryKey key : startKeys )
					{
						putValue( key.toString(), new DataTypeLazyRef( key ) );
					}
				}
			}
		});
	}
	
	/**
	 * @param inKey
	 */
	protected void addReference( final ISTMEntryKey... inKey )
	{
		stm.commit( new STMTransaction( entryKey, this, 0, 0 )
		{
			@Override
			public void execute()
			{
				for( ISTMEntryKey key : inKey )
				{
					putValue( inKey.toString(), new DataTypeLazyRef( key ) );
				}
			}
		});
	}
	
	protected void addData( final ISTMEntryKey inKey, final IPersistentMap<String, DataType<?>> inData )
	{
		stm.publish( inKey, this, Status.OK, inData, new HashSet<String>() );
		addReference( entryKey );
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.producer.STMEntryProducer#stop()
	 */
	protected void stop()
	{
		
	}

}
