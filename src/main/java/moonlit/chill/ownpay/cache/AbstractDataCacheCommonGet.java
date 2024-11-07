package moonlit.chill.ownpay.cache;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author Gawaind
 * @date 2024/11/7 10:51
 */
@Slf4j
public class AbstractDataCacheCommonGet<E> {

    protected Map<String, E> memoryCacheMap = new ConcurrentSkipListMap<>();

    @SuppressWarnings("unchecked")
    public E getCacheData(String key) {
        E cacheData = memoryCacheMap.get(key);
        if (cacheData == null) {
            Class<E> tClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            String className = tClass.getSimpleName();
            String result = String.format("%s为空，key:%s", className, key);
            log.error(result);
            throw new RuntimeException(result);
        }
        return cacheData;
    }

    public boolean containsKey(String key) {
        return memoryCacheMap.containsKey(key);
    }

    public Map<String, E> getMemoryCacheMapUser() {
        return memoryCacheMap;
    }

    //获取set1和set2的交集
    public Set<String> getMixSet(Set<String> set1, Set<String> set2) {
        Set<String> result = new HashSet<>(set1);
        result.retainAll(set2);
        return result;
    }

    //获取set1对set2的差集
    public Set<String> getDifferSet(Set<String> set1, Set<String> set2) {
        Set<String> result = new HashSet<>(set1);
        result.removeAll(set2);
        return result;
    }
}
