package org.juxtapose.streamline.stm;

import static org.juxtapose.streamline.stm.STMUtil.PRODUCER_SERVICES;
import static org.juxtapose.streamline.util.DataConstants.FIELD_QUERY_KEY;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.juxtapose.streamline.producer.IDataKey;
import org.juxtapose.streamline.producer.IDataProducer;
import org.juxtapose.streamline.producer.IDataProducerService;
import org.juxtapose.streamline.producer.executor.IExecutable;
import org.juxtapose.streamline.producer.executor.IExecutor;
import org.juxtapose.streamline.util.IDataRequestSubscriber;
import org.juxtapose.streamline.util.IDataSubscriber;
import org.juxtapose.streamline.util.IPublishedData;
import org.juxtapose.streamline.util.KeyConstants;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataTypeString;
import org.juxtapose.streamline.util.net.ServerConnector;
import org.juxtapose.streamline.util.producerservices.ProducerServiceConstants;

/**
 * @author Pontus Jörgne
 * 28 jun 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 *
 *Software Transactional Memory
 *
 */
public abstract class STM implements ISTM, IDataProducerService, IDataSubscriber, IDataProducer
{
	protected final ConcurrentHashMap<String, IPublishedData> keyToData = new ConcurrentHashMap<String, IPublishedData>();	
	//Services that create producers to data id is service ID
	protected final ConcurrentHashMap<String, IDataProducerService> idToProducerService = new ConcurrentHashMap<String, IDataProducerService>();
	
	private IExecutor executor;
	
	private IPublishedDataFactory dataFactory;
	
	private ServerConnector connector;
	
	/**
	 * @param inExecutor
	 */
	public void init( IExecutor inExecutor )
	{
		executor = inExecutor;
		keyToData.put( KeyConstants.PRODUCER_SERVICE_KEY.getKey(), createEmptyData(Status.OK, this, this));
		registerProducer( this, Status.OK );
		
//		connector = new ServerConnector( 8085 );
//		connector.run();
	}
	
	/**
	 * @param inProducerService
	 * @param initState
	 */
	public void registerProducer( final IDataProducerService inProducerService, final Status initState )
	{
		String id = inProducerService.getServiceId();
		idToProducerService.put( id, inProducerService );
		
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
	public void updateProducerStatus( final IDataProducerService inProducerService, final Status initState )
	{
		commit( new STMTransaction( KeyConstants.PRODUCER_SERVICE_KEY, this, 0, 0 )
		{
			@Override
			public void execute()
			{
				putValue( inProducerService.getServiceId(), new DataTypeString( initState.toString() ) );
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
	public void getDataKey( IDataRequestSubscriber inSubscriber, Long inTag, HashMap<String, String> inQuery)
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
	public void getDataKey(String inProducerService, IDataRequestSubscriber inSubscriber, Long inTag, HashMap<String, String> inQuery)
	{
		IDataProducerService producerService = idToProducerService.get( inProducerService );
		if( producerService == null )
		{
			logError( "Producer "+inProducerService+" could not be found ");
			inSubscriber.queryNotAvailible( inTag );
		}
		producerService.getDataKey( inSubscriber, inTag, inQuery );
	}
	
	@Override
	public IDataProducer getDataProducer(IDataKey inDataKey)
	{
		return this;
	}
	
	public void updateData( IDataKey inKey, IPublishedData inData, boolean inFirstUpdate )
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
	public IPublishedData createEmptyData( Status inStatus, IDataProducer inProducer, IDataSubscriber inSubscriber )
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
	protected IPublishedData createEmptyData( Status inStatus, IDataProducer inProducer )
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
	
	public IPublishedData getData( String inKey )
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
	
	public void deliverKey( IDataKey inDataKey, Long inTag )
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
	public void referencedDataUpdated( final String inFieldKey, final ReferenceLink inLink, final IPublishedData inData ){}
}
