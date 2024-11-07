package moonlit.chill.ownpay.config;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import moonlit.chill.ownpay.exception.PayException;
import moonlit.chill.ownpay.service.PayStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Gawaind
 * @date 2024/11/7 10:39
 */
@Slf4j
@Component
public class PayFactory {

    private final ConcurrentHashMap<String, PayStrategy> strategyMap = new ConcurrentHashMap<>();

    @Autowired
    public PayFactory(Map<String, PayStrategy> map) {
        if (CollectionUtil.isNotEmpty(map)) {
            for (Map.Entry<String, PayStrategy> entry : map.entrySet()) {
                if (StrUtil.isEmpty(entry.getKey()) || entry.getValue() == null) {
                    return;
                }
                strategyMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public PayStrategy getPayStrategy(String code) {
        if (code == null || CollectionUtil.isEmpty(strategyMap)) {
            log.error(String.format("支付获取PayStrategy异常 参数:%s map:%s", code, strategyMap));
            throw new PayException("支付出现异常,请联系管理员");
        }
        PayStrategyEnum payEnum = PayStrategyEnum.getPayStrategyEnum(code);
        if (payEnum == null) {
            log.error(String.format("支付获取beanName异常 参数:%s", code));
            throw new PayException("支付出现异常,请联系管理员");
        }
        return strategyMap.get(payEnum.getName());
    }
}
