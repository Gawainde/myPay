package moonlit.chill.ownpay.vo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import moonlit.chill.ownpay.constants.TradeResultCode;

import java.io.Serializable;

/**
 * @author Gawaind
 * @date 2024/11/5 16:32
 */
@Data
@Slf4j
public class TradeResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 支付结果 */
    private T tradeResult;

    private boolean isSuccess = Boolean.FALSE;

    private String message = "OK";

    private Integer code = TradeResultCode.PAY_FAIL_CODE;

    private Exception exception;

    /** 我方交易号 */
    private String transNum;

    /** 是否需要记录支付流水 默认是 */
    private boolean needFlow = Boolean.TRUE;

    /** 该支付方式是否支持查询 默认是 */
    private boolean needSearch = Boolean.TRUE;

    public void error(String message, Integer code) {
        this.message = message;
        this.code = code;
        this.isSuccess = Boolean.FALSE;
    }

    public void success(T tradeResult) {
        this.code = TradeResultCode.PAY_SUCCESS_CODE;
        this.isSuccess = Boolean.TRUE;
        this.tradeResult = tradeResult;
    }

    public void errorWithException(String message, Integer code, Exception e) {
        this.message = message;
        this.code = code;
        this.exception = e;
        this.isSuccess = Boolean.FALSE;
        log.error("支付异常：{}", e.getMessage(), e);
    }
}
