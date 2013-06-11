package org.juxtapose.streamline.util.producerservices;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.STMEntryProducer;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.stm.STMTransaction;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataTypeLazyRef;

public class DataRefContainerProducer extends STMEntryProducer
{

	public DataRefContainerProducer( ISTMEntryKey inKey, ISTM inSTM )
	{
		super( inKey, inSTM );
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
			}
		});
	}
	
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

}
