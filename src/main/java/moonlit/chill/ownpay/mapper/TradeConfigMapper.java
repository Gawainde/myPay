package moonlit.chill.ownpay.mapper;

import moonlit.chill.ownpay.vo.TradeConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Gawaind
 * @date 2024/11/7 14:19
 */
@Mapper
public interface TradeConfigMapper {

    List<TradeConfig> selectByPayType(@Param("payType") String payType);

    List<TradeConfig> getTradeConfig();
}
