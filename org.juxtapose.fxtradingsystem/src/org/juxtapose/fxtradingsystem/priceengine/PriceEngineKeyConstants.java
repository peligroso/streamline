package org.juxtapose.fxtradingsystem.priceengine;

import org.juxtapose.fxtradingsystem.constants.FXKeyConstants;
import org.juxtapose.fxtradingsystem.constants.FXProducerServiceConstants;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import static org.juxtapose.streamline.tools.STMUtil.*;

import static org.juxtapose.fxtradingsystem.priceengine.PriceEngineDataConstants.*;

public class PriceEngineKeyConstants extends FXKeyConstants
{
	public static final ISTMEntryKey CCY_MODEL_KEY = createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCYMODEL, STATE_TYPE_CCYMODEL );
	
	public static final ISTMEntryKey CCY_EUR_KEY = createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_EUR );
	public static final ISTMEntryKey CCY_USD_KEY = createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_USD );
	public static final ISTMEntryKey CCY_GBP_KEY = createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_GBP );
	public static final ISTMEntryKey CCY_AUD_KEY = createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_AUD );
	public static final ISTMEntryKey CCY_CHF_KEY = createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_CHF );
	public static final ISTMEntryKey CCY_NZD_KEY = createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_NZD );
	public static final ISTMEntryKey CCY_JPY_KEY = createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_JPY );
	public static final ISTMEntryKey CCY_NOK_KEY = createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_NOK );
	public static final ISTMEntryKey CCY_SEK_KEY = createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_SEK );
	public static final ISTMEntryKey CCY_DKK_KEY = createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_DKK );
	public static final ISTMEntryKey CCY_TRY_KEY = createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_TRY );
	public static final ISTMEntryKey CCY_RUB_KEY = createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_RUB );
	public static final ISTMEntryKey CCY_CAD_KEY = createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_CAD );
	public static final ISTMEntryKey CCY_MXN_KEY = createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_MXN );
	public static final ISTMEntryKey CCY_SGD_KEY = createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_SGD );
}
