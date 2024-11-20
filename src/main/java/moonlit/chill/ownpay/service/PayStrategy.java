package moonlit.chill.ownpay.service;

import moonlit.chill.ownpay.constants.TradeResultCode;
import moonlit.chill.ownpay.vo.TradeResult;
import moonlit.chill.ownpay.vo.TradeParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Gawaind
 * @date 2024/4/15 15:06
 */
public interface PayStrategy {

    /**
     * 发起支付
     */
    default <T extends TradeParam> TradeResult<?> tradeMethod(T param) {
        TradeResult<?> result = new TradeResult<>();
        result.error("当前支付方式不支持", TradeResultCode.PAY_FAIL_CODE);
        return result;
    }

    /**
     * 查询
     */
    default <T extends TradeParam> TradeResult<?> tradeQuery(T param) {
        TradeResult<?> result = new TradeResult<>();
        result.error("当前支付方式不支持", TradeResultCode.PAY_FAIL_CODE);
        return result;
    }

    /**
     * 退款
     */
    default <T extends TradeParam> TradeResult<?> refund(T param) {
        TradeResult<?> result = new TradeResult<>();
        result.error("当前支付方式不支持", TradeResultCode.PAY_FAIL_CODE);
        return result;
    }

    /**
     * 退款查询
     */
    default <T extends TradeParam> TradeResult<?> refundQuery(T param) {
        TradeResult<?> result = new TradeResult<>();
        result.error("当前支付方式不支持", TradeResultCode.PAY_FAIL_CODE);
        return result;
    }

    /**
     * 异步回调
     */
    default TradeResult<?> notify(HttpServletRequest request, HttpServletResponse response) {
        TradeResult<?> result = new TradeResult<>();
        result.error("当前支付方式不支持", TradeResultCode.PAY_FAIL_CODE);
        return result;
    }

    /**
     * 支付下载账单
     */
    default TradeResult<List<String[]>> bill(String date) {
        TradeResult<List<String[]>> result = new TradeResult<>();
        result.error("当前支付方式不支持", TradeResultCode.PAY_FAIL_CODE);
        return result;
    }
}
