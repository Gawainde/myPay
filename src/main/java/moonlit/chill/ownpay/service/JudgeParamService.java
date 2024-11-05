package moonlit.chill.ownpay.service;

import moonlit.chill.ownpay.vo.TradeParam;

/**
 * @author AnGao
 * @date 2024/11/5 16:27
 */
public interface JudgeParamService {

    /**
     * 参数校验
     *
     * @param param param
     * @author AnGao
     * @date 2024/11/5
     */
    default void judgeParam(Class<? extends TradeParam> param) {}
}
