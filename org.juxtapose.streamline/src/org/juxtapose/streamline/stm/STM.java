package org.juxtapose.streamline.stm;

import static org.juxtapose.streamline.stm.STMUtil.PRODUCER_SERVICES;
import static org.juxtapose.streamline.util.DataConstants.FIELD_QUERY_KEY;

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
import org.juxtapose.streamline.stm.de.DeclarativeEntriesService;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.ISTMEntrySubscriber;
import org.juxtapose.streamline.util.KeyConstants;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataTypeString;
import org.juxtapose.streamline.util.net.ServerConnector;
import org.juxtapose.streamline.util.producerservices.ProducerServiceConstants;

/**
 * @author Pontus J�rgne
 * 28 jun 2011
 * Copyright (c) Pontus J�rgne. All rights reserved
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
	
	private IPublishedDataFactory dataFactory;
	
	private ServerConnector connector;
	
	private DeclarativeEntriesService m_deService;
	
	/**
	 * @param inExecutor
	 */
	public void init( IExecutor inExecutor )
	{
		executor = inExecutor;
		keyToData.put( KeyConstants.PRODUCER_SERVICE_KEY.getKey(), createEmptyData(Status.OK, this, this));
		registerProducer( this, Status.OK );
		
		m_deService = new DeclarativeEntriesService();
		registerProducer( m_deService, Status.INITIALIZING );
		
		m_deService.init( this );
		
		//TICKET
		//Connector should be removed and replaced with some config
		connector = new ServerConnector( this, 8085 );
		connector.run();
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
		commit( new STMTransaction( KeyConstants.PRODUCER_SERVICE_KEY, this, 0, 0 )
		{
			@Override
			public void execute()
			{
				putValue( inProducerService.getServiceId(), new DataTypeString( initState.toString() ) );
			}
		});
	}
	
	/**
	 * @param inProducerService
	 * @param initState
	 */
	public void updateProducerStatus( final ISTMEntryProducerService inProducerService, final Status newStatus )
	{
		commit( new STMTransaction( KeyConstants.PRODUCER_SERVICE_KEY, this, 0, 0 )
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
		
		if( val != null && val == PRODUCER_SERVICES )
		{
			inSubscriber.deliverKey( KeyConstants.PRODUCER_SERVICE_KEY, inTag );
		}
		inSubscriber.queryNotAvailible( inTag );
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
	public void setDataFactory( IPublishedDataFactory inDataFactory )
	{
		dataFactory = inDataFactory;
	}
	
	/**
	 * @param inStatus
	 * @param inProducer
	 * @param inSubscriber
	 * @return
	 */
	public ISTMEntry createEmptyData( Status inStatus, ISTMEntryProducer inProducer, ISTMEntrySubscriber inSubscriber )
	{
		if( dataFactory == null )
		{
			logError( "Datafactory has not been initiated" );
			System.exit(1);
		}
		return dataFactory.createData(inStatus, inProducer, inSubscriber);
	}
	
	/**
	 * @param inStatus
	 * @param inProducer
	 * @return
	 */
	protected ISTMEntry createEmptyData( Status inStatus, ISTMEntryProducer inProducer )
	{
		if( dataFactory == null )
		{
			logError( "Datafactory has not been initiated" );
			System.exit(1);
		}
		return dataFactory.createData(inStatus, inProducer );
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
}
