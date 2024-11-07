package moonlit.chill.ownpay.service;

import moonlit.chill.ownpay.vo.PayResult;
import moonlit.chill.ownpay.vo.TradeParam;

/**
 * @author Gawaind
 * @date 2024/11/5 16:24
 */
public interface PayService {

    <T extends TradeParam> PayResult<?> pay(T param);
}
