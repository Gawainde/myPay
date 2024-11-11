package moonlit.chill.ownpay.async.service.impl;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import moonlit.chill.ownpay.async.AsyncFactory;
import moonlit.chill.ownpay.async.AsyncFactoryManager;
import moonlit.chill.ownpay.async.service.AsyncServiceForPay;
import moonlit.chill.ownpay.cache.TradeConfigDataCache;
import moonlit.chill.ownpay.cache.TradeMappingsDataCache;
import moonlit.chill.ownpay.cache.WaitingPayCache;
import moonlit.chill.ownpay.config.PayFactory;
import moonlit.chill.ownpay.constants.PayStrategySuffix;
import moonlit.chill.ownpay.service.PayStrategy;
import moonlit.chill.ownpay.vo.PaySearchParam;
import moonlit.chill.ownpay.vo.PaySearchRule;
import moonlit.chill.ownpay.vo.TradeParam;
import moonlit.chill.ownpay.vo.TradeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author Gawaind
 * @date 2024/11/8 16:30
 */
@Slf4j
@Service
public class AsyncServiceForPayImpl implements AsyncServiceForPay {

    @Autowired
    private PayFactory payFactory;

    @Autowired
    private TradeConfigDataCache tradeConfigDataCache;

    @Autowired
    private TradeMappingsDataCache tradeMappingsDataCache;

    @Autowired
    private WaitingPayCache waitingPayCache;

    @Override
    @Async("asyncServiceExecutor")
    public <T extends TradeParam> void executeAsync(T param) {
        try {
            if (businessHandler(param)) {
                Object o = new Object();
                String code = tradeMappingsDataCache.getCode(o, param.getPayType(), param.getPayChannel());
                if (StrUtil.isEmpty(code)) {
                    log.info("当前线程:{}获取CODE为null", Thread.currentThread().getName());
                    offer(param);
                    return;
                }
                tradeConfigDataCache.setCode(code);
                PayStrategy payStrategy = payFactory.getPayStrategy(param.getPayType() + PayStrategySuffix.QUERY);
                if (payStrategy != null) {
                    TradeResult<?> result = payStrategy.payForPayQuery(param);
                    if (result.isSuccess()) {
                        AsyncFactoryManager.me().execute(AsyncFactory.querySuccess(result, param));
                    } else {
                        offer(param);
                    }
                } else {
                    log.info("当前线程:{}获取payStrategy为null", Thread.currentThread().getName());
                    offer(param);
                }
            }
        } catch (Exception e) {
            log.error("当前线程{}发起查询产生异常：{}", Thread.currentThread().getName(), e.getMessage(), e);
            offer(param);
        } finally {
            tradeConfigDataCache.remove();
        }
    }

    /**
     * 业务处理
     * @param param param
     * @return java.lang.Boolean 返回是否还需要查询
     * @author Gawaind
     * @date 2024/11/8
     */
    private <T extends TradeParam> Boolean businessHandler(T param) {
        return true;
    }

    private void offer(TradeParam param) {
        PaySearchParam search = param.getSearchParam();
        PaySearchRule rule = search.getPaySearchRule();
        int level = rule.getLevel();
        int count = rule.getCount();
        if (PaySearchRule.LEVEL_9.getLevel() > level) {
            if (search.getSearchCount() + 1 < count) {
                search.setSearchCount(search.getSearchCount() + 1);
            } else {
                search.setSearchCount(0);
                level += 1;
                search.setPaySearchRule(PaySearchRule.getCallBackTypeByLevel(level));
            }
            param.setSearchParam(search);
            waitingPayCache.offer(param, System.currentTimeMillis() + search.getPaySearchRule().getIntervalTime() * 1000L);
        }
    }
}
