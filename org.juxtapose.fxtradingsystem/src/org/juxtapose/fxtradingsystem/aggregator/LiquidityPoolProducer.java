package org.juxtapose.fxtradingsystem.aggregator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.juxtapose.fxtradingsystem.BigDecimals;
import org.juxtapose.fxtradingsystem.constants.FXDataConstants;
import org.juxtapose.fxtradingsystem.constants.FXProducerServiceConstants;
import org.juxtapose.fxtradingsystem.marketdata.IMarketDataSubscriber;
import org.juxtapose.fxtradingsystem.marketdata.MarketDataConstants;
import org.juxtapose.fxtradingsystem.marketdata.MarketDataSource;
import org.juxtapose.fxtradingsystem.marketdata.QPMessage;
import org.juxtapose.fxtradingsystem.priceengine.PriceEngineDataConstants;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.STMEntryProducer;
import org.juxtapose.streamline.producer.executor.Executable;
import org.juxtapose.streamline.stm.DataTransaction;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.stm.STMTransaction;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.Status;

public class LiquidityPoolProducer extends STMEntryProducer implements IMarketDataSubscriber, ISTMEntryRequestSubscriber{

	final String source;
	final String ccy1;
	final String ccy2;
	final String period;
	
	ArrayList<ISTMEntryKey> keys = new ArrayList<ISTMEntryKey>();
	
	
	public static PriceEntryCompare priceEntryComparatorBid = new PriceEntryCompare( true );
	public static PriceEntryCompare priceEntryComparatorAsk = new PriceEntryCompare( false );
	
	public LiquidityPoolProducer(ISTMEntryKey inKey, ISTM inSTM, String inSource, String inCcy1, String inCcy2, String inPeriod) 
	{
		super(inKey, inSTM);
		
		source = inSource;
		ccy1 = inCcy1;
		ccy2 = inCcy2;
		period = inPeriod;
		
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
		if( source.equals( FXDataConstants.STATE_SOURCE_WILDCARD ))
		{
			requestSourceKey( FXDataConstants.STATE_SOURCE_REUTERS );
			requestSourceKey( FXDataConstants.STATE_SOURCE_BLOOMBERG );
			requestSourceKey( FXDataConstants.STATE_SOURCE_UBS );
			requestSourceKey( FXDataConstants.STATE_SOURCE_GOLDMAN );
		}
		else
		{
			QPMessage subMessage = new QPMessage( QPMessage.SUBSCRIBE, ccy1, ccy2, period);
			MarketDataSource.addSubscriber( this, subMessage, source );
		}
	}
	
	private void requestSourceKey( String inSource )
	{
		HashMap<String, String> query = new HashMap<String, String>();
		query.put( MarketDataConstants.FIELD_TYPE, PriceEngineDataConstants.STATE_TYPE_LIQUIDITY );
		query.put( MarketDataConstants.FIELD_CCY1, ccy1 );
		query.put( MarketDataConstants.FIELD_CCY2, ccy2 );
		query.put( MarketDataConstants.FIELD_PERIOD, FXDataConstants.STATE_PERIOD_SP );
		query.put( FXDataConstants.FIELD_SOURCE, inSource );
		query.put( FXDataConstants.FIELD_INSTRUMENT, FXDataConstants.STATE_INSTRUMENT_SPOT );
		
		stm.getDataKey( FXProducerServiceConstants.AGGREGATOR, this, inSource, query);
	}

	@Override
	public void marketDataUpdated(final QPMessage inMessage, int inHash) 
	{
		stm.execute( new Executable( inHash ) {
			
			@Override
			public void run() 
			{
				parseQuote( inMessage );
			}
			
		}, getPriority() );
	}
	
	private final void parseQuote( final QPMessage inQuote )
	{
		stm.commit( new DataTransaction(entryKey, this, true )
		{
			@Override
			public void execute()
			{
				createMessageFromQuote( inQuote, this );
				
				Long timeStamp = System.nanoTime();
				
				putValue( MarketDataConstants.FIELD_TIMESTAMP, timeStamp );
				
				if( getStatus() != Status.OK )
					setStatus( Status.OK );
			}
		});
	}
	
