package org.juxtapose.streamline.stm;

import static org.juxtapose.streamline.tools.DataConstants.FIELD_QUERY_KEY;
import static org.juxtapose.streamline.tools.STMAssertionUtil.PRODUCER_SERVICES;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.producer.ISTMEntryProducerService;
import org.juxtapose.streamline.producer.executor.IExecutable;
import org.juxtapose.streamline.producer.executor.IExecutor;
import org.juxtapose.streamline.tools.KeyConstants;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.ISTMEntrySubscriber;
import org.juxtapose.streamline.util.ISTMRequestor;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeString;
import org.juxtapose.streamline.util.net.ClientConnector;
import org.juxtapose.streamline.util.net.ServerConnector;
import org.juxtapose.streamline.util.producerservices.ProducerServiceConstants;

import com.trifork.clj_ds.IPersistentMap;
import static org.juxtapose.streamline.tools.STMMessageConstants.*;

/**
 * @author Pontus Jörgne
 * 28 jun 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 *
 *Software Transactional Memory
 *
 */
public abstract class STM implements ISTM, ISTMEntryProducerService, ISTMEntrySubscriber, ISTMEntryProducer
{
	protected final ConcurrentHashMap<String, ISTMEntry> keyToData = new ConcurrentHashMap<String, ISTMEntry>();	
	//Services that create producers to data id is service ID
	protected final ConcurrentHashMap<String, ISTMEntryProducerService> idToProducerService = new ConcurrentHashMap<String, ISTMEntryProducerService>();
	
	private IExecutor executor;
	
	private ISTMEntryFactory entryFactory;
	
	private ServerConnector serverConnector;
	private ClientConnector clientConnector;
	
	private boolean master;
	
	/**
	 * @param inExecutor
	 */
	public void init( IExecutor inExecutor, boolean inMaster )
	{
		executor = inExecutor;
		keyToData.put( KeyConstants.PRODUCER_SERVICE_KEY.getKey(), createEmptyData(Status.OK, this, this));
		registerProducer( this, Status.OK );
		
		//TICKET
		//Connector should be removed and replaced with some config
		master = inMaster;
		
		if( inMaster )
		{
			serverConnector = new ServerConnector( this, 8085 );
			serverConnector.run();
		}
		else
		{
			clientConnector = new ClientConnector( "127.0.0.1", 8085, this );
			clientConnector.run();
		}
	}
	
	/**
	 * @param inProducerService
	 * @param initState
	 */
	public void registerProducer( final ISTMEntryProducerService inProducerService, final Status initState )
	{
		String id = inProducerService.getServiceId();
		if( idToProducerService.putIfAbsent( id, inProducerService ) != null )
		{
			logError( "Producer "+inProducerService.getServiceId()+" already exists" );
			return;
		}
		commit( new STMTransaction( KeyConstants.PRODUCER_SERVICE_KEY, this, 0, 0, false )
		{
			@Override
			public void execute()
			{
				putValue( inProducerService.getServiceId(), new DataTypeString( initState.toString() ) );
				logInfo( "Producer "+inProducerService.getServiceId()+" registered" );
			}
		});
	}
	
