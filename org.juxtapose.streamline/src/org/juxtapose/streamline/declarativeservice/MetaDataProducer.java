package org.juxtapose.streamline.declarativeservice;

import java.util.Iterator;
import java.util.Map.Entry;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.STMEntryProducer;
import org.juxtapose.streamline.stm.DataTransaction;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.util.Status;

import com.trifork.clj_ds.IPersistentMap;

/**
 * @author Pontus Jörgne
 * 21 dec 2013
 * Copyright (c) Pontus Jörgne. All rights reserved
 */
public class MetaDataProducer extends STMEntryProducer {

	final IPersistentMap<String, Object> entryMap;
	
	/**
	 * @param inKey
	 * @param inSTM
	 * @param inMap
	 */
	public MetaDataProducer( ISTMEntryKey inKey, ISTM inSTM, IPersistentMap<String, Object> inMap ) 
	{
		super( inKey, inSTM );
		entryMap = inMap;
	}
	
	/**
	 * @return
	 */
	public ISTMEntryKey getKey()
	{
		return entryKey;
	}

	/* (non-Javadoc)
	 * @see org.juxtapose.streamline.stm.TemporaryController#start()
	 */
	@Override
	protected void start() 
	{
		stm.commit( new DataTransaction( entryKey, MetaDataProducer.this, true )
		{
			@Override
			public void execute()
			{	
				Iterator<Entry<String, Object>> iterator =  entryMap.iterator();

				while( iterator.hasNext() )
				{
					Entry<String, Object> entry = iterator.next();
					putValue( entry.getKey(), entry.getValue() );
				}
				setStatus( Status.OK );
			}
		});
	}

}
