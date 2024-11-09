package moonlit.chill.ownpay.util;

import cn.hutool.core.io.FileUtil;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import lombok.extern.slf4j.Slf4j;
import moonlit.chill.ownpay.cache.TradeConfigDataCache;
import moonlit.chill.ownpay.constants.PayType;
import moonlit.chill.ownpay.vo.TradeCert;
import moonlit.chill.ownpay.vo.TradeConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交易配置
 *
 * @author Gawaind
 * @date 2024/11/8 16:59
 */
@Slf4j
@Component
@Configuration
public class TradeUtil {

    @Autowired
    private TradeConfigDataCache tradeConfigDataCache;

    private static final String SEPARATOR = FileSystems.getDefault().getSeparator();

    @Bean("wxConfig")
    public Map<String, Object> getWxConfigMap() {
        Map<String, Object> map = new HashMap<>();
        try {
            List<TradeConfig> list = tradeConfigDataCache.getTradeConfigByPayType(PayType.WX);
            if (list.isEmpty()) {
                log.error("生成微信证书获取TradeConfig为空");
                throw new RuntimeException("生成微信证书失败");
            }
            for (TradeConfig config : list) {
                String pre = System.getProperty("user.dir") + SEPARATOR + "wxCert" + SEPARATOR + config.getUId() + SEPARATOR;
                List<TradeCert> certs = config.getCerts();
                String certFilePath = pre + "apiclient_cert.pem";
                for (TradeCert cert : certs) {
                    String certPath = pre + cert.getCertName();
                    if (!FileUtil.exist(certPath)) {
                        File file = FileUtil.touch(certPath);
                        file = FileUtil.writeUtf8String(cert.getCertFileContent(), file);
                        if (!FileUtil.exist(file)) {
                            throw new RuntimeException(String.format("生成微信%s证书失败", cert.getCertName()));
                        }
                    }
                    //微信异步回调使用
                    map.put(config.getCode() + "_" + cert.getCertName(), certPath);
                }
                RSAAutoCertificateConfig certificateConfig = new RSAAutoCertificateConfig.Builder()
                        .merchantId(config.getUId())
                        .privateKeyFromPath(map.get(config.getCode() + "_apiclient_key.pem").toString())
                        .merchantSerialNumber(WxCertUtil.getCertificateSerialNumber(certFilePath))
                        .apiV3Key(config.getKey())
                        .build();
                map.put(config.getCode(), certificateConfig);
            }
        } catch (Exception e) {
            log.error("生成微信配置异常");
            throw new RuntimeException(e.getMessage());
        }
        return map;
    }

    @Bean("aliConfig")
    public Map<String, Object> getAliConfigMap() {
        Map<String, Object> map = new HashMap<>();
        try {
            List<TradeConfig> list = tradeConfigDataCache.getTradeConfigByPayType(PayType.ALI);
            if (list.isEmpty()) {
                log.error("生成支付宝证书获取TradeConfig为空");
                throw new RuntimeException("生成支付宝证书失败");
            }
            for (TradeConfig config : list) {
                String pre = System.getProperty("user.dir") + SEPARATOR + "aliCert" + SEPARATOR + config.getUId() + SEPARATOR;
                List<TradeCert> certs = config.getCerts();
                for (TradeCert cert : certs) {
                    String certPath = pre + cert.getCertName();
                    if (!FileUtil.exist(certPath)) {
                        File file = FileUtil.touch(certPath);
                        file = FileUtil.writeUtf8String(cert.getCertFileContent(), file);
                        if (!FileUtil.exist(file)) {
                            throw new RuntimeException(String.format("生成支付宝%s证书失败", cert.getCertName()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("生成支付宝配置异常");
            throw new RuntimeException(e.getMessage());
        }
        return map;
    }
}
