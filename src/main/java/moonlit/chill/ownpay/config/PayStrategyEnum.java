package moonlit.chill.ownpay.config;

import lombok.Getter;
import moonlit.chill.ownpay.constants.PayStrategySuffix;
import moonlit.chill.ownpay.constants.PayType;

/**
 * @author Gawaind
 * @date 2024/11/7 10:41
 */
@Getter
public enum PayStrategyEnum {

    WX_PC_PAY(PayType.WX + PayStrategySuffix.PC,"wxPcPayStrategy"),
    WX_MINI_PAY(PayType.WX + PayStrategySuffix.MINI,"wxMiniPayStrategy"),
    WX_CODE_PAY(PayType.WX + PayStrategySuffix.CODE,"wxCodePayStrategy"),
    WX_QUERY(PayType.WX + PayStrategySuffix.QUERY,"wxOtherMethodStrategy"),
    WX_REFUND(PayType.WX + PayStrategySuffix.REFUND,"wxOtherMethodStrategy"),
    WX_REFUND_QUERY(PayType.WX + PayStrategySuffix.REFUND_QUERY,"wxOtherMethodStrategy"),
    WX_NOTIFY(PayType.WX + PayStrategySuffix.NOTIFY,"wxOtherMethodStrategy"),
    WX_BILL(PayType.WX + PayStrategySuffix.BILL,"wxOtherMethodStrategy"),

    ALI_PC_PAY(PayType.ALI + PayStrategySuffix.PC,"aliPcPayStrategy"),
    ALI_MINI_PAY(PayType.ALI + PayStrategySuffix.MINI,"aliMiniPayStrategy"),
    ALI_CODE_PAY(PayType.ALI + PayStrategySuffix.CODE,"aliCodePayStrategy"),
    ALI_QUERY(PayType.ALI + PayStrategySuffix.QUERY,"aliOtherMethodStrategy"),
    ALI_REFUND(PayType.ALI + PayStrategySuffix.REFUND,"aliOtherMethodStrategy"),
    ALI_REFUND_QUERY(PayType.ALI + PayStrategySuffix.REFUND_QUERY,"aliOtherMethodStrategy"),
    ALI_NOTIFY(PayType.ALI + PayStrategySuffix.NOTIFY,"aliOtherMethodStrategy"),
    ALI_BILL(PayType.ALI + PayStrategySuffix.BILL,"aliOtherMethodStrategy");

    private final String code;

    private final String name;

    PayStrategyEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    public static PayStrategyEnum getPayStrategyEnum(String code){
        PayStrategyEnum[] payEnums = PayStrategyEnum.values();
        if (code != null){
            for (PayStrategyEnum payEnum : payEnums) {
                if (payEnum.code.equals(code)){
                    return payEnum;
                }
            }
        }
        return null;
    }
}
