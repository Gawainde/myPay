package moonlit.chill.ownpay.async.service;

import moonlit.chill.ownpay.vo.TradeParam;

/**
 * @author Gawaind
 * @date 2024/11/8 16:30
 */
public interface AsyncServiceForPay {

    /**
     * 异步查询
     * @author Gawaind
     * @param payParam param
     * @date 2024/11/8
     */
    <T extends TradeParam> void executeAsync(T payParam);
}
