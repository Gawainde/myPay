package moonlit.chill.ownpay.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Gawaind
 * @date 2024/11/7 14:03
 */
@Data
public class TradeBill {

    private Long id;

    private Long docId;

    private String appId;

    private String uId;

    private String docNo;

    private String tradeNo;

    private Integer docType;

    private Integer incomeExpenses;

    private BigDecimal actualAmount;

    private String payType;

    private String description;

    private Date createTime;

    private Date tradeTime;

    private String refundNo;

    private String outRefundNo;

    private String remark;
}
