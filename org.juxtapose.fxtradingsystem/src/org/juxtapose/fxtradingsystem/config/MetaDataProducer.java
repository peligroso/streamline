package org.juxtapose.fxtradingsystem.config;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.STMEntryProducer;
import org.juxtapose.streamline.stm.DataTransaction;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.util.PersistentArrayList;
import org.juxtapose.streamline.util.Status;
import org.juxtapose.streamline.util.data.DataType;
import org.juxtapose.streamline.util.data.DataTypeArrayList;
import org.juxtapose.streamline.util.data.DataTypeHashMap;
import org.juxtapose.streamline.util.data.DataTypeLong;
import org.juxtapose.streamline.util.data.DataTypeString;

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
		return dataKey;
	}

	@Override
	protected void start()
	{
		stm.commit( new DataTransaction( dataKey, MetaDataProducer.this )
		{
			@Override
			public void execute()
			{
				PersistentArrayList<DataTypeString> dateCalcConv = new PersistentArrayList<DataTypeString>( new DataTypeString[]{ new DataTypeString("30_360"), 
																													new DataTypeString("30_Accual"), 
																													new DataTypeString("Acctual_Acctual") } );
				putValue( "DC", new DataTypeArrayList( dateCalcConv ) );
				
				//CCY
				IPersistentMap<String, DataType<?>> ccyMap = PersistentHashMap.emptyMap();
				ccyMap = ccyMap.assoc( "ISO", new DataTypeString("") );
				ccyMap = ccyMap.assoc( "DC", new DataTypeString("DC") );
				ccyMap = ccyMap.assoc( "NAME", new DataTypeString("") );
				
				putValue( "CCY", new DataTypeHashMap(ccyMap) );
				
				//PRICE
				IPersistentMap<String, DataType<?>> priceMap = PersistentHashMap.emptyMap();
				priceMap = priceMap.assoc( "CCY1", new DataTypeString("CCY") );
				priceMap = priceMap.assoc( "CCY2", new DataTypeString("CCY2") );
				priceMap = priceMap.assoc( "PIP", new DataTypeLong(0l) );
				priceMap = priceMap.assoc( "DEC", new DataTypeLong(0l) );
				
				putValue( "PRC", new DataTypeHashMap(priceMap) );
				
				setStatus( Status.OK );
			}
		});

	}

}
