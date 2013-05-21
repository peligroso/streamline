package org.juxtapose.fxtradingclient;

import java.util.HashMap;

import org.juxtapose.fxtradingsystem.constants.FXDataConstants;
import org.juxtapose.fxtradingsystem.constants.FXProducerServiceConstants;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.STMEntrySubscriber;

public class PriceSubscriber extends STMEntrySubscriber
{

	public PriceSubscriber( ISTM inSTM )
	{
		HashMap<String, String> query = new HashMap<String, String>();
		query.put( FXDataConstants.FIELD_CCY1, "EUR");
		query.put( FXDataConstants.FIELD_CCY2, "SEK");
		query.put( FXDataConstants.FIELD_INSTRUMENT, "SP");
		query.put( DataConstants.FIELD_TYPE, "P" );
		
		initialize( inSTM, query, FXProducerServiceConstants.PRICE_ENGINE );
	}

	@Override
	public void queryNotAvailible( Object inTag )
	{
		stm.logInfo( "N/A" );
	}

	@Override
	public void updateData( ISTMEntryKey inKey, ISTMEntry inData, boolean inFirstUpdate )
	{
		stm.logInfo( inData.getDataMap().toString() );
	}

}
