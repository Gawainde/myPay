package moonlit.chill.ownpay.constants;

/**
 * @author Gawaind
 * @date 2024/11/5 16:07
 */
public interface PayStrategySuffix {

    /** 支付查询 */
    String QUERY = "-Q";

    /** 退款查询 */
    String REFUND_QUERY = "-R-Q";

    /** 退款 */
    String REFUND = "-R";

    /** 异步回调 */
    String NOTIFY = "-N";

    /** 下载账单 */
    String BILL = "-B";

    String PC = "-" + PayChannel.PC;

    String MINI = "-" + PayChannel.MINI;

    String CODE = "-" + PayChannel.CODE;
}
