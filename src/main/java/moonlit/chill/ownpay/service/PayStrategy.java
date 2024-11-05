package moonlit.chill.ownpay.service;

import moonlit.chill.ownpay.constants.TradeResultCode;
import moonlit.chill.ownpay.vo.PayResult;
import moonlit.chill.ownpay.vo.TradeParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author AnGao
 * @date 2024/4/15 15:06
 */
public interface PayStrategy {

    /**
     * 发起支付
     */
    default <T extends TradeParam> PayResult<?> payMethod(T param) {
        PayResult<?> result = new PayResult<>();
        result.error("当前支付方式不支持", TradeResultCode.PAY_FAIL_CODE);
        return result;
    }

    /**
     * 支付查询
     */
    default <T extends TradeParam> PayResult<?> payForPayQuery(T param) {
        PayResult<?> result = new PayResult<>();
        result.error("当前支付方式不支持", TradeResultCode.PAY_FAIL_CODE);
        return result;
    }

    /**
     * 退款
     */
    default <T extends TradeParam> PayResult<?> payForRefund(T param) {
        PayResult<?> result = new PayResult<>();
        result.error("当前支付方式不支持", TradeResultCode.PAY_FAIL_CODE);
        return result;
    }

    /**
     * 退款查询
     */
    default <T extends TradeParam> PayResult<?> payForRefundQuery(T param) {
        PayResult<?> result = new PayResult<>();
        result.error("当前支付方式不支持", TradeResultCode.PAY_FAIL_CODE);
        return result;
    }

    /**
     * 异步回调
     */
    default PayResult<?> payForNotify(HttpServletRequest request, HttpServletResponse response) {
        PayResult<?> result = new PayResult<>();
        result.error("当前支付方式不支持", TradeResultCode.PAY_FAIL_CODE);
        return result;
    }

    /**
     * 支付下载账单
     */
    default PayResult<List<String[]>> payForBill(String date) {
        PayResult<List<String[]>> result = new PayResult<>();
        result.error("当前支付方式不支持", TradeResultCode.PAY_FAIL_CODE);
        return result;
    }
}
