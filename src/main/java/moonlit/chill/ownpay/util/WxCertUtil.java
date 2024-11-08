package moonlit.chill.ownpay.util;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.io.resource.Resource;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * @author Gawaind
 * @date 2024/11/8 17:23
 */
@Slf4j
public class WxCertUtil {

    public static String getCertificateSerialNumber(String path){
        if (StrUtil.isEmpty(path)) {
            return null;
        }
        InputStream inputStream;
        X509Certificate cert;
        try {
            File file = new File(path);
            if (file.exists()) {
                inputStream = Files.newInputStream(file.toPath());
            } else {
                Resource resource = new ClassPathResource(path);
                inputStream = resource.getStream();
            }
            Security.addProvider(new BouncyCastleProvider());
            CertificateFactory cf = CertificateFactory.getInstance("X.509", new BouncyCastleProvider());
            cert = (X509Certificate) cf.generateCertificate(inputStream);
            cert.checkValidity();
        } catch (Exception e) {
            log.error("微信证书异常", e);
            return null;
        }
        return cert.getSerialNumber().toString(16).toUpperCase();
    }
}
