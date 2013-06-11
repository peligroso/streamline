package org.juxtapose.streamline.util.producerservices;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.STMEntryProducer;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.stm.STMTransaction;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataTypeLazyRef;

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
		stm.commit( new STMTransaction( dataKey, this, 0, 0 )
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
		stm.commit( new STMTransaction( dataKey, this, 0, 0 )
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
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.producer.STMEntryProducer#stop()
	 */
	protected void stop()
	{
		
	}

}
