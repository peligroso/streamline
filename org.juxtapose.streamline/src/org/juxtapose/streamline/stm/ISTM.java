package org.juxtapose.streamline.stm;

import java.util.HashMap;

import org.juxtapose.streamline.producer.IDataKey;
import org.juxtapose.streamline.producer.IDataProducer;
import org.juxtapose.streamline.producer.IDataProducerService;
import org.juxtapose.streamline.producer.executor.IExecutor;
import org.juxtapose.streamline.util.IDataRequestSubscriber;
import org.juxtapose.streamline.util.IDataSubscriber;
import org.juxtapose.streamline.util.IPublishedData;
import org.juxtapose.streamline.util.Status;

public interface ISTM extends IExecutor
{
	public void registerProducer( final IDataProducerService inProducerService, final Status initState );
	public void subscribeToData( IDataKey inDataKey, IDataSubscriber inSubscriber );
	public void unsubscribeToData( IDataKey inDataKey, IDataSubscriber inSubscriber );
	public void commit( STMTransaction inTransaction );
	public void getDataKey(Integer inProducerService, IDataRequestSubscriber inSubscriber, Long inTag, HashMap<Integer, String> inQuery);
	public void logInfo( String inMessage );
	public void logError( String inMessage );
	public void logError( String inMessage, Throwable inThrowable );
	public void logWarning( String inMessage );
	public void logDebug( String inMessage );
	public IPublishedData createEmptyData( Status inStatus, IDataProducer inProducer, IDataSubscriber inSubscriber );
	public IPublishedData getData( String inKey );
}
