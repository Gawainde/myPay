package moonlit.chill.ownpay.async;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import moonlit.chill.ownpay.async.service.AsyncTaskService;
import moonlit.chill.ownpay.vo.TradeParam;
import moonlit.chill.ownpay.vo.TradeResult;

import java.util.TimerTask;

/**
 * @author Gawaind
 * @date 2024/11/11 14:44
 */
@Slf4j
public class AsyncFactory {

    public static <T extends TradeParam> TimerTask querySuccess(TradeResult<?> result, T tradeParam) {
        return new TimerTask() {
            @Override
            public void run() {
                SpringUtil.getBean(AsyncTaskService.class).querySuccessHandler(result, tradeParam);
            }
        };
    }

    public static <T extends TradeParam> TimerTask initiatePaySuccessHandler(TradeResult<?> result, T tradeParam) {
        return new TimerTask() {
            @Override
            public void run() {
                SpringUtil.getBean(AsyncTaskService.class).initiatePaySuccessHandler(result, tradeParam);
            }
        };
    }

    public static TimerTask paySuccessHandler(TradeResult<?> result) {
        return new TimerTask() {
            @Override
            public void run() {
                SpringUtil.getBean(AsyncTaskService.class).tradeSuccessHandler(result);
            }
        };
    }
}
