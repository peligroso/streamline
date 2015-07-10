package org.juxtapose.streamline.gwt.stm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;



/**
 * @author Pontus Jörgne
 * 20 apr 2015
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class JSSTM implements IJSSTMEntryProducerService, IJSSTMEntryProducer, IJSSTMEntrySubscriber
{
	public static final String SERVICE_ID = "STM";
	
	public static String getService( String inKey )
	{
		return inKey.split( ":" )[0];
	}
	
	private final HashMap<String, JSSTMEntry> keyToData = new HashMap<String, JSSTMEntry>();
	
	protected final HashMap<String, IJSSTMEntryProducerService> idToProducerService = new HashMap<String, IJSSTMEntryProducerService>();
	
	/**
	 * 
	 */
	public JSSTM()
	{
		JSSTMEntry emptyData = createData( JSSTMConstants.STATUS_OK, this, this );
		keyToData.put( JSSTMConstants.PRODUCER_SERVICE_KEY, emptyData);
		registerProducer( this, JSSTMConstants.STATUS_OK );
		
		JSClientConnector connector = new JSClientConnector( this );
		connector.connect();
	}
	
	/**
	 * @param inProducerService
	 * @param inStatus
	 */
	public void registerProducer( final IJSSTMEntryProducerService inProducerService, final String inStatus)
	{
		String id = inProducerService.getServiceId();
		if( idToProducerService.containsKey( id ) )
		{
			logError( "Producer "+inProducerService.getServiceId()+" already exists" );
			return;
		}
		
		idToProducerService.put( id, inProducerService );
		
		commit( new JSSTMTransaction( JSSTMConstants.PRODUCER_SERVICE_KEY, this, 0, 0, false )
		{
			@Override
			public void execute()
			{
				putValue( inProducerService.getServiceId(), inStatus );
				logInfo( "Producer "+inProducerService.getServiceId()+" registered" );
			}
		});
	}
	
	/**
	 * @param inStatus
	 * @param inProducer
	 * @param inSubscriber
	 * @return
	 */
	public JSSTMEntry createData( String inStatus, IJSSTMEntryProducer inProducer, IJSSTMEntrySubscriber inSubscriber )
	{
		HashMap<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put( JSSTMConstants.FIELD_STATUS, inStatus );
		
		ArrayList<IJSSTMEntrySubscriber> emptySubscribers = new ArrayList<IJSSTMEntrySubscriber>();
		
		return new JSSTMEntry( dataMap, new HashSet<String>(), emptySubscribers, inProducer, 0l, true );
	}
	
	public void commit( JSSTMTransaction inTransaction )
	{	
		String dataKey = inTransaction.getDataKey();
		
		JSSTMEntry newData = null;
		
		JSReferenceLink[] addedLinks = null;
		JSReferenceLink[] removedLinks = null;
//		TemporaryController[] removedDependencies = null;
		
		JSSTMEntry existingData = keyToData.get( dataKey );
		if( existingData == null )
		{
			//data has been removed due to lack of interest, transaction is discarded
			return;
		}

		if( existingData.getProducer() != inTransaction.producedBy() )
		{
			logError( "Wrong version DataProducer tried to update data: "+dataKey );
			return;
		}

		inTransaction.putInitDataState( existingData.getDataMap(), existingData.getStatus() );
		
		inTransaction.execute();
		
		if( inTransaction.isDisposed() )
		{
			return;
		}
		HashMap<String, Object> inst = inTransaction.getStateInstruction();
		Set<String> delta = inTransaction.getDeltaState();
		if( !existingData.isCompleteVersion() )
		{
			/**If previous update was a partial update we need to merge the deltas**/
			delta.addAll( existingData.getDeltaSet() );
		}
		existingData.setUpdatedData( inst, delta, inTransaction.isCompleteStateTransition() );
		
		keyToData.put( dataKey, newData );
		
		if( inTransaction.containesReferenceInstructions() )
		{
			//Init reference links
			Map< String, JSDataTypeRef > dataReferences = inTransaction.getAddedReferences();
			addedLinks = dataReferences == null ? null : new JSReferenceLink[ dataReferences.size() ];
			
			if( dataReferences != null )
			{
				IJSSTMEntryProducer producer = newData.getProducer();
				if( producer == null )
					logError( "Tried to add reference to data with null producer" );
				else
				{
					int i = 0;
					for( String fieldKey : dataReferences.keySet() )
					{
						JSDataTypeRef ref = dataReferences.get( fieldKey );
						JSReferenceLink refLink = new JSReferenceLink( producer, this, fieldKey, ref );
						producer.addDataReferences( fieldKey, refLink );
						addedLinks[i] = refLink;
						i++;
					}
				}
			}
			
			//Dispose reference links
			List< String > removedReferences = inTransaction.getRemovedReferences();
			removedLinks = removedReferences == null ? null : new JSReferenceLink[ removedReferences.size() ];

			if( removedReferences != null )
			{
				IJSSTMEntryProducer producer = newData.getProducer();
				if( producer == null )
					logError( "Tried to remove reference from data with null producer" );
				else
				{
					int i = 0;
					for( String fieldKey : removedReferences )
					{
						JSReferenceLink refLink = producer.removeReferenceLink( fieldKey );
						if( refLink == null )
						{
							logError( "Tried to remove reference Link that is not stored in the producer" );
						}
						removedLinks[i] = refLink;
						i++;
					}
				}
			}
		}
		
		if( newData != null )
		{
			/**Incomplete stateTransition does not go out as an update to subscribers**/
			if( inTransaction.isCompleteStateTransition() )
			{
				newData.updateSubscribers( dataKey, inTransaction.isFullUpdate() );
			}
		}
		
		if( inTransaction.containesReferenceInstructions() )
		{
			if( addedLinks != null )
			{
				for( JSReferenceLink link : addedLinks )
				{
					link.start();
				}
			}

			if( removedLinks != null )
			{
				for( JSReferenceLink link : removedLinks )
				{
					link.stop();
				}
			}
		}
	}
	
	public void subscribeToData( String inDataKey, IJSSTMEntrySubscriber inSubscriber )
	{
		String service = getService( inDataKey );
		IJSSTMEntryProducerService producerService = idToProducerService.get( service );
		if( producerService == null )
		{
			logError( "Key: "+inDataKey+" not valid, producer service does not exist"  );
			return;
		}
		
		IJSSTMEntryProducer producer = null;
		JSSTMEntry newData = null;
		
		JSSTMEntry existingData = keyToData.get( inDataKey );

		if( existingData == null )
		{
			//First subscriber
			producer = producerService.getDataProducer( inDataKey );

			newData = createData( JSSTMConstants.STATUS_ON_REQUEST, producer, inSubscriber);
			keyToData.put( inDataKey, newData );
		}
		else
		{
			existingData.addSubscriber( inSubscriber );
		}
		
		if( newData != null )
			inSubscriber.updateData( inDataKey, newData, true );

		if( producer != null )
			producer.init();
	}
	
	public void unsubscribeToData( String inDataKey, IJSSTMEntrySubscriber inSubscriber)
	{
		IJSSTMEntryProducerService producerService = idToProducerService.get( getService( inDataKey ) );
		if( producerService == null )
		{
			logError( "Key: "+inDataKey+" not valid, producer service does not exist"  );
			return;
		}
		
		IJSSTMEntryProducer producer = null;
		
		try
		{
			JSSTMEntry existingData = keyToData.get( inDataKey );

			if( existingData == null )
			{
				logError( "Key: "+inDataKey+", Data has already been removed which is unconditional since an existing subscriber is requesting to unsubscribe"  );
				return;
			}
			else
			{
				existingData.removeSubscriber( inSubscriber );
				if( existingData.hasSubscribers() )
				{
					//Do nothing, entry is already updated
				}
				else
				{
					keyToData.remove( inDataKey );
					producer = existingData.getProducer();
				}
			}

		}catch( Throwable t )
		{
			logError( t.getMessage() );
		}

		if( producer != null )
			producer.dispose();
		
	}

	@Override
	public String getServiceId() {
		return SERVICE_ID;
	}
	
	public void logError(String inError)
	{
		
	}
	
	public void logInfo(String inError)
	{
		
	}

	@Override
	public void updateData( String inKey, JSSTMEntry inData,
			boolean inFullUpdate ) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void referencedDataUpdated( String inFieldKey,
			JSReferenceLink inLink, JSSTMEntry inData ) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getDataKey( IJSSTMEntrySubscriber inSubscriber, String inTag, Map<String, String> inQuery ) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IJSSTMEntryProducer getDataProducer( String inDataKey ) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addDataReferences( String inFieldKey, JSReferenceLink inLink ) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSReferenceLink removeReferenceLink( String inField ) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
