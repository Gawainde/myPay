package moonlit.chill.pay.constants;

/**
 * @author AnGao
 * @date 2024/11/5 16:15
 */
public interface TradeResultCode {

    /** 支付成功 */
    Integer PAY_SUCCESS_CODE = 200;

    /** 支付失败 */
    Integer PAY_FAIL_CODE = 500;

    /** 支持由于异常导致失败code */
    Integer PAY_ERROR_CODE = 550;

    /** 付款码支付 用户支付中*/
    Integer PAY_USER_PAYING = 600;
}
