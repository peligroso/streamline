package org.juxtapose.fxtradingsystem.priceengine;

import static org.juxtapose.fxtradingsystem.priceengine.PriceEngineDataConstants.STATE_EUR;
import static org.juxtapose.fxtradingsystem.priceengine.PriceEngineDataConstants.STATE_SEK;

import java.util.HashMap;

import org.juxtapose.fxtradingsystem.FXProducerServiceConstants;
import org.juxtapose.streamline.producer.STMEntryProducer;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.stm.STMTransaction;
import org.juxtapose.streamline.util.ISTMEntryRequestSubscriber;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataTypeRef;

public class FwdPriceProducer extends STMEntryProducer implements ISTMEntryRequestSubscriber
{
	long spotTag = 0;
	long swapTag = 1; 
	
	ISTMEntryKey spotDataKey;
	ISTMEntryKey swapDataKey;
	
	final String ccy1;
	final String ccy2;
	final String period;
	
	public FwdPriceProducer( ISTMEntryKey inKey, String inCcy1, String inCcy2, String inPeriod, ISTM inSTM )
	{
		super( inKey, inSTM );
		ccy1 = inCcy1;
		ccy2 = inCcy2;
		period = inPeriod;
	}
	
	public void linkData()
	{
		HashMap<String, String> querySp = PriceEngineUtil.getSpotPriceQuery( STATE_EUR, STATE_SEK );
		stm.getDataKey( FXProducerServiceConstants.PRICE_ENGINE, this, spotTag, querySp );
		
		HashMap<String, String> querySw = PriceEngineUtil.getFwdPriceQuery( STATE_EUR, STATE_SEK, period );
		stm.getDataKey( FXProducerServiceConstants.PRICE_ENGINE, this, swapTag, querySw );
	}
	
	@Override
	protected void start()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData, boolean inFirstUpdate )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deliverKey(ISTMEntryKey inDataKey, Long inTag)
	{
		if( inTag.equals( spotTag ) )
		{
			spotDataKey = inDataKey;
		}
		else if( inTag.equals( swapDataKey ))
		{
			swapDataKey = inDataKey;
		}
		
		if( swapDataKey != null && spotDataKey != null )
		{
			stm.commit( new STMTransaction( dataKey, FwdPriceProducer.this, 2, 0 )
			{
				@Override
				public void execute()
				{
					addReference( PriceEngineDataConstants.FIELD_SPOT, new DataTypeRef( spotDataKey ) );
					addReference( PriceEngineDataConstants.FIELD_NEAR_SWAP, new DataTypeRef( swapDataKey ) );
				}
			});
		}
	}
	

	@Override
	public void queryNotAvailible(Long inTag)
	{
		setStatus( Status.ERROR );
		stm.logError( "could not retrieve datakey from market data" );
		return;
		
	}

}
