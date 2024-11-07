package moonlit.chill.ownpay.util;

import cn.hutool.core.util.IdUtil;

/**
 * @author Gawaind
 * @date 2024/11/7 10:32
 */
public class TransNumUtil {

    private static final String YY_MM_DD = "YYMMdd";

    public static String createTransNum(String payType, String payChannel){
        String random = IdUtil.fastSimpleUUID().toUpperCase();
        //TODO 根据业务生成交易号
        return random;
    }
}
