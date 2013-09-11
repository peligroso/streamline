package org.juxtapose.fxtradingsystem.ordermanager;

import java.math.BigDecimal;
import java.util.HashMap;

import org.juxtapose.fxtradingsystem.constants.FXDataConstants;
import org.juxtapose.fxtradingsystem.constants.FXProducerServiceConstants;
import org.juxtapose.fxtradingsystem.priceengine.PriceEngineUtil;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.STMEntryProducer;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.stm.ReferenceLink;
import org.juxtapose.streamline.stm.STMTransaction;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataTypeRef;

public class RFQProducer extends STMEntryProducer implements ISTMEntryRequestSubscriber
{
	final static long priceTag = 0;
	final String ccy1;
	final String ccy2;
	final String nearPeriod;
	final String farPeriod;
	final BigDecimal amt;
	
	public RFQProducer( ISTMEntryKey inKey, ISTM inSTM, String inCcy1, String inCcy2, String inNearPeriod, String inFarPeriod, BigDecimal inAmt )
	{
		super( inKey, inSTM );
		ccy1 = inCcy1;
		ccy2 = inCcy2;
		nearPeriod = inNearPeriod;
		farPeriod = inFarPeriod;
		amt = inAmt;
	}

	@Override
	protected void start()
	{
		stm.commit( new STMTransaction( entryKey, RFQProducer.this, 1, 0, true )
		{
			@Override
			public void execute()
			{
				putValue( FXDataConstants.FIELD_CCY1, ccy1 );
				putValue( FXDataConstants.FIELD_CCY2, ccy2 );
				
				if( nearPeriod != null )
					putValue( FXDataConstants.FIELD_NEAR_SWAP, nearPeriod);
				if( farPeriod != null )
					putValue( FXDataConstants.FIELD_FAR_SWAP, farPeriod);
			}
		});
		
		HashMap<String, String> query;
		
		if( nearPeriod == null && farPeriod == null )
			query = PriceEngineUtil.getSpotPriceQuery( ccy1, ccy2 );
		else if( farPeriod == null )
			query = PriceEngineUtil.getFwdPriceQuery( ccy1, ccy2, nearPeriod );
		else
			query = PriceEngineUtil.getSwapPriceQuery( ccy1, ccy2, nearPeriod, farPeriod );
		
		stm.getDataKey( FXProducerServiceConstants.PRICE_ENGINE, RFQProducer.this, priceTag, query );
	}

	@Override
	public void deliverKey( final ISTMEntryKey inDataKey, Object inTag)
	{
		if( inTag.equals( priceTag ))
		{
			stm.commit( new STMTransaction( entryKey, RFQProducer.this, 1, 0, false )
			{
				@Override
				public void execute()
				{
					addReference( FXDataConstants.FIELD_PRICE, new DataTypeRef( inDataKey ) );
				}
			});
		}
	}

	@Override
	public void queryNotAvailible(Object inTag)
	{
		setStatus( Status.NA );
		stm.logError( "could not retrieve datakey from price engine" );
		return;
	}
	
	protected void referenceDataCall( final String inFieldKey, final ReferenceLink inLink, final ISTMEntry inData, STMTransaction inTransaction )
	{
		if( inFieldKey == FXDataConstants.FIELD_PRICE )
		{
			Boolean priced = (Boolean)inTransaction.get( FXDataConstants.FIELD_FIRST_UPDATE );
			
			if( priced == null )
			{
				inTransaction.putValue( FXDataConstants.FIELD_FIRST_UPDATE, true );
			}
			else if( priced )
			{
				inTransaction.putValue( FXDataConstants.FIELD_FIRST_UPDATE, false );
			}
		}
		
		if( inTransaction.getStatus() != Status.OK )
		{
			if( inData.getStatus() == Status.OK )
			{
				inTransaction.setStatus( Status.OK );
			}
		}
	}
	

}
