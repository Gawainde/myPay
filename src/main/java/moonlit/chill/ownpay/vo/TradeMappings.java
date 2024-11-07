package moonlit.chill.ownpay.vo;

import lombok.Data;

/**
 * @author Gawaind
 * @date 2024/11/7 14:02
 */
@Data
public class TradeMappings {

    private Long id;

    private String fieldName;

    private String fieldValue;

    private String code;

    private String payType;

    private String payChannel;

    private Integer priorities;

    private String remark;

    public String key(){
        return payType + "-" + payChannel;
    }
}
