package moonlit.chill.ownpay.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import moonlit.chill.ownpay.async.AsyncFactory;
import moonlit.chill.ownpay.async.AsyncFactoryManager;
import moonlit.chill.ownpay.cache.TradeConfigDataCache;
import moonlit.chill.ownpay.cache.TradeMappingsDataCache;
import moonlit.chill.ownpay.cache.WaitingPayCache;
import moonlit.chill.ownpay.config.PayFactory;
import moonlit.chill.ownpay.constants.TradeResultCode;
import moonlit.chill.ownpay.exception.PayException;
import moonlit.chill.ownpay.service.PayService;
import moonlit.chill.ownpay.service.PayStrategy;
import moonlit.chill.ownpay.vo.TradeResult;
import moonlit.chill.ownpay.vo.PaySearchParam;
import moonlit.chill.ownpay.vo.PaySearchRule;
import moonlit.chill.ownpay.vo.TradeParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Gawaind
 * @date 2024/11/5 16:24
 */
@Slf4j
@Service
public class PayServiceImpl implements PayService {

    @Autowired
    private PayFactory payFactory;

    @Autowired
    private TradeConfigDataCache tradeConfigDataCache;

    @Autowired
    private TradeMappingsDataCache tradeMappingsDataCache;

    @Autowired
    private WaitingPayCache waitingPayCache;

    @Override
    public <T extends TradeParam> String pay(@Validated T param) {
        log.info("发起支付入参:{}", JSONUtil.toJsonStr(param));
        TradeResult<?> result = new TradeResult<>();
        try {
            paramHandler(param);
            PayStrategy payStrategy = payFactory.getPayStrategy(param.getPayType() + "-" + param.getPayChannel());
            if (payStrategy != null){
                //后续根据具体订单实体 获取code
                Object o = new Object();
                String code = tradeMappingsDataCache.getCode(o, param.getPayType(), param.getPayChannel());
                if (StrUtil.isEmpty(code)){
                    log.error("支付获取code为null");
                    throw new PayException("发起支付异常，请联系管理员");
                }
                tradeConfigDataCache.setCode(code);
                log.info("调用支付入参:{}", JSONUtil.toJsonStr(result));
                result = payStrategy.payMethod(param);
                log.info("发起支付返回:{}", JSONUtil.toJsonStr(param));
                if (result.isSuccess() || result.getCode().equals(TradeResultCode.PAY_USER_PAYING)){
                    AsyncFactoryManager.me().execute(AsyncFactory.initiatePaySuccessHandler(result, param));
                    return result.isSuccess() ? result.getTradeResult().toString() : "";
                } else {
                    throw new PayException(result.getMessage());
                }
            } else {
                log.error("支付获取payStrategy为null");
                throw new PayException("发起支付异常，请联系管理员");
            }
        } catch (Exception e) {
            throw new PayException(e.getMessage());
        } finally {
            tradeConfigDataCache.remove();
        }
    }

    private <T extends TradeParam> void paramHandler(T param) {
        if (param.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0){
            throw new PayException("支付金额不能小于0");
        }
        Map<String, String> map = new HashMap<>();
        map.put("payChannel", param.getPayChannel());
        param.setAdditionalInfo(map);
        //TODO 参数校验
    }
}
