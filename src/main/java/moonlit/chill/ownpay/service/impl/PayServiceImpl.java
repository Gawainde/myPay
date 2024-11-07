package moonlit.chill.ownpay.service.impl;

import moonlit.chill.ownpay.constants.TradeResultCode;
import moonlit.chill.ownpay.service.ParamHandlerService;
import moonlit.chill.ownpay.service.PayService;
import moonlit.chill.ownpay.vo.PayResult;
import moonlit.chill.ownpay.vo.TradeParam;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Gawaind
 * @date 2024/11/5 16:24
 */
@Service
public class PayServiceImpl implements PayService {

    @Resource
    private ParamHandlerService paramHandlerService;

    
    @Override
    public <T extends TradeParam> PayResult<?> pay(T param) {
        PayResult<?> result = new PayResult<>();
        try {
            paramHandlerService.paramHandler(param);
        } catch (Exception e) {
            result.errorWithException(e.getMessage(), TradeResultCode.PAY_ERROR_CODE, e);
        }
        return result;
    }
}
