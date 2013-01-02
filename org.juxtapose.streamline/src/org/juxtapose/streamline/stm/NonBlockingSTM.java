package org.juxtapose.streamline.stm;

import java.util.HashSet;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.producer.ISTMEntryProducerService;
import org.juxtapose.streamline.util.ISTMEntrySubscriber;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.Status;

/**
 * @author Pontus Jörgne
 * Jan 2, 2012
 * Copyright (c) Pontus Jörgne. All rights reserved
 * 
 * NonBlockngSTM is experimental ant not complete. It exhibits strange behavior and does not support DataTypeRef
 * Use BlockingSTM
 */
public class NonBlockingSTM extends STM
{
	
	public void subscribeToData( ISTMEntryKey inDataKey, ISTMEntrySubscriber inSubscriber )
	{
		ISTMEntryProducerService producerService = idToProducerService.get( inDataKey.getService() );
		if( producerService == null )
		{
			logError( "Key: "+inDataKey+" not valid, producer service does not exist"  );
			return;
		}
		
		ISTMEntry existingData = keyToData.get( inDataKey.getKey() );
		
		boolean set = false;
		do
		{
			if( existingData == null )
			{
				//First subscriber
				ISTMEntryProducer producer = producerService.getDataProducer( inDataKey );
				ISTMEntry newData = createEmptyData( Status.ON_REQUEST, producer, inSubscriber);
				
				existingData = keyToData.putIfAbsent( inDataKey.getKey(), newData );
				set = (existingData ==  null);
				
				if( set )
				{
					//Init producer
					producer.init();
				}
			}
			else
			{
				ISTMEntry newData = existingData.addSubscriber( inSubscriber );
				set = keyToData.replace( inDataKey.getKey(), existingData, newData );
				
				if( !set )
					existingData = keyToData.get( inDataKey.getKey() );
				else
					inSubscriber.updateData( inDataKey, existingData, true );
			}
		}
		while( !set );
		
	}
	
	/**
	 * @param inDataKey
	 * @param inSubscriber
	 */
	public void unsubscribeToData( ISTMEntryKey inDataKey, ISTMEntrySubscriber inSubscriber )
	{
		ISTMEntryProducerService producerService = idToProducerService.get( inDataKey.getService() );
		if( producerService == null )
		{
			logError( "Key: "+inDataKey+" not valid, producer service does not exist"  );
			return;
		}
		
		ISTMEntry existingData = keyToData.get( inDataKey.getKey() );
		
		boolean set = false;
		do
		{
			if( existingData == null )
			{
				logError( "Key: "+inDataKey+" not valid, data does not exist"  );
				return;
			}
			else
			{
				ISTMEntry newData = existingData.removeSubscriber( inSubscriber );
				if( newData.hasSubscribers() )
				{
					set = keyToData.replace( inDataKey.getKey(), existingData, newData );
				}
				else
				{
					set = keyToData.remove( inDataKey.getKey(), existingData );
					if( set )
					{
						existingData.getProducer().dispose();
					}
				}
				
				if( !set )
					existingData = keyToData.get( inDataKey.getKey() );
			}
		}
		while( !set );
	}
	
	@Override
	public void commit(STMTransaction inTransaction)
	{
		String dataKey = inTransaction.getDataKey().getKey();

		ISTMEntry existingData;
		ISTMEntry newData;

		try
		{
			do
			{
				existingData = keyToData.get( dataKey );
				if( existingData == null )
				{
					//data has been removed due to lack of interest, transaction is discarded
					return;
				}

				if( STMUtil.validateProducerToData(existingData, inTransaction) )
				{
					logError( "Wrong version DataProducer tried to update data: "+dataKey );
					//The producer for this data is of the wrong version, Transaction is discarded
					return;
				}
				
				inTransaction.putInitDataState( existingData.getDataMap(), existingData.getStatus() );
				inTransaction.execute();

				newData = existingData.setUpdatedData( inTransaction.getStateInstruction(), inTransaction.getDeltaState(), inTransaction.getStatus(), inTransaction.isCompleteStateTransition() );

			}
			while( !keyToData.replace( dataKey, existingData, newData ) );
			
			if( !inTransaction.isCompleteStateTransition() )
			{
				newData.updateSubscribers( inTransaction.getDataKey() );
			}

		}catch( Exception e){}
		
	}

	@Override
	public void updateSubscriberPriority( ISTMEntryKey inDataKey,ISTMEntrySubscriber inSubscriber ) 
	{
		
	}


}
