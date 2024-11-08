package moonlit.chill.ownpay.service.impl.config;

import lombok.extern.slf4j.Slf4j;
import moonlit.chill.ownpay.mapper.TradeConfigMapper;
import moonlit.chill.ownpay.service.TradeConfigService;
import moonlit.chill.ownpay.vo.TradeConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Gawaind
 * @date 2024/11/7 13:55
 */
@Slf4j
@Service
public class TradeConfigServiceImpl implements TradeConfigService {

    @Autowired
    private TradeConfigMapper tradeConfigMapper;

    @Override
    public List<TradeConfig> selectByPayType(String payType) {
        return tradeConfigMapper.selectByPayType(payType);
    }

    @Override
    public List<TradeConfig> getTradeConfig() {
        return tradeConfigMapper.getTradeConfig();
    }
}
