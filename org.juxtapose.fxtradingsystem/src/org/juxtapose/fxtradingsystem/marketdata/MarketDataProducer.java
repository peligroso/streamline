package org.juxtapose.fxtradingsystem.marketdata;

import org.juxtapose.streamline.producer.STMEntryProducer;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.executor.Executable;
import org.juxtapose.streamline.producer.executor.IExecutor;
import org.juxtapose.streamline.producer.executor.StickyRunnable;
import org.juxtapose.streamline.stm.DataTransaction;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.stm.STMTransaction;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataTypeBigDecimal;
import org.juxtapose.streamline.util.data.DataTypeLong;

/**
 * @author Pontus Jörgne
 * Jan 22, 2012
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class MarketDataProducer extends STMEntryProducer implements IMarketDataSubscriber
{
	final String source;
	final String ccy1;
	final String ccy2;
	final String period;
	
	/**
	 * @param inKey
	 * @param inSTM
	 */
	public MarketDataProducer( ISTMEntryKey inKey, ISTM inSTM )
	{
		super( inKey, inSTM );
		
		source = dataKey.getValue( MarketDataConstants.FIELD_SOURCE );
		ccy1 = dataKey.getValue( MarketDataConstants.FIELD_CCY1 );
		ccy2 = dataKey.getValue( MarketDataConstants.FIELD_CCY2 );
		period = dataKey.getValue( MarketDataConstants.FIELD_PERIOD );
	}
	@Override
	protected void start()
	{
		if( source == null || ccy1 == null || ccy2 == null || period == null )
		{
			stm.logError( "Missing required field in MarketDataProducer" );
			return;
		}
		
		try
		{
			startSubscription();
		}catch( Exception e )
		{
			stm.logError( e.getMessage(), e );
		}
	}
	
	
	public void startSubscription( ) throws Exception
	{
		QPMessage subMessage = new QPMessage( QPMessage.SUBSCRIBE, ccy1, ccy2, period);
		MarketDataSource.addSubscriber( this, subMessage, source );
	}
	
	protected void stop()
	{
		super.stop();
		MarketDataSource.removeSubscriber( this, source );
	}
	
	public void parseQuote( final QPMessage inQuote )
	{
		stm.commit( new DataTransaction(dataKey, this )
		{
			@Override
			public void execute()
			{
				putValue( MarketDataConstants.FIELD_BID, new DataTypeBigDecimal( inQuote.bid ));
				putValue( MarketDataConstants.FIELD_ASK, new DataTypeBigDecimal( inQuote.ask ));
				
				Long timeStamp = System.nanoTime();
				
				putValue( MarketDataConstants.FIELD_TIMESTAMP, new DataTypeLong( timeStamp ));
				
				if( getStatus() != Status.OK )
					setStatus( Status.OK );
			}
		});
	}
	@Override
	public void marketDataUpdated( final QPMessage inMessage, final int inHash )
	{
		stm.execute( new Executable( inHash ) {
			
			@Override
			public void run() 
			{
				parseQuote( inMessage );
			}
			
		}, getPriority() );
		
	}
	
}
