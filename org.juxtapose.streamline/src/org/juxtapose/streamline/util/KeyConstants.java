package org.juxtapose.streamline.util;

import static org.juxtapose.streamline.util.producerservices.ProducerServiceConstants.*;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ProducerUtil;
import org.juxtapose.streamline.stm.STMUtil;

public class KeyConstants
{
	public static ISTMEntryKey PRODUCER_SERVICE_KEY = ProducerUtil.createDataKey( STM_SERVICE_KEY, STMUtil.PRODUCER_SERVICES, STMUtil.PRODUCER_SERVICES );
}
