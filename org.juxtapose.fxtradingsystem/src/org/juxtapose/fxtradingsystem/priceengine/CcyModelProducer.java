
package org.juxtapose.fxtradingsystem.priceengine;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.juxtapose.streamline.producer.STMEntryProducer;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.stm.ReferenceLink;
import org.juxtapose.streamline.stm.STMTransaction;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.util.ISTMEntry;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeRef;

/**
 * @author Pontus Jörgne
 * 17 okt 2011
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class CcyModelProducer extends STMEntryProducer
{
	/**
	 * @param inSTM
	 */
	public CcyModelProducer( ISTM inSTM )
	{
		super( PriceEngineKeyConstants.CCY_MODEL_KEY, inSTM);
	}
	
	@Override
	public void start()
	{
		stm.commit( new STMTransaction( PriceEngineKeyConstants.CCY_MODEL_KEY, CcyModelProducer.this, 2, 0, true )
		{
			@Override
			public void execute()
			{
				setStatus( Status.ON_REQUEST );
				addReference(PriceEngineDataConstants.FIELD_EUR, new DataTypeRef( PriceEngineKeyConstants.CCY_EUR_KEY ) );
				addReference(PriceEngineDataConstants.FIELD_SEK, new DataTypeRef( PriceEngineKeyConstants.CCY_SEK_KEY ) );
				addReference(PriceEngineDataConstants.FIELD_NOK, new DataTypeRef( PriceEngineKeyConstants.CCY_NOK_KEY ) );
				addReference(PriceEngineDataConstants.FIELD_DKK, new DataTypeRef( PriceEngineKeyConstants.CCY_DKK_KEY ) );
				addReference(PriceEngineDataConstants.FIELD_CHF, new DataTypeRef( PriceEngineKeyConstants.CCY_CHF_KEY ) );
				addReference(PriceEngineDataConstants.FIELD_GBP, new DataTypeRef( PriceEngineKeyConstants.CCY_GBP_KEY ) );
				addReference(PriceEngineDataConstants.FIELD_AUD, new DataTypeRef( PriceEngineKeyConstants.CCY_AUD_KEY ) );
				addReference(PriceEngineDataConstants.FIELD_NZD, new DataTypeRef( PriceEngineKeyConstants.CCY_NZD_KEY ) );
				addReference(PriceEngineDataConstants.FIELD_SGD, new DataTypeRef( PriceEngineKeyConstants.CCY_SGD_KEY ) );
				addReference(PriceEngineDataConstants.FIELD_JPY, new DataTypeRef( PriceEngineKeyConstants.CCY_JPY_KEY ) );
				addReference(PriceEngineDataConstants.FIELD_CAD, new DataTypeRef( PriceEngineKeyConstants.CCY_CAD_KEY ) );
				addReference(PriceEngineDataConstants.FIELD_RUB, new DataTypeRef( PriceEngineKeyConstants.CCY_TRY_KEY ) );
				//						addValue(FXDataConstants.CCY2, new DataTypeString(ccy2) );
				//						addValue(FXDataConstants.FIELD_SEQUENCE, new DataTypeLong(seq) );
				//						
				//						addPriceUpdate( rand, this );
			}
		});
	}
	
//	public DataTypeData getCcyData( int inCcy1 )
//	{
//		IPublishedData data = stm.createEmptyData(Status.OK, this, null);
//		data.putDataValue(inKey, inValue);
//	}

	@Override
	public void stop()
	{
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.producer.IDataProducer#referencedDataUpdated(java.lang.Integer, org.juxtapose.streamline.util.IPublishedData)
	 */
	public void postReferenceDataCall( String inFieldKey, ReferenceLink inLink, ISTMEntry inData )
	{
		checkStatus();
	}
	
	public void checkStatus()
	{
		ISTMEntry data = stm.getData( entryKey.getKey() );
		if( data != null )
		{
			if( data.getStatus() == Status.ON_REQUEST )
			{
				Iterator<Entry<String, DataType<?>>> iterator = data.getDataMap().iterator();
				
				while( iterator.hasNext() )
				{
					Entry<String, DataType<?>> entry = iterator.next();
					
					if( entry.getKey().equals( DataConstants.FIELD_STATUS ))
						continue;
					
					ISTMEntry ref = ((DataTypeRef)entry.getValue()).getReferenceData();
					
					if( ref != null )
					{
						if( ref.getStatus() != Status.OK )
							return;
					}
					else
					{
						return;
					}
				}
				
				stm.commit( new STMTransaction( PriceEngineKeyConstants.CCY_MODEL_KEY, CcyModelProducer.this, 0, 0, false )
				{
					@Override
					public void execute()
					{
						setStatus( Status.OK );
					}
				});
			}
		}
			
		
	}

}
