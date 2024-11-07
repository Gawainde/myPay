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
    default <T extends TradeParam> TradeResult<?> payMethod(T param) {
        TradeResult<?> result = new TradeResult<>();
        result.error("当前支付方式不支持", TradeResultCode.PAY_FAIL_CODE);
        return result;
    }

    /**
     * 支付查询
     */
    default <T extends TradeParam> TradeResult<?> payForPayQuery(T param) {
        TradeResult<?> result = new TradeResult<>();
        result.error("当前支付方式不支持", TradeResultCode.PAY_FAIL_CODE);
        return result;
    }

    /**
     * 退款
     */
    default <T extends TradeParam> TradeResult<?> payForRefund(T param) {
        TradeResult<?> result = new TradeResult<>();
        result.error("当前支付方式不支持", TradeResultCode.PAY_FAIL_CODE);
        return result;
    }

    /**
     * 退款查询
     */
    default <T extends TradeParam> TradeResult<?> payForRefundQuery(T param) {
        TradeResult<?> result = new TradeResult<>();
        result.error("当前支付方式不支持", TradeResultCode.PAY_FAIL_CODE);
        return result;
    }

    /**
     * 异步回调
     */
    default TradeResult<?> payForNotify(HttpServletRequest request, HttpServletResponse response) {
        TradeResult<?> result = new TradeResult<>();
        result.error("当前支付方式不支持", TradeResultCode.PAY_FAIL_CODE);
        return result;
    }

    /**
     * 支付下载账单
     */
    default TradeResult<List<String[]>> payForBill(String date) {
        TradeResult<List<String[]>> result = new TradeResult<>();
        result.error("当前支付方式不支持", TradeResultCode.PAY_FAIL_CODE);
        return result;
    }
}
