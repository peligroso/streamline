package org.juxtapose.fxtradingsystem.priceengine;

import static org.juxtapose.fxtradingsystem.priceengine.PriceEngineDataConstants.STATE_EUR;
import static org.juxtapose.fxtradingsystem.priceengine.PriceEngineDataConstants.STATE_USD;

import org.juxtapose.fxtradingsystem.FXDataConstants;
import org.juxtapose.streamline.producer.DataProducer;
import org.juxtapose.streamline.producer.IDataKey;
import org.juxtapose.streamline.producer.executor.IExecutor;
import org.juxtapose.streamline.stm.DataTransaction;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.stm.STMTransaction;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataTypeLong;
import org.juxtapose.streamline.util.data.DataTypeString;

/**
 * @author Pontus Jörgne
 * Dec 11, 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class CcyProducer extends DataProducer
{
	final String ccy;
	
	/**
	 * @param inSTM
	 * @param inCcy
	 */
	public CcyProducer(ISTM inSTM, IDataKey inKey, String inCcy )
	{
		super( inKey, inSTM );
		ccy = inCcy;
	}
	
	@Override
	public void start()
	{
		stm.commit( new DataTransaction( dataKey, CcyProducer.this )
		{
			@Override
			public void execute()
			{
				setStatus( Status.OK );
				putValue(FXDataConstants.FIELD_PIP, new DataTypeLong(10000L) );
				putValue(FXDataConstants.FIELD_DECIMALS, new DataTypeLong(5L) );
				if( ccy.equals(STATE_EUR))
					putValue(FXDataConstants.FIELD_BASE_CCY, new DataTypeString(STATE_USD) );
				else
					putValue(FXDataConstants.FIELD_BASE_CCY, new DataTypeString(STATE_EUR) );
			}
		});
	}

	@Override
	public void stop()
	{
		// TODO Auto-generated method stub

	}

}
