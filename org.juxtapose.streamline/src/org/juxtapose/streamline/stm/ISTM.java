package org.juxtapose.streamline.stm;

import java.util.HashSet;
import java.util.Map;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.producer.ISTMEntryProducerService;
import org.juxtapose.streamline.producer.executor.IExecutor;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.ISTMEntrySubscriber;
import org.juxtapose.streamline.util.ISTMRequestor;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;

import com.trifork.clj_ds.IPersistentMap;

public interface ISTM extends IExecutor
{
	public void registerProducer( final ISTMEntryProducerService inProducerService, final Status initState );
	public void updateProducerStatus( final ISTMEntryProducerService inProducerService, final Status newStatus );
	public void subscribeToData( ISTMEntryKey inDataKey, ISTMEntrySubscriber inSubscriber );
	public void unsubscribeToData( ISTMEntryKey inDataKey, ISTMEntrySubscriber inSubscriber );
	public void publish( ISTMEntryKey inDataKey, ISTMEntryProducer inProducer, Status inStatus, IPersistentMap<String, DataType<?>> inData, HashSet<String> inDeltaSet );
	public void commit( STMTransaction inTransaction );
	public void getDataKey(String inProducerService, ISTMEntryRequestSubscriber inSubscriber, Object inTag, Map<String, String> inQuery);
	public void logInfo( String inMessage );
	public void logError( String inMessage );
	public void logError( String inMessage, Throwable inThrowable );
	public void logWarning( String inMessage );
	public void logDebug( String inMessage );
	public ISTMEntry createEmptyData( Status inStatus, ISTMEntryProducer inProducer, ISTMEntrySubscriber inSubscriber );
	public ISTMEntry getData( String inKey );
	public void updateSubscriberPriority( ISTMEntryKey inDataKey, ISTMEntrySubscriber inSubscriber );
	public void request( String inService, int inTag, ISTMRequestor inRequestor, String inVariable, IPersistentMap<String, DataType<?>> inData );
}
