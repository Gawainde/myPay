package moonlit.chill.ownpay.service.impl.wx;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import lombok.extern.slf4j.Slf4j;
import moonlit.chill.ownpay.cache.TradeConfigDataCache;
import moonlit.chill.ownpay.constants.TradeResultCode;
import moonlit.chill.ownpay.service.PayStrategy;
import moonlit.chill.ownpay.util.TransNumUtil;
import moonlit.chill.ownpay.vo.TradeConfig;
import moonlit.chill.ownpay.vo.TradeParam;
import moonlit.chill.ownpay.vo.TradeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 微信小程序支付
 *
 * @author Gawaind
 * @date 2024/11/7 11:31
 */
@Slf4j
@Service("wxMiniPayStrategy")
public class WxMiniPayStrategyImpl implements PayStrategy {

    @Autowired
    private TradeConfigDataCache tradeConfigDataCache;

    @Resource(name = "wxConfig")
    private Map<String, Object> configMap;

    private static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

    private Config getConfig() {
        TradeConfig config = tradeConfigDataCache.getTradeConfig();
        return (Config) configMap.get(config.getCode());
    }


    @Override
    public <T extends TradeParam> TradeResult<?> tradeMethod(T param) {
        log.info(String.format("微信小程序支付参数:%s", JSONUtil.toJsonStr(param)));
        TradeResult<String> result = new TradeResult<>();
        try {
            if (param.getMiniOpenId() == null) {
                log.error("微信小程序支付获取miniOpenId为null");
                result.error("请使用微信登录后再试", TradeResultCode.PAY_FAIL_CODE);;
                return result;
            }
            String transNum = TransNumUtil.createTransNum(param.getPayType(), param.getPayChannel());
            result.setTransNum(transNum);
            Config config = getConfig();
            TradeConfig tradeConfig = tradeConfigDataCache.getTradeConfig();
            JsapiServiceExtension service = new JsapiServiceExtension.Builder().config(config).build();
            PrepayRequest request = new PrepayRequest();
            request.setAppid(tradeConfig.getMiniAppId());
            request.setMchid(tradeConfig.getUId());
            request.setDescription(param.getPayTitle());
            request.setOutTradeNo(transNum);
            request.setTimeExpire(DateUtil.format(param.getPayDeadLine(), TIME_FORMAT));
            request.setAttach(JSONUtil.toJsonStr(param.getAdditionalInfo()));
            Amount amount = new Amount();
            amount.setTotal(param.getTotalAmount().multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).intValue());
            request.setAmount(amount);
            request.setNotifyUrl(tradeConfig.getNotifyUrl());
            Payer payer = new Payer();
            payer.setOpenid(param.getMiniOpenId());
            request.setPayer(payer);
            log.info(String.format("微信小程序发起支付参数:%s", JSONUtil.toJsonStr(request)));
            PrepayWithRequestPaymentResponse response = service.prepayWithRequestPayment(request);
            log.info(String.format("微信小程序发起支付返回:%s", JSONUtil.toJsonStr(response)));
            if (ObjectUtil.isNotEmpty(response)) {
                result.success(JSONUtil.toJsonStr(response));
            } else {
                result.error("发起支付失败，请联系管理员", TradeResultCode.PAY_FAIL_CODE);
            }
        } catch (Exception e) {
            result.errorWithException("发起支付失败，请联系管理员", TradeResultCode.PAY_ERROR_CODE, e);
        }
        return result;
    }
}
