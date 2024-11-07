package moonlit.chill.ownpay.cache;

import lombok.extern.slf4j.Slf4j;
import moonlit.chill.ownpay.cache.service.ILocalDataCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Gawaind
 * @date 2024/11/7 10:54
 */
@Slf4j
@Component
@Configuration
@EnableScheduling
public class LocalCachesSchedule {

    @Autowired
    public Map<String, ILocalDataCache> map;

    public void refreshCache(String key) {
        map.get(key).refresh();
    }

    public void initData() {
        Collection<ILocalDataCache> values = map.values();
        values = values.stream().sorted(Comparator.comparing(ILocalDataCache::getOrder)).collect(Collectors.toList());
        for (ILocalDataCache iLocalDataCache : values) {
            iLocalDataCache.initData();
        }
    }
}
