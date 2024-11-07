package moonlit.chill.ownpay.service.impl;

import moonlit.chill.ownpay.service.ParamHandlerService;
import moonlit.chill.ownpay.vo.TradeParam;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * @author Gawaind
 * @date 2024/11/6 9:18
 */
@Service
@Primary
public class ParamHandlerServiceImpl implements ParamHandlerService {
    @Override
    public <T extends TradeParam> void paramHandler(T param) {
        ParamHandlerService.super.paramHandler(param);
    }
}
