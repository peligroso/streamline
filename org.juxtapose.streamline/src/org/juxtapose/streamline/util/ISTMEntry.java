package org.juxtapose.streamline.util;

import java.util.Set;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;

import com.trifork.clj_ds.IPersistentMap;


public interface ISTMEntry {

	public ISTMEntryProducer getProducer();
	
	public Object getValue( String inKey );
	public Object getUpdatedValue( String inKey );
	public boolean isDeltaValue( String inKey );
	public ISTMEntry addSubscriber(ISTMEntrySubscriber inSubscriber);
	public ISTMEntry removeSubscriber(ISTMEntrySubscriber inSubscriber);
	public boolean hasSubscribers();
	public IPersistentMap<String, Object> getDataMap();
	public ISTMEntry setUpdatedData(IPersistentMap<String, Object> stateInstruction, Set<String> deltaState, boolean completeUpdate);
	public void updateSubscribers(ISTMEntryKey dataKey, boolean inFullUpdate);
	public ISTMEntry putDataValue( String inKey, Object inValue )throws Exception;
	public Status getStatus();
	public long getSequenceID();
	public boolean isCompleteVersion();
	public Set<String> getDeltaSet();
	public int getHighPriosubscriberCount();
	public ISTMEntry changeSubscriberPriority( ISTMEntrySubscriber inSubscriber, int inNewPriority );
	public int getPriority();
}
