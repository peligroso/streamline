package org.juxtapose.streamline.util.subscriber;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.util.ISTMEntrySubscriber;
import org.juxtapose.streamline.util.ISTMEntry;

/**
 * @author Pontus Jörgne
 * Feb 2, 2012
 * Copyright (c) Pontus Jörgne. All rights reserved
 * A data sequencer is used where each data update must be handled in proper order without any misses.
 * To deal with race conditions incoming data is only redistributed to the subscriber if sequence number matches the expected sequence numner,
 * otherwise the update is put on a queue to wait until all previous updates have been processed. 
 * This implementation could be extended to use the Disruptor ringbuffer.
 */
public class DataSequencer implements ISTMEntrySubscriber
{
	ConcurrentHashMap<Long, Sequence> queue = new ConcurrentHashMap<Long, Sequence>();
	AtomicReference<Sequence> polePosition = new AtomicReference<Sequence>(Sequence.INIT_SEQUENCE);

	final ISequencedDataSubscriber subscriber;
	final ISTM stm;
	final ISTMEntryKey key;
	final int priority;

	/**
	 * @param inSubscriber
	 * @param inSTM
	 * @param inKey
	 */
	public DataSequencer( ISequencedDataSubscriber inSubscriber, ISTM inSTM, ISTMEntryKey inKey, int inPriority )
	{
		subscriber = inSubscriber;
		stm = inSTM;
		key = inKey;
		priority = inPriority;
		
	}
	
	public void start()
	{
		stm.subscribeToData( key, this );
	}
	
	public void stop()
	{
		stm.unsubscribeToData( key, this );
	}
	/**
	 * @return
	 */
	 private boolean updatePolePosition()
	 {
		 Sequence next = polePosition.get();
		 if( next.type == Sequence.TYPE_NO_OBJ )
		 {
			 Sequence nextSeq = queue.remove( next.id );
			 if( nextSeq != null )
			 {
				 return polePosition.compareAndSet( next, nextSeq );
			 }
		 }
		 return false;
	 }
	 
	 private boolean trySequence( Sequence inPoleObj, int inType, Long inID, Sequence inTrySequence )
	 {
		 if( inPoleObj.type == inType && inPoleObj.id.equals( inID ))
		 {
			 if( polePosition.compareAndSet( inPoleObj, inTrySequence ) )
			 {
				 subscriber.dataUpdated( this );
				 assert polePosition.get().type == Sequence.TYPE_NO_OBJ : "Object has not been remove from pole position";

				 while( updatePolePosition() )
				 {
					 subscriber.dataUpdated( this );
					 assert polePosition.get().type == Sequence.TYPE_NO_OBJ : "Object has not been remove from pole position";
				 }
				 return true;
			 }
		 }
		 return false;
	 }

	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.util.IDataSubscriber#updateData(java.lang.String, org.juxtapose.streamline.util.IPublishedData, boolean)
	 */
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData, boolean inFirstUpdate )
	{
		Sequence syncObj = new Sequence( inData.getSequenceID(), inData, Sequence.TYPE_OBJ);

		Sequence poleObj = polePosition.get();
		
		if( trySequence( poleObj, Sequence.TYPE_NO_OBJ, syncObj.id, syncObj ) )
			return;
		
		if( trySequence( Sequence.INIT_SEQUENCE, Sequence.TYPE_INIT, Sequence.INIT_SEQUENCE.id, syncObj ) )
			return;
		
		else
		{
			do
			{
				poleObj = polePosition.get();
				queue.remove( syncObj.id );

				if( trySequence( poleObj, Sequence.TYPE_NO_OBJ, syncObj.id, syncObj ) )
					return;
				else if( poleObj.id >= syncObj.id )
				{
					return;
				}
				else
				{
					queue.put( syncObj.id, syncObj );
				}
			}while( polePosition.get() != poleObj );
		}
	}

	 /**
	 * @return
	 */
	public ISTMEntry get()
	 {
		 Sequence ret = polePosition.get();
		 Sequence inBetweenSequence = new Sequence( ret.id+1, null, Sequence.TYPE_NO_OBJ );
		 polePosition.set( inBetweenSequence );

		 return ret.object;
	 }
	
	public ISTMEntryKey getDataKey()
	{
		return key;
	}

	@Override
	public int getPriority() 
	{
		return priority;
	}

}


