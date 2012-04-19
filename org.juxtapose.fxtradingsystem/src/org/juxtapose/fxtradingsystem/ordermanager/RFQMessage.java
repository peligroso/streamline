package org.juxtapose.fxtradingsystem.ordermanager;

public class RFQMessage
{
	public static final int TYPE_NEW_REQUEST = 0;
	public static final int TYPE_DR = 1;
	public static final int TYPE_PRICING = 2;
	
	public final int messageType;
	public final String orderType;
	public final String ccy1;
	public final String ccy2;
	public final String nearDate;
	public final String farDate;
	
	public final Double bidPrice;
	public final Double askPrice;
	
	public final long tag;
	
	public final Long firstTakeTime;
	public final Long updateTime;
	
	public final long sequence;
	
	public RFQMessage( String inCcy1, String inCcy2, String inOrderType, String inNearDate, String inFarDate, long inTag )
	{
		messageType = TYPE_NEW_REQUEST;
		
		ccy1 = inCcy1;
		ccy2 = inCcy2;
		orderType = inOrderType;
		
		nearDate = inNearDate;
		farDate =inFarDate;
		
		bidPrice = null;
		askPrice = null;
		
		tag = inTag;
		
		firstTakeTime = null;
		updateTime = null;
		
		sequence = 0;
	}
	
	public RFQMessage( int inMessageType, String inCcy1, String inCcy2, long inTag, Double inBidPrice, Double inAskPrice, Long inFirstTakeTime, Long inUpdateTime, long inSequence )
	{
		messageType = inMessageType;
		
		ccy1 = inCcy1;
		ccy2 = inCcy2;
		orderType = null;
		
		nearDate = null;
		farDate = null;
		
		bidPrice = inBidPrice;
		askPrice = inAskPrice;
		
		tag = inTag;
		
		firstTakeTime = inFirstTakeTime;
		updateTime = inUpdateTime;
		
		sequence = inSequence;
	}
	
}
