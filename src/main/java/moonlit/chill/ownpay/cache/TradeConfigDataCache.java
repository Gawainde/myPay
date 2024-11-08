package moonlit.chill.ownpay.cache;

import lombok.extern.slf4j.Slf4j;
import moonlit.chill.ownpay.cache.service.ILocalDataCache;
import moonlit.chill.ownpay.service.TradeConfigService;
import moonlit.chill.ownpay.vo.TradeConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Gawaind
 * @date 2024/11/7 10:50
 */
@Slf4j
@Component("TradeConfigDataCache")
public class TradeConfigDataCache extends AbstractDataCacheCommonGet<TradeConfig> implements ILocalDataCache {

    @Resource
    private TradeConfigService tradeConfigService;

    private static final ThreadLocal<String> TL = new ThreadLocal<>();

    public void setMemoryCacheMapUser(Map<String, TradeConfig> memoryCacheMapUser) {
        this.memoryCacheMap = memoryCacheMapUser;
    }

    public TradeConfig getTradeConfig() {
        String code = TL.get();
        return memoryCacheMap.get(code);
    }

    public List<TradeConfig> getTradeConfigByPayType(String payType){
        Map<String, TradeConfig> map = memoryCacheMap;
        List<TradeConfig> list = new ArrayList<>();
        for (TradeConfig value : map.values()) {
            if (value.getPayType().equals(payType)){
                list.add(value);
            }
        }
        if (map.isEmpty()){
            list = tradeConfigService.selectByPayType(payType);
        }
        return list;
    }

    public void remove(){
        TL.remove();
    }

    public void setCode(String code) {
        TL.set(code);
    }

    @Override
    public void refresh() {
        List<TradeConfig> tradeConfig = tradeConfigService.getTradeConfig();
        Map<String, TradeConfig> tempMap = new ConcurrentHashMap<>();
        Set<String> newCode = new HashSet<>();
        for (TradeConfig config : tradeConfig) {
            newCode.add(config.getCode());
            tempMap.put(config.getCode(), config);
        }
        Set<String> oldCode = memoryCacheMap.keySet();
        Set<String> delSet = getDifferSet(oldCode, newCode);
        Set<String> addSet = getDifferSet(newCode, oldCode);
        Set<String> updateSet = getMixSet(newCode, oldCode);
        addSet.addAll(updateSet);
        if (!delSet.isEmpty()) {
            for (String key : delSet) {
                memoryCacheMap.remove(key);
            }
        }
        if (!addSet.isEmpty()) {
            for (String key : addSet) {
                memoryCacheMap.put(key,tempMap.get(key));
            }
        }
    }

    @Override
    public void initData() {
        List<TradeConfig> tradeConfig = tradeConfigService.getTradeConfig();
        for (TradeConfig config : tradeConfig) {
            memoryCacheMap.put(config.getCode(), config);
        }
    }
}
