package moonlit.chill.ownpay.service;

import moonlit.chill.ownpay.vo.TradeMappings;

import java.util.List;

/**
 * @author Gawaind
 * @date 2024/11/7 14:45
 */
public interface TradeMappingService {

    /**
     * 获取交易映射
     * @return java.util.List<moonlit.chill.ownpay.vo.TradeMappings>
     * @author Gawaind
     * @date 2024/11/7
     */
    List<TradeMappings> selectTradeMappings();
}