	private final void createMessageFromQuote( final QPMessage inMessage, DataTransaction inTransaction )
	{
		PersistentArrayList<PersistentArrayList<Object>> bidSide = createSide( inMessage.bid, true );	
		PersistentArrayList<PersistentArrayList<Object>> askSide = createSide( inMessage.ask, false );
		
		inTransaction.putValue( MarketDataConstants.FIELD_BID, bidSide );
		inTransaction.putValue( MarketDataConstants.FIELD_ASK, askSide );
		
	}
	
	private final PersistentArrayList<PersistentArrayList<Object>> createSide( Double inQuote, boolean inBid )
	{
		PersistentArrayList<PersistentArrayList<Object>> side = new PersistentArrayList<PersistentArrayList<Object>>(); 
		
		BigDecimal pips = BigDecimals.getInt( 10000 );
		
		for( int i = 1; i < 5; i++ )
		{
			PersistentArrayList<Object> entry = new PersistentArrayList<Object>();
			
			BigDecimal bid = new BigDecimal( inQuote ).setScale( 4, RoundingMode.HALF_UP );
			
			BigDecimal adjust = BigDecimals.getInt( i ).divide( pips );
			
			if( !inBid )
				adjust = adjust.multiply(BigDecimals.MINUS_ONE);
			
			adjust = adjust.setScale( 4, RoundingMode.HALF_UP );
			bid = bid.add( adjust );
			
			BigDecimal amt = new BigDecimal(1000000).multiply( new BigDecimal(i) );
			amt = amt.setScale( 1, RoundingMode.HALF_UP );
			
			entry = entry.add( bid );
			entry = entry.add( amt );
			entry = entry.add( FXDataConstants.getSourceCode( source ) );
			
			side = side.add( entry );
		}
		
		return side;
	}

	@Override
	public void deliverKey( ISTMEntryKey inDataKey, Object inTag ) 
	{
		keys.add( inDataKey );
		stm.subscribeToData( inDataKey, this );
	}

	@Override
	public void queryNotAvailible( Object  inTag ) 
	{
		stm.logError( "Could not find object for tag "+inTag);
		
	}
	
	public void updateData( ISTMEntryKey inKey, final ISTMEntry inData, boolean inFullUpdate )
	{
		if( inData.getStatus() == Status.ON_REQUEST )
			return;
		
		stm.commit( new STMTransaction( entryKey, LiquidityPoolProducer.this, inFullUpdate )
		{
			@Override
			public void execute()
			{
				putValue( DataConstants.FIELD_TIMESTAMP, inData.getValue( DataConstants.FIELD_TIMESTAMP ) );
				PersistentArrayList<PersistentArrayList<Object>> bidSide = recalculateLiquiditySide( true );
				PersistentArrayList<PersistentArrayList<Object>> askSide = recalculateLiquiditySide( false );
				
				if( bidSide == null || askSide == null )
				{
					dispose();
					return;
				}
				putValue( FXDataConstants.FIELD_BID, bidSide );
				putValue( FXDataConstants.FIELD_ASK, askSide );
				
				setStatus( Status.OK );
			}
		} );
		
	}
	
	/**
	 * @param inBid
	 * @return
	 */
	private final PersistentArrayList<PersistentArrayList<Object>> recalculateLiquiditySide( boolean inBid )
	{
		Set<PersistentArrayList<Object>> entries = new TreeSet<PersistentArrayList<Object>>( inBid ? priceEntryComparatorBid : priceEntryComparatorAsk ); 
		
		String field = inBid ? FXDataConstants.FIELD_BID : FXDataConstants.FIELD_ASK;
		
		for( ISTMEntryKey key : keys )
		{
			ISTMEntry liquidity = stm.getData( key.getKey() );
			PersistentArrayList<Object> list = (PersistentArrayList<Object>)liquidity.getValue( field );
			
			if( list == null )
				continue;
			
			for( int i =  0; i < list.size(); i++ )
			{
				PersistentArrayList<Object> entry = (PersistentArrayList<Object>)list.get( i );
				entries.add( entry );
			}
		}
		
		PersistentArrayList<PersistentArrayList<Object>> side = new PersistentArrayList<PersistentArrayList<Object>>();
		for( PersistentArrayList<Object> entry : entries )
		{
			side = side.add( entry );
		}
		
		return side;
	}
	
	protected void stop()
	{
		super.stop();
		
		for( ISTMEntryKey key : keys )
		{
			stm.unsubscribeToData( key, this );
		}
		
		if( !"*".equals( source ))
			MarketDataSource.removeSubscriber( this, source );
	}
}
