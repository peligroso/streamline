package org.juxtapose.fxtradingsystem;

import org.juxtapose.streamline.util.DataConstants;

public class FXDataConstants extends DataConstants
{
	public static final int BASE = DataConstants.getBase();
	
	public static final int FIELD_CCY1 = BASE+1;
	public static final int FIELD_CCY2 = BASE+2;
	public static final int FIELD_PERIOD = BASE+3;
	public static final int FIELD_BID = BASE+4;
	public static final int FIELD_ASK = BASE+5;
	public static final int FIELD_SPREAD = BASE+6;
	public static final int FIELD_DECIMALS = BASE+7;
	public static final int FIELD_PIP = BASE+8;
	public static final int FIELD_BASE_CCY = BASE+9;
	public static final int FIELD_QUOTE_CCY = BASE+10;
	public static final int FIELD_STATIC_DATA = BASE+11;
	
	public static final int FIELD_EUR = BASE+12;
	public static final int FIELD_USD = BASE+13;
	public static final int FIELD_GBP = BASE+14;
	public static final int FIELD_AUD = BASE+15;
	public static final int FIELD_CHF = BASE+16;
	public static final int FIELD_NZD = BASE+17;
	public static final int FIELD_JPY = BASE+18;
	public static final int FIELD_NOK = BASE+19;
	public static final int FIELD_SEK = BASE+20;
	public static final int FIELD_DKK = BASE+21;
	public static final int FIELD_TRY = BASE+22;
	public static final int FIELD_RUB = BASE+23;
	public static final int FIELD_CAD = BASE+24;
	public static final int FIELD_MXN = BASE+25;
	public static final int FIELD_SGD = BASE+26;
	
	public static String STATE_PERIOD_SP = "SP";
	public static String STATE_PERIOD_1W = "1W";
	public static String STATE_PERIOD_1M = "1M";
	public static String STATE_PERIOD_3M = "3M";
	public static String STATE_PERIOD_6M = "6M";
	public static String STATE_PERIOD_9M = "9M";
	public static String STATE_PERIOD_1Y = "1Y";
	
	public static int FIELD_INSTRUMENT = BASE+27;
	
	public static final int FIELD_PERIOD_NEAR = BASE+28;
	public static final int FIELD_PERIOD_FAR = BASE+29;
	
	public static final int FIELD_SPOT = BASE+30;
	public static final int FIELD_NEAR_SWAP = BASE+31;
	public static final int FIELD_FAR_SWAP = BASE+32;
	
	public static final int FIELD_PRICE = BASE+33;
	
	public static final int FIELD_ID = BASE+34;
	
	public static final int FIELD_PRICED = BASE+35;
	
	public static final int FIELD_FIRST_UPDATE = BASE+36;
	
	public static String STATE_INSTRUMENT_SPOT = "SP";
	public static String STATE_INSTRUMENT_FWD = "FWD";
	public static String STATE_INSTRUMENT_SWAP = "SWAP";
	
	public static final String STATE_TYPE_RFQ = "RFQ";
	
	public static int getBase()
	{
		return BASE+37;
	}
}
