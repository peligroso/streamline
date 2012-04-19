package org.juxtapose.fxtradingsystem.marketdata;

public class QPMessage
{
	public static String RECIEVER_PREFIX = "SUBSCRIBE_";
	public static String SENDER_PREFIX = "PUBLISH_";
	
	public static String SUBSCRIBE = "SUBSCRIBE";
	public static String UNSUBSCRIBE = "UNSUBSCRIBE";
	public static String QUOTE = "QUOTE";
	
	public final String ccy1;
	public final String ccy2;
	public final String period;
	
	public final String type;
	
	public final Double bid;
	public final Double ask;
		
	public QPMessage( String inType, String inCcy1, String inCcy2, String inPeriod )
	{
		type = inType;
		ccy1 = inCcy1;
		ccy2 = inCcy2;
		period = inPeriod;
		bid = null;
		ask = null;
	}
	
	public QPMessage( String inType, String inCcy1, String inCcy2, String inPeriod, Double inBid, Double inAsk )
	{
		type = inType;
		ccy1 = inCcy1;
		ccy2 = inCcy2;
		period = inPeriod;
		bid = inBid;
		ask = inAsk;
	}
	
	public QPMessage( String inMessage )
	{
		String[] split = inMessage.split( ":" );
		type = split[0];
		ccy1 = split[1];
		ccy2 = split[2];
		period = split[3];
		
		if( split.length > 4 )
		{
			bid = Double.parseDouble( split[4] );
			ask = Double.parseDouble( split[5] );
		}
		else
		{
			bid = null;
			ask = null;
		}
	}
	
	public String toString()
	{
		if( bid == null && ask == null )
			return type+":"+ccy1+":"+ccy2+":"+period;
		else
			return type+":"+ccy1+":"+ccy2+":"+period+":"+bid+":"+ask;
	}
}
