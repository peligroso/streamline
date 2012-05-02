package org.juxtapose.fxtradingsystem.priceengine;

import static org.juxtapose.fxtradingsystem.priceengine.PriceEngineDataConstants.STATE_EUR;
import static org.juxtapose.fxtradingsystem.priceengine.PriceEngineDataConstants.STATE_SEK;

import java.util.HashMap;

import org.juxtapose.fxtradingsystem.FXProducerServiceConstants;
import org.juxtapose.streamline.producer.DataProducer;
import org.juxtapose.streamline.producer.IDataKey;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.stm.STMTransaction;
import org.juxtapose.streamline.util.IDataRequestSubscriber;
import org.juxtapose.streamline.util.IPublishedData;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataTypeRef;

public class FwdPriceProducer extends DataProducer implements IDataRequestSubscriber
{
	long spotTag = 0;
	long swapTag = 1; 
	
	IDataKey spotDataKey;
	IDataKey swapDataKey;
	
	final String ccy1;
	final String ccy2;
	final String period;
	
	public FwdPriceProducer( IDataKey inKey, String inCcy1, String inCcy2, String inPeriod, ISTM inSTM )
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
	public void updateData( IDataKey inKey, IPublishedData inData, boolean inFirstUpdate )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deliverKey(IDataKey inDataKey, Long inTag)
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
