package moonlit.chill.ownpay.cache;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import moonlit.chill.ownpay.cache.service.ILocalDataCache;
import moonlit.chill.ownpay.exception.PayException;
import moonlit.chill.ownpay.service.TradeMappingService;
import moonlit.chill.ownpay.vo.TradeMappings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Gawaind
 * @date 2024/11/7 11:20
 */
@Slf4j
@Component
public class TradeMappingsDataCache extends AbstractDataCacheCommonGet<List<TradeMappings>> implements ILocalDataCache {

    @Resource
    private TradeMappingService tradeMappingService;

    public void setMemoryCacheMapUser(Map<String, List<TradeMappings>> memoryCacheMap) {
        this.memoryCacheMap = memoryCacheMap;
    }

    public String getCode(Object obj, String payType, String payChannel) {
        try {
            if (ObjectUtil.isEmpty(obj) || payType == null || payChannel == null) {
                log.error("获取交易映射失败");
                return null;
            }
            Map<String, List<TradeMappings>> map = this.memoryCacheMap;
            String key = payType + "-" + payChannel;
            //根据优先级顺序进行优先匹配
            List<TradeMappings> list = map.get(key).stream().sorted(Comparator.comparingInt(TradeMappings::getPriorities)).collect(Collectors.toList());
            for (TradeMappings payMapping : list) {
                String fieldName = payMapping.getFieldName();
                String fieldValue = payMapping.getFieldValue();
                Field field = ReflectUtil.getField(obj.getClass(), fieldName);
                if (field != null) {
                    field.setAccessible(true);
                    Object value = ReflectUtil.getFieldValue(obj, field);
                    if (value != null && value.toString().equals(fieldValue)) {
                        return payMapping.getCode();
                    }
                }
            }
        } catch (Exception e) {
            log.error("tradeMappings获取CODE失败", e);
            throw new PayException("支付异常，请联系管理员");
        }
        return null;
    }


    @Override
    public void refresh() {
        List<TradeMappings> list = tradeMappingService.selectTradeMappings();
        Map<String, List<TradeMappings>> tempMap = list.stream().collect(Collectors.groupingBy(TradeMappings::key));
        Set<String> newKey = tempMap.keySet();
        Set<String> oldKey = memoryCacheMap.keySet();
        Set<String> delSet = getDifferSet(oldKey, newKey);
        Set<String> addSet = getDifferSet(newKey, oldKey);
        Set<String> updateSet = getMixSet(newKey, oldKey);
        addSet.addAll(updateSet);
        if (!delSet.isEmpty()) {
            for (String key : delSet) {
                memoryCacheMap.remove(key);
            }
        }
        if (!addSet.isEmpty()) {
            for (String key : addSet) {
                memoryCacheMap.put(key, tempMap.get(key));
            }
        }
    }

    @Override
    public void initData() {
        List<TradeMappings> list = tradeMappingService.selectTradeMappings();
        Map<String, List<TradeMappings>> collect = list.stream().collect(Collectors.groupingBy(TradeMappings::key));
        memoryCacheMap.putAll(collect);
    }
}
