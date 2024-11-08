package moonlit.chill.ownpay.service.impl.config;

import lombok.extern.slf4j.Slf4j;
import moonlit.chill.ownpay.mapper.TradeMappingMapper;
import moonlit.chill.ownpay.service.TradeMappingService;
import moonlit.chill.ownpay.vo.TradeMappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Gawaind
 * @date 2024/11/7 14:45
 */
@Slf4j
@Service
public class TradeMappingServiceImpl implements TradeMappingService {

    @Autowired
    private TradeMappingMapper tradeMappingMapper;

    @Override
    public List<TradeMappings> selectTradeMappings() {
        return tradeMappingMapper.selectTradeMappings();
    }
}
