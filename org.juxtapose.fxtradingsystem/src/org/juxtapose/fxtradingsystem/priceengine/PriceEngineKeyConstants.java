package org.juxtapose.fxtradingsystem.priceengine;

import org.juxtapose.fxtradingsystem.FXKeyConstants;
import org.juxtapose.fxtradingsystem.FXProducerServiceConstants;
import org.juxtapose.streamline.producer.IDataKey;
import org.juxtapose.streamline.producer.ProducerUtil;

import static org.juxtapose.fxtradingsystem.priceengine.PriceEngineDataConstants.*;

public class PriceEngineKeyConstants extends FXKeyConstants
{
	public static final IDataKey CCY_MODEL_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCYMODEL, STATE_TYPE_CCYMODEL );
	
	public static final IDataKey CCY_EUR_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_EUR );
	public static final IDataKey CCY_USD_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_USD );
	public static final IDataKey CCY_GBP_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_GBP );
	public static final IDataKey CCY_AUD_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_AUD );
	public static final IDataKey CCY_CHF_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_CHF );
	public static final IDataKey CCY_NZD_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_NZD );
	public static final IDataKey CCY_JPY_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_JPY );
	public static final IDataKey CCY_NOK_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_NOK );
	public static final IDataKey CCY_SEK_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_SEK );
	public static final IDataKey CCY_DKK_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_DKK );
	public static final IDataKey CCY_TRY_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_TRY );
	public static final IDataKey CCY_RUB_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_RUB );
	public static final IDataKey CCY_CAD_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_CAD );
	public static final IDataKey CCY_MXN_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_MXN );
	public static final IDataKey CCY_SGD_KEY = ProducerUtil.createDataKey( FXProducerServiceConstants.PRICE_ENGINE, STATE_TYPE_CCY, STATE_SGD );
}
