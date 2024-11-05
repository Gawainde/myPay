package moonlit.chill.ownpay.vo;

import lombok.Getter;

/**
 * 回调策略
 * @author AnGao
 * @date 2024/11/5 16:10
 */
@Getter
public enum PaySearchRule {

    /**
     * 等级1 10s 执行1次
     */
    LEVEL_1(1, 10 * 1000, 1),

    /**
     * 等级2 30s 执行1次
     */
    LEVEL_2(2, 30 * 1000, 1),

    /**
     * 等级3 1m 执行3次
     */
    LEVEL_3(3, 60 * 1000, 3),

    /**
     * 等级4 5m 执行2次
     */
    LEVEL_4(4, 5 * 60 * 1000, 2),

    /**
     * 等级5 10m 执行2次
     */
    LEVEL_5(5, 10 * 60 * 1000, 2),

    /**
     * 等级6 1h 执行1次
     */
    LEVEL_6(6, 60 * 60 * 1000, 1),

    /**
     * 等级7 2h 执行1次
     */
    LEVEL_7(7, 2 * 60 * 60 * 1000, 1),

    /**
     * 等级8 6h 执行1次
     */
    LEVEL_8(8, 6 * 60 * 60 * 1000, 1),

    /**
     * 等级9 12h 执行1次
     */
    LEVEL_9(9, 12 * 60 * 60 * 1000, 1);


    PaySearchRule(int level, int intervalTime, int count) {
        this.level = level;
        this.intervalTime = intervalTime;
        this.count = count;
    }

    /**
     * 级别
     */
    private final int level;

    /**
     * 回调间隔时间(单位：秒)
     */
    private final long intervalTime;

    /**
     * 回调次数
     */
    private final int count;

    public static PaySearchRule getCallBackTypeByLevel(int level){
        PaySearchRule[] types = PaySearchRule.values();
        for (PaySearchRule type : types) {
            if (type.getLevel() == level){
                return type;
            }
        }
        return null;
    }
}
