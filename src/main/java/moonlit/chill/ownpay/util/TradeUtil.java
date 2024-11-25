package moonlit.chill.ownpay.util;

import cn.hutool.core.io.FileUtil;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.RSAPublicKeyConfig;
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

    private static final String SERVER_URL = "https://openapi.alipay.com/gateway.do";

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
                Config wxConfig = new RSAPublicKeyConfig.Builder()
                        .merchantId(config.getUId())
                        .privateKeyFromPath(map.get(config.getCode() + "_apiclient_key.pem").toString())
                        .publicKeyFromPath(map.get(config.getCode() + "_pub_key.pem").toString())
                        .publicKeyId(config.getPublicKeyId())
                        .merchantSerialNumber(WxCertUtil.getCertificateSerialNumber( pre + "apiclient_cert.pem"))
                        .apiV3Key(config.getKey())
                        .build();
                map.put(config.getCode(), wxConfig);
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
                //throw new RuntimeException("生成支付宝证书失败");
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
                CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
                certAlipayRequest.setServerUrl(SERVER_URL);
                certAlipayRequest.setAppId(config.getAppId());
                certAlipayRequest.setPrivateKey(config.getPrivateKey());
                certAlipayRequest.setFormat("json");
                certAlipayRequest.setCharset("utf-8");
                certAlipayRequest.setSignType("RSA2");
                certAlipayRequest.setCertPath(pre + "appCertPublicKey.crt");
                certAlipayRequest.setAlipayPublicCertPath(pre + "aliCertPublicKey.crt");
                certAlipayRequest.setRootCertPath(pre + "alipayRootCert.crt");
                map.put(config.getCode(), new DefaultAlipayClient(certAlipayRequest));
                map.put(config.getCode() + "_aliPayCertPath", pre + "aliCertPublicKey.crt");
            }
        } catch (Exception e) {
            log.error("生成支付宝配置异常");
            throw new RuntimeException(e.getMessage());
        }
        return map;
    }
}
