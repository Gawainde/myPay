package moonlit.chill.ownpay.service;

import moonlit.chill.ownpay.vo.TradeConfig;

import java.util.List;

/**
 * @author Gawaind
 * @date 2024/11/7 13:55
 */
public interface TradeConfigService {

    /**
     * 根据支付类型获取Config
     * @return java.util.List<moonlit.chill.ownpay.vo.TradeConfig>
     * @author Gawaind
     * @param payType 支付方式
     * @date 2024/11/7
     */
    List<TradeConfig> selectByPayType(String payType);

    /**
     * 获取Config
     * @return java.util.List<moonlit.chill.ownpay.vo.TradeConfig>
     * @author Gawaind
     * @date 2024/11/7
     */
    List<TradeConfig> getTradeConfig();
}
