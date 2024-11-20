package moonlit.chill.ownpay.service.impl.wx;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import lombok.extern.slf4j.Slf4j;
import moonlit.chill.ownpay.cache.TradeConfigDataCache;
import moonlit.chill.ownpay.constants.TradeResultCode;
import moonlit.chill.ownpay.service.PayStrategy;
import moonlit.chill.ownpay.util.TransNumUtil;
import moonlit.chill.ownpay.vo.TradeConfig;
import moonlit.chill.ownpay.vo.TradeParam;
import moonlit.chill.ownpay.vo.TradeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 微信主扫
 *
 * @author Gawaind
 * @date 2024/11/7 10:43
 */
@Slf4j
@Service("wxPcPayStrategy")
public class WxPcPayStrategyImpl implements PayStrategy {

    @Autowired
    private TradeConfigDataCache tradeConfigDataCache;

    @Resource(name = "wxConfig")
    private Map<String, Object> configMap;

    private static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

    private Config getConfig() {
        TradeConfig config = tradeConfigDataCache.getTradeConfig();
        return (Config) configMap.get(config.getCode());
    }

    @Override
    public <T extends TradeParam> TradeResult<?> tradeMethod(T param) {
        log.info(String.format("微信主扫入参:%s", JSONUtil.toJsonStr(param)));
        TradeResult<String> result = new TradeResult<>();
        try {
            String transNum = TransNumUtil.createTransNum(param.getPayType(), param.getPayChannel());
            result.setTransNum(transNum);
            TradeConfig config = tradeConfigDataCache.getTradeConfig();
            NativePayService service = new NativePayService.Builder().config(getConfig()).build();
            PrepayRequest request = new PrepayRequest();
            Amount amount = new Amount();
            amount.setTotal(param.getTotalAmount().multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).intValue());
            request.setAmount(amount);
            request.setAppid(config.getAppId());
            request.setMchid(config.getUId());
            request.setDescription(param.getPayTitle());
            request.setNotifyUrl(config.getNotifyUrl());
            request.setOutTradeNo(transNum);
            request.setTimeExpire(DateUtil.format(param.getPayDeadLine(), TIME_FORMAT));
            request.setAttach(JSONUtil.toJsonStr(param.getAdditionalInfo()));
            log.info("微信主扫参数：{}", JSONUtil.toJsonStr(request));
            PrepayResponse response = service.prepay(request);
            log.info("微信主扫返回数据：{}", JSONUtil.toJsonStr(response));
            if (ObjectUtil.isNotEmpty(response) && StrUtil.isNotEmpty(response.getCodeUrl())) {
                result.success(response.getCodeUrl());
            } else {
                result.error("发起支付失败，请联系管理员", TradeResultCode.PAY_FAIL_CODE);
            }
        } catch (Exception e) {
            log.error("微信主扫发起支付失败{}", e.getMessage(), e);
            result.errorWithException("发起支付失败，请联系管理员", TradeResultCode.PAY_ERROR_CODE, e);
        }
        return result;
    }
}
