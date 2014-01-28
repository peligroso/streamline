package org.juxtapose.streamline.tools;


import org.juxtapose.streamline.producer.ISTMEntryKey;

import static org.juxtapose.streamline.tools.ProducerServiceConstants.*;
import static org.juxtapose.streamline.tools.STMUtil.*;

public class KeyConstants
{
	public static ISTMEntryKey PRODUCER_SERVICE_KEY = createEntryKey( STM_SERVICE_KEY, STMAssertionUtil.PRODUCER_SERVICES, STMAssertionUtil.PRODUCER_SERVICES );
}
