package moonlit.chill.ownpay.vo;

import lombok.Data;

/**
 * @author Gawaind
 * @date 2024/11/7 13:58
 */
@Data
public class TradeCert {

    private Long id;

    private String certCode;

    private String certFileContent;

    private String certName;

    private String remark;
}
