package org.juxtapose.fxtradingclient;

import org.juxtapose.streamline.producer.executor.BlockingQueueExecutor;
import org.juxtapose.streamline.stm.BlockingSTM;
import org.juxtapose.streamline.stm.ISTM;
import org.juxtapose.streamline.stm.STM;
import org.juxtapose.streamline.stm.STMEntryFactory;

public class STMUtil
{
	static ISTM stm;
	
	static
	{
		STM bstm = new BlockingSTM();
		STMEntryFactory entryFactory = new STMEntryFactory();
		bstm.setDataFactory( entryFactory );
		bstm.init( new BlockingQueueExecutor( 1, 1, 1, 1 ), false );
		
		stm = bstm;
	}
	
	public static synchronized ISTM getSTM()
	{
		return stm;
	}
}