	/**
	 * @param inProducerService
	 * @param initState
	 */
	public void updateProducerStatus( final ISTMEntryProducerService inProducerService, final Status newStatus )
	{
		commit( new STMTransaction( KeyConstants.PRODUCER_SERVICE_KEY, this, 0, 0, false )
		{
			@Override
			public void execute()
			{
				putValue( inProducerService.getServiceId(), new DataTypeString( newStatus.toString() ) );
			}
		});
	}
	
	

	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.util.producer.IDataProducerService#getServiceId()
	 */
	@Override
	public String getServiceId()
	{
		return ProducerServiceConstants.STM_SERVICE_KEY;
	}

	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.util.producer.IDataProducerService#getKey(java.util.HashMap)
	 */
	@Override
	public void getDataKey( ISTMEntryRequestSubscriber inSubscriber, Object inTag, Map<String, String> inQuery)
	{
		String val = inQuery.get( FIELD_QUERY_KEY );
		
		if( val != null && PRODUCER_SERVICES.equals( val ) )
		{
			inSubscriber.deliverKey( KeyConstants.PRODUCER_SERVICE_KEY, inTag );
		}
		else
		{
			inSubscriber.queryNotAvailible( inTag );
		}
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.exp.ISTM#getDataKey(java.lang.Integer, java.util.HashMap)
	 */
	public void getDataKey(String inProducerService, ISTMEntryRequestSubscriber inSubscriber, Object inTag, Map<String, String> inQuery)
	{
		ISTMEntryProducerService producerService = idToProducerService.get( inProducerService );
		if( producerService == null )
		{
			logError( "Producer "+inProducerService+" could not be found ");
			inSubscriber.queryNotAvailible( inTag );
		}
		producerService.getDataKey( inSubscriber, inTag, inQuery );
	}
	
	@Override
	public ISTMEntryProducer getDataProducer(ISTMEntryKey inDataKey)
	{
		return this;
	}
	
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData, boolean inFirstUpdate )
	{
		
	}
	
	public void init()
	{
		
	}
	public void dispose()
	{
		
	}
	
	/**
	 * @param inDataFactory
	 */
	public void setDataFactory( ISTMEntryFactory inDataFactory )
	{
		entryFactory = inDataFactory;
	}
	
	/**
	 * @param inStatus
	 * @param inProducer
	 * @param inSubscriber
	 * @return
	 */
	public ISTMEntry createEmptyData( Status inStatus, ISTMEntryProducer inProducer, ISTMEntrySubscriber inSubscriber )
	{
		if( entryFactory == null )
		{
			logError( "Datafactory has not been initiated" );
			System.exit(1);
		}
		return entryFactory.createData(inStatus, inProducer, inSubscriber);
	}
	
