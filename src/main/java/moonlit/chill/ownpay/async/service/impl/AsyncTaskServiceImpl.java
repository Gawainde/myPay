package moonlit.chill.ownpay.async.service.impl;

import moonlit.chill.ownpay.async.service.AsyncTaskService;
import moonlit.chill.ownpay.cache.WaitingPayCache;
import moonlit.chill.ownpay.vo.PaySearchParam;
import moonlit.chill.ownpay.vo.PaySearchRule;
import moonlit.chill.ownpay.vo.TradeParam;
import moonlit.chill.ownpay.vo.TradeResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Gawaind
 * @date 2024/11/11 14:46
 */
@Service
public class AsyncTaskServiceImpl implements AsyncTaskService {

    @Resource
    private WaitingPayCache waitingPayCache;

    @Override
    public <T extends TradeParam> void initiatePaySuccessHandler(TradeResult<?> result, T param) {
        param.setTransNum(result.getTransNum());
        if (result.isNeedSearch()){
            PaySearchParam searchParam = new PaySearchParam();
            searchParam.setStartTime(System.currentTimeMillis());
            searchParam.setPaySearchRule(PaySearchRule.LEVEL_1);
            param.setSearchParam(searchParam);
            waitingPayCache.offer(param, System.currentTimeMillis() + PaySearchRule.LEVEL_1.getIntervalTime() * 1000L);
        }
        if (result.isSync()){
            //接口同步返回订单支付成功
            //TODO 业务处理
        }
        //TODO 业务处理
    }

    @Override
    public <T extends TradeParam> void querySuccessHandler(TradeResult<?> result, T tradeParam) {
        //TODO 业务处理
    }

    @Override
    public void tradeSuccessHandler(TradeResult<?> result) {
        //TODO 业务处理
    }
}
