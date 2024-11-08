package moonlit.chill.ownpay.service.impl.wx;

import com.wechat.pay.java.core.Config;
import lombok.extern.slf4j.Slf4j;
import moonlit.chill.ownpay.cache.TradeConfigDataCache;
import moonlit.chill.ownpay.service.PayStrategy;
import moonlit.chill.ownpay.vo.TradeConfig;
import moonlit.chill.ownpay.vo.TradeParam;
import moonlit.chill.ownpay.vo.TradeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 微信主扫
 * @author Gawaind
 * @date 2024/11/7 10:43
 */
@Slf4j
@Service("wxPcPayStrategy")
public class WxPcPayStrategyImpl implements PayStrategy {

    @Autowired
    private TradeConfigDataCache tradeConfigDataCache;

    @Resource(name = "wxConfig")
    private Map<String, Object> configMap;

    private static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

    private Config getConfig() {
        TradeConfig config = tradeConfigDataCache.getTradeConfig();
        return (Config) configMap.get(config.getCode());
    }

    @Override
    public <T extends TradeParam> TradeResult<?> payMethod(T param) {
        TradeResult<?> result = new TradeResult<>();
        return PayStrategy.super.payMethod(param);
    }
}
