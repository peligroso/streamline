package org.juxtapose.fxtradingsystem.config;

import org.juxtapose.fxtradingsystem.constants.FXDataConstants;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.STMEntryProducer;
import org.juxtapose.streamline.stm.DataTransaction;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.tools.DataConstants;
import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.Status;

import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.PersistentHashMap;

public class MetaDataProducer extends STMEntryProducer
{

	public MetaDataProducer( ISTMEntryKey inKey, ISTM inSTM )
	{
		super( inKey, inSTM );
	}
	
	public ISTMEntryKey getKey()
	{
		return entryKey;
	}

	@Override
	protected void start()
	{
		stm.commit( new DataTransaction( entryKey, MetaDataProducer.this, true )
		{
			@Override
			public void execute()
			{
				PersistentArrayList<String> dateCalcConv = new PersistentArrayList<String>( new String[]{ "30_360", "30_Accual","Acctual_Acctual"} );
				putValue( "DC", dateCalcConv );
				
				//CCY
				IPersistentMap<String, Object> ccyMap = PersistentHashMap.emptyMap();
				ccyMap = ccyMap.assoc( "ISO", "" );
				ccyMap = ccyMap.assoc( "DC", "DC" );
				ccyMap = ccyMap.assoc( "NAME", "" );
				
				String[] keys = new String[]{ "ISO" };
				PersistentArrayList<String> keyList = new PersistentArrayList<String>( keys );
				ccyMap = ccyMap.assoc( DataConstants.FIELD_KEYS, keyList );
				
				IPersistentMap<String, Object> holidayMap = PersistentHashMap.emptyMap();
				holidayMap = holidayMap.assoc( "NAME", "" );
				holidayMap = holidayMap.assoc( "DATE", "DC" );
				keys = new String[]{ "NAME" };
				PersistentArrayList<String> holidayKeyList = new PersistentArrayList<String>( keys );
				holidayMap = holidayMap.assoc( DataConstants.FIELD_KEYS, holidayKeyList );
				
				ccyMap = ccyMap.assoc( "HOLIDAY", holidayMap );

				putValue( "CCY", ccyMap );
				
				//PRICE
				IPersistentMap<String, Object> priceMap = PersistentHashMap.emptyMap();
				priceMap = priceMap.assoc( FXDataConstants.FIELD_CCY1, "CCY" );
				priceMap = priceMap.assoc( FXDataConstants.FIELD_CCY2, "CCY" );
				priceMap = priceMap.assoc( FXDataConstants.FIELD_PIP, 0l );
				priceMap = priceMap.assoc( FXDataConstants.FIELD_DECIMALS, 0l );
				
				keys = new String[]{ FXDataConstants.FIELD_CCY1, FXDataConstants.FIELD_CCY2 };
				keyList = new PersistentArrayList<String>( keys );
				priceMap = priceMap.assoc( DataConstants.FIELD_KEYS, keyList );
				
				putValue( "PRC", priceMap );
				
				setStatus( Status.OK );
			}
		});

	}

}
