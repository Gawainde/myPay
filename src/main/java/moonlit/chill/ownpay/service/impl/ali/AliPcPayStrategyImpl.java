package moonlit.chill.ownpay.service.impl.ali;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import lombok.extern.slf4j.Slf4j;
import moonlit.chill.ownpay.cache.TradeConfigDataCache;
import moonlit.chill.ownpay.constants.TradeResultCode;
import moonlit.chill.ownpay.exception.PayException;
import moonlit.chill.ownpay.service.PayStrategy;
import moonlit.chill.ownpay.util.TransNumUtil;
import moonlit.chill.ownpay.vo.TradeConfig;
import moonlit.chill.ownpay.vo.TradeParam;
import moonlit.chill.ownpay.vo.TradeResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 支付宝主扫
 *
 * @author Gawaind
 * @date 2024/11/7 13:02
 */
@Slf4j
@Service("aliPcPayStrategy")
public class AliPcPayStrategyImpl implements PayStrategy {

    @Resource
    private TradeConfigDataCache tradeConfigDataCache;

    @Resource(name = "aliConfig")
    private Map<String, Object> clientMap;

    private static final String ALI_PC_PAY_CODE = "FAST_INSTANT_TRADE_PAY";

    @Override
    public TradeResult<?> tradeMethod(TradeParam tradeParam) {
        log.info(String.format("支付宝PC支付参数:%s", JSONUtil.toJsonStr(tradeParam)));
        TradeConfig tradeConfig = tradeConfigDataCache.getTradeConfig();
        AlipayTradePagePayRequest aliPayRequest = new AlipayTradePagePayRequest();
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        TradeResult<String> result = new TradeResult<>();
        aliPayRequest.setReturnUrl(tradeConfig.getReturnUrl());
        aliPayRequest.setNotifyUrl(tradeConfig.getNotifyUrl());
        String outTradeNo = TransNumUtil.createTransNum(tradeParam.getPayType(), tradeParam.getPayChannel());
        result.setTransNum(outTradeNo);
        model.setTotalAmount(tradeParam.getTotalAmount().toString());
        model.setSubject(tradeParam.getPayTitle());
        model.setProductCode(ALI_PC_PAY_CODE);
        model.setBody(JSONUtil.toJsonStr(tradeParam.getAdditionalInfo()));
        model.setOutTradeNo(outTradeNo);
        model.setTimeExpire(DateUtil.format(tradeParam.getPayDeadLine(), DatePattern.NORM_DATETIME_PATTERN));
        aliPayRequest.setBizModel(model);
        AlipayTradePagePayResponse payResponse;
        try {
            AlipayClient aliPayClient = getAliPayClient();
            payResponse = aliPayClient.pageExecute(aliPayRequest, "GET");
            log.info(String.format("支付宝PC支付参数:%s  返回数据:%s", JSONUtil.toJsonStr(tradeParam), JSONUtil.toJsonStr(payResponse)));
            if (!payResponse.isSuccess()) {
                result.error(payResponse.getSubMsg(), TradeResultCode.PAY_FAIL_CODE);
            } else {
                result.setTradeResult(payResponse.getBody());
            }
        } catch (AlipayApiException e) {
            result.errorWithException(e.getErrMsg(), TradeResultCode.PAY_ERROR_CODE, e);
        }
        return result;
    }

    private DefaultAlipayClient getAliPayClient() {
        DefaultAlipayClient alipayClient;
        try {
            TradeConfig tradeConfig = tradeConfigDataCache.getTradeConfig();
            alipayClient = getCertClient(tradeConfig);
        } catch (Exception e) {
            log.error("支付宝获取DefaultAlipayClient异常", e);
            throw new PayException("支付异常，请联系管理员");
        }
        return alipayClient;
    }

    private DefaultAlipayClient getCertClient(TradeConfig tradeConfig) {
        return (DefaultAlipayClient) clientMap.get(tradeConfig.getCode());
    }
}
