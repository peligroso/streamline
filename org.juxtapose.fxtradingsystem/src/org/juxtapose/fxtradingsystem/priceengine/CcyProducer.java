package org.juxtapose.fxtradingsystem.priceengine;

import static org.juxtapose.fxtradingsystem.priceengine.PriceEngineDataConstants.STATE_EUR;
import static org.juxtapose.fxtradingsystem.priceengine.PriceEngineDataConstants.STATE_USD;

import org.juxtapose.fxtradingsystem.constants.FXDataConstants;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.STMEntryProducer;
import org.juxtapose.streamline.stm.DataTransaction;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.util.Status;

/**
 * @author Pontus Jörgne
 * Dec 11, 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class CcyProducer extends STMEntryProducer
{
	final String ccy;
	
	/**
	 * @param inSTM
	 * @param inCcy
	 */
	public CcyProducer(ISTM inSTM, ISTMEntryKey inKey, String inCcy )
	{
		super( inKey, inSTM );
		ccy = inCcy;
	}
	
	@Override
	public void start()
	{
		stm.commit( new DataTransaction( entryKey, CcyProducer.this, true )
		{
			@Override
			public void execute()
			{
				setStatus( Status.OK );
				putValue(FXDataConstants.FIELD_PIP, 10000L );
				putValue(FXDataConstants.FIELD_DECIMALS, 5L );
				if( ccy.equals(STATE_EUR))
					putValue(FXDataConstants.FIELD_BASE_CCY, STATE_USD );
				else
					putValue(FXDataConstants.FIELD_BASE_CCY, STATE_EUR );
			}
		});
	}

	@Override
	public void stop()
	{
		// TODO Auto-generated method stub

	}

}
