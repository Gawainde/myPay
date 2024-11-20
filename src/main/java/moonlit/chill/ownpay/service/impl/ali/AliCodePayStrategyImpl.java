package moonlit.chill.ownpay.service.impl.ali;

import cn.hutool.json.JSONUtil;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePayModel;
import com.alipay.api.request.AlipayTradePayRequest;
import com.alipay.api.response.AlipayTradePayResponse;
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
 * 支付宝被扫
 *
 * @author Gawaind
 * @date 2024/11/7 13:01
 */
@Slf4j
@Service("aliCodePayStrategy")
public class AliCodePayStrategyImpl implements PayStrategy {

    @Resource
    private TradeConfigDataCache tradeConfigDataCache;

    @Resource(name = "aliConfig")
    private Map<String, Object> clientMap;

    @Override
    public TradeResult<?> tradeMethod(TradeParam tradeParam) {
        log.info(String.format("支付宝付款码支付参数:%s", JSONUtil.toJsonStr(tradeParam)));
        TradeConfig tradeConfig = tradeConfigDataCache.getTradeConfig();
        TradeResult<String> result = new TradeResult<>();
        AlipayTradePayRequest request = new AlipayTradePayRequest();
        AlipayTradePayModel model = new AlipayTradePayModel();
        String outTradeNo = TransNumUtil.createTransNum(tradeParam.getPayType(), tradeParam.getPayChannel());
        result.setTransNum(outTradeNo);
        tradeParam.setTransNum(outTradeNo);
        model.setOutTradeNo(outTradeNo);
        model.setTotalAmount(tradeParam.getTotalAmount().toString());
        model.setScene("bar_code");
        model.setSubject(tradeParam.getPayTitle());
        model.setBody(JSONUtil.toJsonStr(tradeParam.getAdditionalInfo()));
        model.setAuthCode(tradeParam.getAuthCode());
        model.setTimeoutExpress("1m");
        request.setBizModel(model);
        request.setNotifyUrl(tradeConfig.getNotifyUrl());
        try {
            AlipayClient aliPayClient = getAliPayClient();
            AlipayTradePayResponse payResponse;
            payResponse = aliPayClient.certificateExecute(request, "GET");
            log.info(String.format("支付宝付款码支付参数:%s  返回数据:%s", JSONUtil.toJsonStr(tradeParam), JSONUtil.toJsonStr(payResponse)));
            if (payResponse.isSuccess()) {
                if ("10000".equals(payResponse.getCode())) {
                    //10000 代表成功
                    result.setTradeResult(JSONUtil.toJsonStr(payResponse));
                } else if ("10003".equals(payResponse.getCode()) || "20000".equals(payResponse.getCode())) {
                    result.error(payResponse.getSubMsg(), TradeResultCode.PAY_USER_PAYING);
                } else {
                    result.error(payResponse.getSubMsg(), TradeResultCode.PAY_FAIL_CODE);
                }
            } else {
                //支付失败
                result.error(payResponse.getSubMsg(), TradeResultCode.PAY_FAIL_CODE);
            }
        } catch (Exception e) {
            result.errorWithException(e.getMessage(), TradeResultCode.PAY_ERROR_CODE, e);
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
