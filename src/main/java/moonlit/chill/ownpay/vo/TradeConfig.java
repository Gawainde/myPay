package moonlit.chill.ownpay.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Gawaind
 * @date 2024/11/7 10:51
 */
@Data
public class TradeConfig {

    /** id */
    private Long id;

    /** 支付方式 */
    private String payType;

    /** appId */
    private String appId;

    /** 商户号 */
    private String uId;

    /** code 唯一 */
    private String code;

    /** 0 禁用 1 启用  默认0 */
    private Integer status;

    /** 异步回调地址 */
    private String notifyUrl;

    /** 微信V3密钥 */
    private String key;

    /** 微信V2密钥 */
    private String v2Key;

    /** 私钥 */
    private String privateKey;

    /** 公钥 */
    private String publicKey;

    /** 同步回调地址 */
    private String returnUrl;

    /** 账单存放地址 */
    private String downPath;

    /** 用户小程序ID */
    private String miniAppId;

    /** 支付宝使用，解决自调用问题 */
    private String appAuthToken;

    /** 备注 */
    private String remark;

    /** 证书 */
    private List<TradeCert> certs;

}