	/**
	 * @param inStatus
	 * @param inProducer
	 * @return
	 */
	protected ISTMEntry createEmptyData( Status inStatus, ISTMEntryProducer inProducer )
	{
		if( entryFactory == null )
		{
			logError( "Datafactory has not been initiated" );
			System.exit(1);
		}
		return entryFactory.createData(inStatus, inProducer );
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.exp.ISTM#logInfo(java.lang.String)
	 */
	public void logInfo( String inMessage )
	{
		System.out.println( inMessage );
	}
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.exp.ISTM#logError(java.lang.String)
	 */
	public void logError( String inMessage )
	{
		System.err.println( inMessage );
	}
	
	public void logError( String inMessage, Throwable inThrowable )
	{
		System.err.println( inMessage );
		inThrowable.printStackTrace();
	}
	
	public void logWarning( String inMessage )
	{
		System.err.println( inMessage );
	}
	
	public void logDebug( String inMessage )
	{
		System.err.println( inMessage );
	}
	
	public ISTMEntry getData( String inKey )
	{
		return keyToData.get( inKey );
	}
	
	@Override
	public void execute(IExecutable inExecutable, int inPrio)
	{
		executor.execute( inExecutable, inPrio );
	}

	@Override
	public void execute(IExecutable inExecutable, int inPrio, String inSequenceKey)
	{
		executor.execute( inExecutable, inPrio, inSequenceKey );
	}

	@Override
	public void executeBlocking(IExecutable inExecutable, int inPrio, ReentrantLock inSequenceLock)
	{
		executor.executeBlocking( inExecutable, inPrio, inSequenceLock );
	}

	@Override
	public void scheduleExecution(IExecutable inExecutable, int inPrio, long inTime, TimeUnit inTimeUnit)
	{
		executor.scheduleExecution( inExecutable, inPrio, inTime, inTimeUnit );
	}
	
	public void deliverKey( ISTMEntryKey inDataKey, Long inTag )
	{
		
	}
	public void queryNotAvailible( Long inTag )
	{
		
	}
	
	public void addDependency( String inKey, TemporaryController inController )
	{}
	public TemporaryController removeDependency( String inDataKey )
	{
		return null;
	}
	
	
	public void addDataReferences( String inFieldKey, ReferenceLink inLink ){}
	public ReferenceLink removeReferenceLink( String inField ){ return null; }
	public void disposeReferenceLinks( List< String > inReferenceFields ){}
	public void referencedDataUpdated( final String inFieldKey, final ReferenceLink inLink, final ISTMEntry inData ){}
	
	public int getPriority()
	{
		return IExecutor.LOW;
	}
	
	public void setPriority( int inPriority )
	{
		
	}
	
	@Override
	public boolean isDisposed() 
	{
		return false;
	}

	@Override
	public HashSet<TemporaryController> getDependencyControllers() 
	{
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.impl.STM#subscribe(org.juxtapose.streamline.util.producer.IDataKey, org.juxtapose.streamline.util.IDataSubscriber)
	 */
	public void subscribeToData( ISTMEntryKey inDataKey, ISTMEntrySubscriber inSubscriber )
	{
		ISTMEntryProducerService producerService = idToProducerService.get( inDataKey.getService() );
		if( producerService == null )
		{
			logError( "Key: "+inDataKey+" not valid, producer service does not exist"  );
			return;
		}
		
		ISTMEntryProducer producer = null;
		ISTMEntry newData = null;
		int newPriority = -1;
		HashSet<TemporaryController> dependencies = null;
		
		lock( inDataKey.getKey() );
		
		try
		{
			ISTMEntry existingData = keyToData.get( inDataKey.getKey() );

			if( existingData == null )
			{
				//First subscriber
				producer = producerService.getDataProducer( inDataKey );

				//REVISIT Potentially we should not notify subscribers for certain newDatas and just wait for the initial update instead.
				newData = createEmptyData( Status.ON_REQUEST, producer, inSubscriber);
				keyToData.put( inDataKey.getKey(), newData );

				if( inSubscriber.getPriority() == IExecutor.HIGH )
					producer.setPriority( IExecutor.HIGH );
			}
			else
			{
				newData = existingData.addSubscriber( inSubscriber );
				keyToData.put( inDataKey.getKey(), newData );
				
				if( newData.getPriority() != existingData.getPriority() && existingData.getPriority() != IExecutor.HIGH )
				{
					newPriority = newData.getPriority();
					newData.getProducer().setPriority( newPriority );
					dependencies = newData.getProducer().getDependencyControllers();
				}
			}

		}catch( Throwable t )
		{
			logError( t.getMessage(), t );
		}
		finally
		{
			unlock( inDataKey.getKey() );
		}
		
		if( dependencies != null )
		{
			for( TemporaryController tc : dependencies )
			{
				tc.setPriority( newPriority );
			}
		}
		
		if( newData != null )
			inSubscriber.updateData( inDataKey, newData, true );

		if( producer != null )
			producer.init();
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.exp.ISTM#unsubscribeToData(org.juxtapose.streamline.producer.IDataKey, org.juxtapose.streamline.util.IDataSubscriber)
	 */
	@Override
	public void unsubscribeToData(ISTMEntryKey inDataKey, ISTMEntrySubscriber inSubscriber)
	{
		ISTMEntryProducerService producerService = idToProducerService.get( inDataKey.getService() );
		if( producerService == null )
		{
			logError( "Key: "+inDataKey+" not valid, producer service does not exist"  );
			return;
		}
		
		ISTMEntryProducer producer = null;
		
		lock( inDataKey.getKey() );
		
		try
		{
			ISTMEntry existingData = keyToData.get( inDataKey.getKey() );

			if( existingData == null )
			{
				logError( "Key: "+inDataKey+", Data has already been removed which is unconditional since an existing subscriber is requesting to unsubscribe"  );
				return;
			}
			else
			{
				ISTMEntry newData = existingData.removeSubscriber( inSubscriber );
				if( newData.hasSubscribers() )
				{
					keyToData.replace( inDataKey.getKey(), newData );
				}
				else
				{
					keyToData.remove( inDataKey.getKey() );
					producer = existingData.getProducer();
				}
			}

		}catch( Throwable t )
		{
			logError( t.getMessage(), t );
		}
		finally
		{
			unlock( inDataKey.getKey() );
		}

		if( producer != null )
			producer.dispose();
		
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.ISTM#updateSubscriberPriority(org.juxtapose.streamline.producer.ISTMEntryKey, org.juxtapose.streamline.util.ISTMEntrySubscriber)
	 */
	public void updateSubscriberPriority( ISTMEntryKey inDataKey, ISTMEntrySubscriber inSubscriber )
	{
		lock( inDataKey.getKey() );
		
		ISTMEntryProducer producer = null;
		int newPriority = 0;
		
		HashSet<TemporaryController> dependencies = null;
		
		try
		{
			ISTMEntry existingData = keyToData.get( inDataKey.getKey() );

			if( existingData == null )
			{
				//All Subscribers has left the building
				return;
			}
			
			ISTMEntry newEntry = existingData.changeSubscriberPriority( inSubscriber, inSubscriber.getPriority() );
			
			if( newEntry == null )
			{
				//Subscriber has left the entry
				return;
			}
			
			if( existingData.getPriority() == newEntry.getPriority() )
			{
				//No side effects
				return;
			}
			
			newPriority = newEntry.getPriority();
			producer = newEntry.getProducer();
			
			if( producer == null || producer.isDisposed() )
			{
				//producer is disposed
				return;
			}
			
			dependencies = producer.getDependencyControllers();
		}
		catch( Throwable t )
		{
			logError( t.getMessage(), t );
		}
		finally
		{
			unlock( inDataKey.getKey() );
		}
		
		if( dependencies != null )
		{
			for( TemporaryController tc : dependencies )
			{
				tc.setPriority( newPriority );
			}
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.ISTM#publish(org.juxtapose.streamline.producer.ISTMEntryKey, org.juxtapose.streamline.producer.ISTMEntryProducer, org.juxtapose.streamline.util.Status, com.trifork.clj_ds.IPersistentMap, java.util.HashSet)
	 */
	public void publish( ISTMEntryKey inDataKey, ISTMEntryProducer inProducer, Status inStatus, IPersistentMap<String, DataType<?>> inData, HashSet<String> inDeltaSet )
	{
		lock( inDataKey.getKey() );
		try
		{
			ISTMEntry entry = keyToData.get( inDataKey.getKey() );

			if( entry != null )
			{
				//If data already exists it can only be modified by its own producer
				if( ! entry.getProducer().equals( inProducer ) )
				{
					logError( "Producer "+inProducer+" tried to publish a record for Key: "+inDataKey+" That is owned by producer: "+entry.getProducer() );
					return;
				}
			}
			else
			{
				entry = createEmptyData( inStatus, inProducer );
				entry = entry.addSubscriber( inProducer );
			}

			entry = entry.setUpdatedData( inData, new HashSet<String>(), true );
			
			keyToData.put( inDataKey.getKey(), entry );
		}
		catch( Throwable t )
		{
			logError( t.getMessage(), t );
		}
		finally
		{
			unlock( inDataKey.getKey() );
		}
	}
	
	public void request( String inService, int inTag, ISTMRequestor inRequestor, String inVariable, IPersistentMap<String, DataType<?>> inData )
	{
		ISTMEntryProducerService producerService = idToProducerService.get( inService );
		if( producerService == null )
		{
			logError( PRODUCER_NOT_EXISTS  );
			return;
		}
		
		producerService.request( inTag, inRequestor, inVariable, inData );
	}
	
	public void request( int inTag, ISTMRequestor inRequestor, String inVariable, IPersistentMap<String, DataType<?>> inData  )
	{
		inRequestor.requestError( inTag, REQUEST_NOT_SUPPORTED );
	}
	
	
	abstract protected void lock( String inKey );
	abstract protected void unlock( String inKey );
}
