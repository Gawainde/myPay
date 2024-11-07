package moonlit.chill.ownpay.cache;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import moonlit.chill.ownpay.vo.TradeParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Gawaind
 * @date 2024/11/7 10:56
 */
@Slf4j
@Component("waitingPayCache")
public class WaitingPayCache {

    @Autowired
    public StringRedisTemplate redisTemplate;

    private static final String ASYNC_SEARCH = "ASYNC:SEARCH:";

    private static final String ASYNC_SEARCH_LOCK = "ASYNC:SEARCH:LOCK:";

    public static final RedisScript<List<Object>> ZPOP;

    static {
        String luaScript = "local result = redis.call('zrangebyscore', KEYS[1], 0, ARGV[1]) " +
                "if result ~= nil and #result > 0 " +
                "then " +
                "    redis.call('zrem', KEYS[1], unpack(result)) " +
                "    return result " +
                "end ";
        ZPOP = new DefaultRedisScript(luaScript, List.class);
    }

    private static final String uuId = UUID.fastUUID().toString(true);

    public boolean isEmpty() {
        Long size = redisTemplate.opsForZSet().zCard(ASYNC_SEARCH);
        return size == null || size <= 0L;
    }

    public <T extends TradeParam> void offer(T tradeParam, long score) {
        try {
            redisTemplate.opsForZSet().add(ASYNC_SEARCH, JSONUtil.toJsonStr(tradeParam), score);
        } catch (Exception e) {
            log.error("支付成功后异步查询加入redis失败", e);
        }
    }

    public <T extends TradeParam> List<T> poll(Class<T> tClass) {
        ArrayList<String> list = ListUtil.toList(ASYNC_SEARCH);
        List<Object> objects = redisTemplate.execute(ZPOP, list, System.currentTimeMillis());
        if (objects == null) {
            return new ArrayList<>();
        }
        return objects.stream().map(e -> JSONUtil.toBean(JSONUtil.toJsonStr(e), tClass)).collect(Collectors.toList());
    }

    public boolean getLock() {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(ASYNC_SEARCH_LOCK, uuId, 10L, TimeUnit.MINUTES));
    }

    public void releaseLock() {
        String value = redisTemplate.opsForValue().get(ASYNC_SEARCH_LOCK);
        if (!StrUtil.isEmpty(value) && value.equals(uuId)) {
            redisTemplate.opsForValue().getOperations().delete(ASYNC_SEARCH_LOCK);//删除key
        }
    }
}
