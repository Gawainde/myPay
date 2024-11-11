package moonlit.chill.ownpay.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Gawaind
 * @date 2024/11/11 15:11
 */
@Data
public class TradeResultResponse {

    /** 支付方式 */
    private String payType;

    /** 发起支付时传入的附加信息 json格式*/
    private String additionalInfo;

    /** 交易号 */
    private String transNum;

    /** 三方交易号 */
    private String tradeNo;

    /** 交易金额 */
    private BigDecimal totalAmount;

    /** 交易时间 */
    private Date tradeTime;
}
