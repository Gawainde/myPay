package moonlit.chill.ownpay.async.service;

import moonlit.chill.ownpay.vo.TradeParam;
import moonlit.chill.ownpay.vo.TradeResult;

/**
 * @author Gawaind
 * @date 2024/11/11 14:46
 */
public interface AsyncTaskService {

    /**
     * 发起支付后处理
     * @author Gawaind
     * @param result 发起支付结果
     * @param tradeParam 参数
     * @date 2024/11/11
     */
    <T extends TradeParam> void initiatePaySuccessHandler(TradeResult<?> result, T tradeParam);

    /**
     * 查询成功后处理
     * @return void
     * @author Gawaind
     * @param result 查询结果
     * @param tradeParam 参数
     * @date 2024/11/11
     */
    <T extends TradeParam> void querySuccessHandler(TradeResult<?> result, T tradeParam);

    /**
     * 交易成功处理
     * @return void
     * @author Gawaind
     * @param result 交易结果
     * @date 2024/11/11
     */
    void tradeSuccessHandler(TradeResult<?> result);
}
