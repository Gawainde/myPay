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
