package org.juxtapose.fxtradingsystem.priceengine;

import org.juxtapose.fxtradingsystem.FXKeyConstants;
import org.juxtapose.fxtradingsystem.FXProducerServiceConstants;
import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ProducerUtil;

import static org.juxtapose.fxtradingsystem.priceengine.PriceEngineDataConstants.*;

public class PriceEngineKeyConstants extends FXKeyConstants
{
	public static final ISTMEntryKey CCY_MODEL_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCYMODEL, STATE_TYPE_CCYMODEL );
	
	public static final ISTMEntryKey CCY_EUR_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_EUR );
	public static final ISTMEntryKey CCY_USD_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_USD );
	public static final ISTMEntryKey CCY_GBP_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_GBP );
	public static final ISTMEntryKey CCY_AUD_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_AUD );
	public static final ISTMEntryKey CCY_CHF_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_CHF );
	public static final ISTMEntryKey CCY_NZD_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_NZD );
	public static final ISTMEntryKey CCY_JPY_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_JPY );
	public static final ISTMEntryKey CCY_NOK_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_NOK );
	public static final ISTMEntryKey CCY_SEK_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_SEK );
	public static final ISTMEntryKey CCY_DKK_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_DKK );
	public static final ISTMEntryKey CCY_TRY_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_TRY );
	public static final ISTMEntryKey CCY_RUB_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_RUB );
	public static final ISTMEntryKey CCY_CAD_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_CAD );
	public static final ISTMEntryKey CCY_MXN_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_MXN );
	public static final ISTMEntryKey CCY_SGD_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_SGD );
}
