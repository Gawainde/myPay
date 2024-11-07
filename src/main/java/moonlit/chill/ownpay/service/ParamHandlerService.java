package moonlit.chill.ownpay.service;

import moonlit.chill.ownpay.vo.TradeParam;

/**
 * @author Gawaind
 * @date 2024/11/5 16:27
 */
public interface ParamHandlerService {

    /**
     * 参数校验
     *
     * @param param param
     * @author Gawaind
     * @date 2024/11/5
     */
    default <T extends TradeParam> void paramHandler(T param) {}
}
