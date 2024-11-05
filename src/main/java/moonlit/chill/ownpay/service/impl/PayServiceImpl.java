package moonlit.chill.ownpay.service.impl;

import moonlit.chill.ownpay.service.PayService;
import moonlit.chill.ownpay.vo.PayResult;
import moonlit.chill.ownpay.vo.TradeParam;
import org.springframework.stereotype.Service;

/**
 * @author AnGao
 * @date 2024/11/5 16:24
 */
@Service
public class PayServiceImpl implements PayService {


    @Override
    public <T extends TradeParam> PayResult<?> pay(T param) {

        return null;
    }
}
