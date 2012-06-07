package org.juxtapose.streamline.experimental.protocol.message;

import org.juxtapose.streamline.util.ISTMEntry;

public class SubscriptionEvent extends Message 
{
	long ref;
	ISTMEntry m_data;
	boolean fullUpdate;
}
