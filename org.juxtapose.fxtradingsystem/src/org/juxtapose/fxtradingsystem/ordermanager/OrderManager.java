package org.juxtapose.fxtradingsystem.ordermanager;

import static org.juxtapose.streamline.tools.STMUtil.createEntryKey;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.juxtapose.fxtradingsystem.constants.FXDataConstants;
import org.juxtapose.fxtradingsystem.constants.FXProducerServiceConstants;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ISTMEntryProducer;
import org.juxtapose.streamline.producer.ISTMEntryProducerService;
import org.juxtapose.streamline.producer.executor.Executable;
import org.juxtapose.streamline.producer.executor.IExecutor;
import org.juxtapose.streamline.stm.osgi.DataProducerService;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.tools.KeyConstants;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataTypeRef;
import org.juxtapose.streamline.util.subscriber.DataSequencer;
import org.juxtapose.streamline.util.subscriber.ISequencedDataSubscriber;

/**
 * @author Pontus Jörgne
 * Feb 26, 2012
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class OrderManager extends DataProducerService implements IOrderManager, ISTMEntryProducerService, ISequencedDataSubscriber
{
	volatile String priceKey = null;
	
	AtomicLong sequenceId = new AtomicLong(-1);
	
	final long spotPriceQueryTag = 1;
	
	ClientConnector connector;
	
	ConcurrentHashMap<Long, RFQContext> idToRFQProducer = new ConcurrentHashMap<Long, RFQContext>(512);
	
	boolean priceFromLiquidity = false;
	

	@Override
	public ISTMEntryProducer getDataProducer(ISTMEntryKey inDataKey)
	{
		if( FXDataConstants.STATE_TYPE_RFQ.equals( inDataKey.getType() ))
		{
			String val = inDataKey.getValue( FXDataConstants.FIELD_ID );
			Long id = Long.parseLong( val );
			
			RFQContext ctx = idToRFQProducer.get( id );
			
			if( ctx ==  null )
			{
				stm.logError("Could not find rfq producer for key "+inDataKey);
				return null;
			}
			
			return ctx.producer;
		}
		return null;
	}

	@Override
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData, boolean inFullUpdate )
	{
		if( inKey.equals( KeyConstants.PRODUCER_SERVICE_KEY ))
		{
			if( inData.isDeltaValue( FXProducerServiceConstants.PRICE_ENGINE ) )
			{
				Object dataValue = inData.getValue( FXProducerServiceConstants.PRICE_ENGINE );
				if( dataValue != null )
				{
					System.out.println( "Price engine is registered with status: "+dataValue);

					if( dataValue.equals( Status.OK.toString() ) )
					{
						connector = new ClientConnector( this );
					}
				}
				else
				{
					System.out.println( "Price engine is not registered");
				}
			}
		}
	}
	
	@Override
	public String getServiceId()
	{
		return FXProducerServiceConstants.ORDER_MANAGER;
	}

	@Override
	public void dataUpdated(DataSequencer inSequencer)
	{
		ISTMEntry data = inSequencer.get();
		
		processLiquidityData( data, inSequencer.getDataKey() );
		
	}
	
	private void processData( ISTMEntry inData, ISTMEntryKey inKey)
	{
		Status status = inData.getStatus();
		if( status == Status.OK )
		{
			String idStr = inKey.getValue( FXDataConstants.FIELD_ID );
			
			final Long id = Long.parseLong( idStr );
			DataTypeRef priceRef = (DataTypeRef)inData.getValue( FXDataConstants.FIELD_PRICE );
			final BigDecimal bid = (BigDecimal)priceRef.getReferenceData().getValue( FXDataConstants.FIELD_BID );
			final BigDecimal ask = (BigDecimal)priceRef.getReferenceData().getValue( FXDataConstants.FIELD_ASK );
			final BigDecimal spread = (BigDecimal)priceRef.getReferenceData().getValue( FXDataConstants.FIELD_SPREAD );
			
			final String ccy1 = (String)inData.getValue( FXDataConstants.FIELD_CCY1 );
			final String ccy2 = (String)inData.getValue( FXDataConstants.FIELD_CCY2 );
			
			Long tou = (Long)priceRef.getReferenceData().getValue( DataConstants.FIELD_TIMESTAMP );
			
			final long sequence = inData.getSequenceID();

			BigDecimal validateSpread = ask.subtract( bid );
			boolean valid = validateSpread.equals( spread );
			
			long now = System.nanoTime();
			
			final long updateProcessingTime = now-tou;
			
			final Long firstTakeTime;
			Boolean firstTake = (Boolean)inData.getValue( FXDataConstants.FIELD_FIRST_UPDATE );
			if( firstTake != null && firstTake )
			{
				RFQContext context = idToRFQProducer.get( id );

				if( context != null )
				{
					firstTakeTime = now - context.startTime;
				}
				else
					firstTakeTime = null;
			}
			else
				firstTakeTime = null;
			
			if( ! valid )
			{
				System.err.println( "Price is not valid : "+validateSpread+" != "+spread );
			}
			else
			{
//				stm.execute( new Runnable(){
//
//					@Override
//					public void run()
//					{
//						if( firstTakeTime != null )
//							System.out.println( "RoundTrip: "+firstTakeTime); 
//						System.out.println( "Price is "+bid.get().toPlainString()+" / "+ask.get().toPlainString()+" sequence "+sequence+" updatetime: "+updateProcessingTime+"   id: "+id );
//					}
//					
//				}, IExecutor.LOW );
			}
			
//			RFQMessage message = new RFQMessage( RFQMessage.TYPE_PRICING, ccy1.get(), ccy2.get(), id,  bid.get().doubleValue(), ask.get().doubleValue(), firstTakeTime, updateProcessingTime, sequence );
			
//			long start = System.nanoTime();
			
//			connector.updateRFQ( message );
			
//			long end = System.nanoTime();
//			
//			System.err.println("Time it took for price update was: "+(end-start)+" nano");
			
		}
		else
		{
//			/long sequence = inData.getSequenceID();
//			System.out.println( "PriceStatus is "+status+" "+sequence+inData.getDataMap() );
		}
	}
	
	private void processLiquidityData( ISTMEntry inData, ISTMEntryKey inKey )
	{
		Status status = inData.getStatus();
		if( status == Status.OK )
		{
			String idStr = inKey.getValue( FXDataConstants.FIELD_ID );
			
			final Long id = Long.parseLong( idStr );
			final BigDecimal bid = (BigDecimal)inData.getValue( FXDataConstants.FIELD_BID );
			final BigDecimal ask = (BigDecimal)inData.getValue( FXDataConstants.FIELD_ASK );
			
			final String ccy1 = (String)inData.getValue( FXDataConstants.FIELD_CCY1 );
			final String ccy2 = (String)inData.getValue( FXDataConstants.FIELD_CCY2 );
			
			Long tou = (Long)inData.getValue( DataConstants.FIELD_TIMESTAMP );
			
			final long sequence = inData.getSequenceID();

			long now = System.nanoTime();
			
			final long updateProcessingTime = now-tou;
			
			final Long firstTakeTime;
			Boolean firstTake = (Boolean)inData.getValue( FXDataConstants.FIELD_FIRST_UPDATE );
			if( firstTake != null && firstTake )
			{
				RFQContext context = idToRFQProducer.get( id );

				if( context != null )
				{
					firstTakeTime = now - context.startTime;
				}
				else
					firstTakeTime = null;
			}
			else
				firstTakeTime = null;
			
			
			RFQMessage message = new RFQMessage( RFQMessage.TYPE_PRICING, ccy1, ccy2, id,  bid.doubleValue(), ask.doubleValue(), firstTakeTime, updateProcessingTime, sequence, BigDecimal.ONE );
			
			
			connector.updateRFQ( message );
			
		}
	}

	/**
	 * @param inMessage
	 */
	public void sendRFQ( final RFQMessage inMessage )
	{
		stm.execute( new Executable(){

			@Override
			public void run()
			{
				long rfqID = inMessage.tag;
				
				String id = Long.toString( rfqID );
				
				ISTMEntryKey key;
				RFQLiquidityProducer producer;
				
				if( inMessage.orderType.equals( FXDataConstants.STATE_INSTRUMENT_SPOT ))
				{
					key = createEntryKey( getServiceId(), FXDataConstants.STATE_TYPE_RFQ, 
						new String[]{FXDataConstants.FIELD_ID, FXDataConstants.FIELD_CCY1, FXDataConstants.FIELD_CCY2}, 
						new String[]{id, inMessage.ccy1, inMessage.ccy2 } );
					
					producer = new RFQLiquidityProducer( key, stm, inMessage.ccy1, inMessage.ccy2, null, null, inMessage.amt );
				}
				else if( inMessage.orderType.equals( FXDataConstants.STATE_INSTRUMENT_FWD ))
				{
					key = createEntryKey( getServiceId(), FXDataConstants.STATE_TYPE_RFQ, 
							new String[]{FXDataConstants.FIELD_ID, FXDataConstants.FIELD_CCY1, FXDataConstants.FIELD_CCY2, FXDataConstants.FIELD_NEAR_SWAP}, 
							new String[]{id, inMessage.ccy1, inMessage.ccy2, inMessage.nearDate } );
					
					producer = new RFQLiquidityProducer( key, stm, inMessage.ccy1, inMessage.ccy2, inMessage.nearDate, null, inMessage.amt );
				}
				else if( inMessage.orderType.equals( FXDataConstants.STATE_INSTRUMENT_SWAP ))
				{
					key = createEntryKey( getServiceId(), FXDataConstants.STATE_TYPE_RFQ, 
							new String[]{FXDataConstants.FIELD_ID, FXDataConstants.FIELD_CCY1, FXDataConstants.FIELD_CCY2, FXDataConstants.FIELD_NEAR_SWAP, FXDataConstants.FIELD_FAR_SWAP}, 
							new String[]{id, inMessage.ccy1, inMessage.ccy2, inMessage.nearDate, inMessage.farDate } );
					
					producer = new RFQLiquidityProducer( key, stm, inMessage.ccy1, inMessage.ccy2, inMessage.nearDate, inMessage.farDate, inMessage.amt );
				}
				else
				{
					stm.logError( "Invalid RFQ order type "+inMessage.orderType );
					return;
				}
			
				DataSequencer seq = new DataSequencer( OrderManager.this, stm, key, IExecutor.HIGH );
				
				RFQContext ctx = new RFQContext( seq, producer, System.nanoTime() );
				idToRFQProducer.put( rfqID, ctx );
				
				seq.start();
			}

			@Override
			public void setHash(int inHash) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public int getHash() {
				// TODO Auto-generated method stub
				return 0;
			}
			
		}, IExecutor.HIGH );
	}
	
	/**
	 * @param inMessage
	 */
	public void sendDR( final RFQMessage inMessage )
	{
		stm.execute( new Executable(){

			@Override
			public void run()
			{
				RFQContext ctx = idToRFQProducer.remove( inMessage.tag );
				if( ctx != null )
				{
					ctx.sequencer.stop();
				}
			}
			
		}, IExecutor.HIGH );
	}
	
	@Override
	public void getDataKey(ISTMEntryRequestSubscriber inSubscriber, Object inTag, Map<String, String> inQuery)
	{
		inSubscriber.queryNotAvailible( inTag );
	}


}
