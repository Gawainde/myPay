package moonlit.chill.ownpay.service.impl.ali;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeCreateRequest;
import com.alipay.api.response.AlipayTradeCreateResponse;
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
 * 支付宝小程序支付
 *
 * @author Gawaind
 * @date 2024/11/7 13:02
 */
@Slf4j
@Service("aliMiniPayStrategy")
public class AliMiniPayStrategyImpl implements PayStrategy {

    @Resource
    private TradeConfigDataCache tradeConfigDataCache;

    @Resource(name = "aliConfig")
    private Map<String, Object> clientMap;

    @Override
    public TradeResult<?> tradeMethod(TradeParam tradeParam) {
        log.info(String.format("支付宝小程序支付参数:%s", JSONUtil.toJsonStr(tradeParam)));
        TradeResult<String> result = new TradeResult<>();
        try {
            if (StrUtil.isEmpty(tradeParam.getMiniOpenId())) {
                result.error("支付异常，请联系管理员", TradeResultCode.PAY_ERROR_CODE);
                return result;
            }
            TradeConfig tradeConfig = tradeConfigDataCache.getTradeConfig();
            AlipayClient alipayClient = getAliPayClient();
            AlipayTradeCreateRequest request = new AlipayTradeCreateRequest();
            request.setNotifyUrl(tradeConfig.getNotifyUrl());
            JSONObject bizContent = new JSONObject();
            String outTradeNo = TransNumUtil.createTransNum(tradeParam.getPayType(), tradeParam.getPayChannel());
            result.setTransNum(outTradeNo);
            bizContent.put("out_trade_no", outTradeNo);
            bizContent.put("total_amount", tradeParam.getTotalAmount());
            bizContent.put("subject", tradeParam.getPayTitle());
            bizContent.put("product_code", "JSAPI_PAY");
            bizContent.put("buyer_id", tradeParam.getMiniOpenId());
            bizContent.put("op_app_id", tradeConfig.getMiniAppId());
            bizContent.put("body", JSONUtil.toJsonStr(tradeParam.getAdditionalInfo()));
            bizContent.put("time_expire", DateUtil.format(tradeParam.getPayDeadLine(), DatePattern.NORM_DATETIME_PATTERN));
            request.setBizContent(JSONUtil.toJsonStr(bizContent));
            if (StrUtil.isNotEmpty(tradeConfig.getAppAuthToken())) {
                request.putOtherTextParam("app_auth_token", tradeConfig.getAppAuthToken());
            }
            AlipayTradeCreateResponse response;
            response = alipayClient.certificateExecute(request);
            if (response.isSuccess()) {
                JSONObject res = new JSONObject();
                res.put("tradeNO", response.getTradeNo());
                res.put("outTradeNo", response.getOutTradeNo());
                result.setTradeResult(JSONUtil.toJsonStr(res));
            } else {
                result.error(response.getSubMsg(), TradeResultCode.PAY_FAIL_CODE);
            }
        } catch (Exception e) {
            result.errorWithException("支付失败，请重试", TradeResultCode.PAY_ERROR_CODE, e);
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
