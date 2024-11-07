package moonlit.chill.ownpay.service.impl;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import moonlit.chill.ownpay.cache.TradeConfigDataCache;
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
    private TradeConfigDataCache payConfigDataCache;

    @Autowired
    private WaitingPayCache waitingPayCache;

    @Override
    public <T extends TradeParam> TradeResult<?> pay(@Validated T param) {
        log.info("发起支付入参:{}", JSONUtil.toJsonStr(param));
        TradeResult<?> result = new TradeResult<>();
        try {
            paramHandler(param);
            PayStrategy payStrategy = payFactory.getPayStrategy(param.getPayType() + "-" + param.getPayChannel());
            if (payStrategy != null){
                log.info("调用支付入参:{}", JSONUtil.toJsonStr(result));
                result = payStrategy.payMethod(param);
                log.info("发起支付返回:{}", JSONUtil.toJsonStr(param));
                if (result.isSuccess() || result.getCode().equals(TradeResultCode.PAY_USER_PAYING)){
                    initiatePaySuccessHandler(result, param);
                }
            }
        } catch (Exception e) {
            result.errorWithException(e.getMessage(), TradeResultCode.PAY_ERROR_CODE, e);
        } finally {
            payConfigDataCache.remove();
        }
        return result;
    }

    private <T extends TradeParam> void initiatePaySuccessHandler(TradeResult<?> result, T param) {
        param.setTransNum(result.getTransNum());
        if (result.isNeedSearch()){
            PaySearchParam searchParam = new PaySearchParam();
            searchParam.setStartTime(System.currentTimeMillis());
            searchParam.setPaySearchRule(PaySearchRule.LEVEL_1);
            param.setSearchParam(searchParam);
            waitingPayCache.offer(param, System.currentTimeMillis() + PaySearchRule.LEVEL_1.getIntervalTime() * 1000L);
        }
        //TODO 业务处理
    }

    private <T extends TradeParam> void paramHandler(T param) {
        if (param.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0){
            throw new PayException("支付金额不能小于0");
        }
        //TODO 参数校验
    }
}
