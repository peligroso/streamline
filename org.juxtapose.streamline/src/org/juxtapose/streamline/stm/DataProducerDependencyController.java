package org.juxtapose.streamline.stm;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.util.ISTMEntrySubscriber;
import org.juxtapose.streamline.util.ISTMEntry;

public class DataProducerDependencyController extends TemporaryController implements ISTMEntrySubscriber
{
	protected ISTMEntryKey key;
	protected final ISTM stm;
	protected final ISTMEntryProducer parentProducer;
	
	/**
	 * @param inParent
	 * @param inSTM
	 * @param inHashKey
	 * @param inRef
	 */
	public DataProducerDependencyController( ISTMEntryProducer inProducer, ISTM inSTM, ISTMEntryKey inKey )
	{
		super( inProducer.getPriority() );
		stm = inSTM;
		key = inKey;
		parentProducer = inProducer;
	}
	
	protected void start()
	{
		stm.subscribeToData( key, this );
	}
	
	@Override
	public void updateData(ISTMEntryKey inKey, final ISTMEntry inData, boolean inFirstUpdate)
	{
		parentProducer.updateData( inKey, inData, inFirstUpdate );
	}
	
	protected void stop()
	{
		stm.unsubscribeToData( key, this );
	}

	@Override
	protected void priorityUpdated(int inPriority) 
	{
		stm.updateSubscriberPriority( key, this );
	}
	
	
}
