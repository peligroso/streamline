package org.juxtapose.fxtradingsystem.constants;

import java.math.BigDecimal;

import org.juxtapose.streamline.tools.DataConstants;

public class FXDataConstants extends DataConstants
{
	public static final String FIELD_CCY1 = "C1";
	public static final String FIELD_CCY2 = "C2";
	public static final String FIELD_PERIOD = "PERD";
	public static final String FIELD_BID = "BID";
	public static final String FIELD_ASK = "ASK";
	public static final String FIELD_SPREAD = "SPRD";
	public static final String FIELD_DECIMALS = "DEC";
	public static final String FIELD_PIP = "PIP";
	public static final String FIELD_BASE_CCY = "C1";
	public static final String FIELD_QUOTE_CCY = "C2";
	public static final String FIELD_STATIC_DATA = "STIC_D";
	public static final String FIELD_SOURCE = "SC";
	
	public static final String FIELD_EUR = "EUR";
	public static final String FIELD_USD = "USD";
	public static final String FIELD_GBP = "GBP";
	public static final String FIELD_AUD = "AUD";
	public static final String FIELD_CHF = "CHF";
	public static final String FIELD_NZD = "NZD";
	public static final String FIELD_JPY = "JPY";
	public static final String FIELD_NOK = "NOK";
	public static final String FIELD_SEK = "SEK";
	public static final String FIELD_DKK = "DKK";
	public static final String FIELD_TRY = "TRY";
	public static final String FIELD_RUB = "RUB";
	public static final String FIELD_CAD = "CAD";
	public static final String FIELD_MXN = "MXN";
	public static final String FIELD_SGD = "SGD";
	
	public static String STATE_PERIOD_SP = "SP";
	public static String STATE_PERIOD_1W = "1W";
	public static String STATE_PERIOD_1M = "1M";
	public static String STATE_PERIOD_3M = "3M";
	public static String STATE_PERIOD_6M = "6M";
	public static String STATE_PERIOD_9M = "9M";
	public static String STATE_PERIOD_1Y = "1Y";
	
	public static String FIELD_INSTRUMENT = "INST";
	
	public static final String FIELD_PERIOD_NEAR = "NEAR_P";
	public static final String FIELD_PERIOD_FAR = "FAR_P";
	
	public static final String FIELD_SPOT = "SP";
	public static final String FIELD_NEAR_SWAP = "N_SW";
	public static final String FIELD_FAR_SWAP = "F_SW";
	
	public static final String FIELD_PRICE = "PRC";
	
	public static final String FIELD_ID = "ID";
	
	public static final String FIELD_PRICED = "PRICED";
	
	public static final String FIELD_FIRST_UPDATE = "F_UD";
	
	public static String STATE_INSTRUMENT_SPOT = "SP";
	public static String STATE_INSTRUMENT_FWD = "FWD";
	public static String STATE_INSTRUMENT_SWAP = "SW";
	
	public static final String STATE_TYPE_RFQ = "RFQ";
	
	public static final String STATE_SOURCE_REUTERS = "REUTERS";
	public static final String STATE_SOURCE_BLOOMBERG = "BLOOMBERG";
	public static final String STATE_SOURCE_UBS = "UBS";
	public static final String STATE_SOURCE_GOLDMAN = "GOLDMAN";
	public static final String STATE_SOURCE_WILDCARD = "*";
	
	public static final BigDecimal CODE_REUTERS = BigDecimal.ONE;
	public static final BigDecimal CODE_BLOOMBERG = new BigDecimal(2);
	public static final BigDecimal CODE_UBS = new BigDecimal(3);
	public static final BigDecimal CODE_GOLDMAN = new BigDecimal(4);
	
	public static BigDecimal getSourceCode( String inSourceName )
	{
		if( STATE_SOURCE_REUTERS.equals( inSourceName ) )
			return CODE_REUTERS;
		if( STATE_SOURCE_BLOOMBERG.equals( inSourceName ) )
			return CODE_BLOOMBERG;
		if( STATE_SOURCE_UBS.equals( inSourceName ) )
			return CODE_UBS;
		if( STATE_SOURCE_GOLDMAN.equals( inSourceName ) )
			return CODE_GOLDMAN;
		
		return null;
		
	}
}
