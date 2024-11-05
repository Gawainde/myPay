package moonlit.chill.pay.service;

import moonlit.chill.pay.vo.PayResult;
import moonlit.chill.pay.vo.TradeParam;

/**
 * @author AnGao
 * @date 2024/11/5 16:24
 */
public interface PayService {

    <T extends TradeParam> PayResult<?> pay(T param);
}
