package moonlit.chill.ownpay.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 支付查询策略
 * @author Gawaind
 * @date 2024/11/5 16:02
 */
@Data
public class PaySearchParam implements Serializable {

    private static final long serialVersionUID = 1001L;

    /** 开始时间*/
    private long startTime;

    /** 查询次数*/
    private int searchCount = 0;

    /** 回调策略*/
    private PaySearchRule paySearchRule;
}
