package moonlit.chill.ownpay.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * @author Gawaind
 * @date 2024/11/5 15:56
 */
@Data
public class TradeParam {

    /** 订单ID */
    @NotEmpty(message = "订单ID不能为空")
    private String docId;

    /**
     * 支付方式
     * {@link moonlit.chill.ownpay.constants.PayType}
     * @date 2024/11/5
     */
    @NotEmpty(message = "支付方式不能为空")
    private String payType;

    /**
     * 支付渠道
     * {@link moonlit.chill.ownpay.constants.PayChannel}
     * @date 2024/11/5
     */
    @NotEmpty(message = "支付渠道不能为空")
    private String payChannel;

    /** 支付金额 */
    private BigDecimal totalAmount;

    /** 退款金额 */
    private BigDecimal refundAmount;

    /** 支付超时时间 */
    private Date payDeadLine;

    /** 付款码 */
    private String authCode;

    /** 交易号 */
    private String transNum;

    /** 用户小程序Id */
    private String miniOpenId;

    @NotEmpty(message = "支付title不能为空")
    private String payTitle;

    /** 退款单号 */
    private String refundNo;

    /** 附加信息 */
    private Map<String, String> additionalInfo;

    /** 异步查询规则 */
    private PaySearchParam searchParam;
}
