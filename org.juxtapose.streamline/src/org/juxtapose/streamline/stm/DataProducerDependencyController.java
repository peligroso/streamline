package org.juxtapose.streamline.stm;

import org.juxtapose.streamline.producer.IDataKey;
import org.juxtapose.streamline.producer.IDataProducer;
import org.juxtapose.streamline.util.IDataSubscriber;
import org.juxtapose.streamline.util.IPublishedData;

public class DataProducerDependencyController extends TemporaryController implements IDataSubscriber
{
	protected IDataKey key;
	protected final ISTM stm;
	protected final IDataProducer parentProducer;
	
	/**
	 * @param inParent
	 * @param inSTM
	 * @param inHashKey
	 * @param inRef
	 */
	public DataProducerDependencyController( IDataProducer inProducer, ISTM inSTM, IDataKey inKey )
	{
		stm = inSTM;
		key = inKey;
		parentProducer = inProducer;
	}
	
	protected void start()
	{
		stm.subscribeToData( key, this );
	}
	
	@Override
	public void updateData(IDataKey inKey, final IPublishedData inData, boolean inFirstUpdate)
	{
		parentProducer.updateData( inKey, inData, inFirstUpdate );
	}
	
	protected void stop()
	{
		stm.unsubscribeToData( key, this );
	}
}
