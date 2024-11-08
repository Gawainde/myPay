package moonlit.chill.ownpay.async;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import moonlit.chill.ownpay.async.service.AsyncServiceForPay;
import moonlit.chill.ownpay.cache.WaitingPayCache;
import moonlit.chill.ownpay.vo.TradeParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;

/**
 * 异步查询任务
 * @author Gawaind
 * @date 2024/11/8 16:27
 */
@Slf4j
@Component
public class QueryTask {

    @Autowired
    private WaitingPayCache waitingPayCache;

    @Autowired
    private AsyncServiceForPay asyncServiceForPay;

    @Scheduled(cron = "0/10 * * * * ?")
    public void payPolling() {
        boolean lock = false;
        try {
            lock = waitingPayCache.getLock();
            if (lock) {
                payAsync();
            }
        } finally {
            if (lock) {
                waitingPayCache.releaseLock();
            }
        }
    }

    private void payAsync() {
        if (waitingPayCache.isEmpty()) {
            return;
        }
        List<TradeParam> param = waitingPayCache.poll(TradeParam.class);
        for (TradeParam payParam : param) {
            if (ObjectUtil.isEmpty(payParam) || StrUtil.isNotEmpty(payParam.getDocId())) {
                continue;
            }
            try {
                asyncServiceForPay.executeAsync(payParam);
            } catch (Exception e) {
                log.error("支付异步查询发生异常", e);
                if (e instanceof RejectedExecutionException) {
                    log.error("支付异步查询线程池已满", e);
                }
                waitingPayCache.offer(payParam, System.currentTimeMillis());
            }
        }
    }
}
