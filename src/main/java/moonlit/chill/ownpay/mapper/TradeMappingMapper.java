package moonlit.chill.ownpay.mapper;

import moonlit.chill.ownpay.vo.TradeMappings;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Gawaind
 * @date 2024/11/7 14:21
 */
@Mapper
public interface TradeMappingMapper {

    List<TradeMappings> selectTradeMappings();
}
