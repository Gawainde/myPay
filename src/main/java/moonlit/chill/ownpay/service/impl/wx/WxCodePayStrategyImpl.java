package moonlit.chill.ownpay.service.impl.wx;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import moonlit.chill.ownpay.cache.TradeConfigDataCache;
import moonlit.chill.ownpay.constants.PayType;
import moonlit.chill.ownpay.constants.TradeResultCode;
import moonlit.chill.ownpay.service.PayStrategy;
import moonlit.chill.ownpay.util.IpUtil;
import moonlit.chill.ownpay.util.TransNumUtil;
import moonlit.chill.ownpay.vo.TradeConfig;
import moonlit.chill.ownpay.vo.TradeParam;
import moonlit.chill.ownpay.vo.TradeResult;
import moonlit.chill.ownpay.vo.TradeResultResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 微信被扫
 *
 * @author Gawaind
 * @date 2024/11/7 11:31
 */
@Slf4j
@Service("wxCodePayStrategy")
public class WxCodePayStrategyImpl implements PayStrategy {

    @Resource
    private TradeConfigDataCache tradeConfigDataCache;

    @Autowired
    private HttpServletRequest request;

    private static final Pattern wxCodePattern = Pattern.compile("^1[0-6][0-9]{16}$");

    private static final String URL = "https://api.mch.weixin.qq.com/pay/micropay";

    private static final String SUCCESS_FLAG = "SUCCESS";

    private static final String USER_PAYING = "USERPAYING";

    private static final String SYSTEM_ERROR = "SYSTEMERROR";

    @Override
    public <T extends TradeParam> TradeResult<?> payMethod(T param) {
        log.info(String.format("微信付款码支付参数:%s", JSONUtil.toJsonStr(param)));
        TradeResult<TradeResultResponse> result = new TradeResult<>();
        TradeResultResponse tradeResultResponse = new TradeResultResponse();
        try {
            if (param.getAuthCode() != null) {
                Matcher matcher = wxCodePattern.matcher(param.getAuthCode());
                if (!matcher.matches()) {
                    result.error("请扫描正确的付款码", TradeResultCode.PAY_ERROR_CODE);
                    return result;
                }
            } else {
                result.error("请扫描正确的付款码", TradeResultCode.PAY_ERROR_CODE);
                return result;
            }
            String transNum = TransNumUtil.createTransNum(param.getPayType(), param.getPayChannel());
            result.setTransNum(transNum);
            param.setTransNum(transNum);
            Date now = DateUtil.date();
            Map<String, String> paramMap = new HashMap<>();
            TradeConfig tradeConfig = tradeConfigDataCache.getTradeConfig();
            paramMap.put("appid", tradeConfig.getAppId());
            paramMap.put("mch_id", tradeConfig.getUId());
            paramMap.put("nonce_str", IdUtil.simpleUUID());
            paramMap.put("body", param.getPayTitle());
            paramMap.put("time_start", DateUtil.format(now, DatePattern.PURE_DATETIME_PATTERN));
            paramMap.put("attach", JSONUtil.toJsonStr(param.getAdditionalInfo()));
            paramMap.put("time_expire", DateUtil.format(param.getPayDeadLine(), DatePattern.PURE_DATETIME_PATTERN));
            paramMap.put("out_trade_no", transNum);
            paramMap.put("total_fee", param.getTotalAmount().multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).toString());
            paramMap.put("spbill_create_ip", IpUtil.getIp(request));
            paramMap.put("auth_code", param.getAuthCode());
            String signature = generateSignature(paramMap);
            paramMap.put("sign", signature);
            Map<String, Object> map = requestWithoutCert(paramMap);
            log.info("付款码支付返回：{}", JSONUtil.toJsonStr(map));
            log.info(String.format("微信付款码支付参数:%s  返回数据:%s", JSONUtil.toJsonStr(param), JSONUtil.toJsonStr(map)));
            if (!SUCCESS_FLAG.equals(map.get("return_code"))) {
                result.error(map.get("return_msg").toString(), TradeResultCode.PAY_FAIL_CODE);
            } else if (!SUCCESS_FLAG.equals(map.get("result_code"))) {
                if (StringUtils.equals(map.get("err_code").toString(), SYSTEM_ERROR) || StringUtils.equals(map.get("err_code").toString(), USER_PAYING)) {
                    result.error(map.get("return_msg").toString(), TradeResultCode.PAY_USER_PAYING);
                    return result;
                }
            } else {
                tradeResultResponse.setPayType(PayType.WX);
                JSONObject object = new JSONObject();
                object.put("payChannel", param.getPayChannel());
                tradeResultResponse.setAdditionalInfo(object.toJSONString());
                tradeResultResponse.setTransNum(map.get("out_trade_no").toString());
                tradeResultResponse.setTradeNo(map.get("transaction_id").toString());
                BigDecimal amountPay = (new BigDecimal(map.get("total_fee").toString()).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
                tradeResultResponse.setTotalAmount(amountPay);
                tradeResultResponse.setTradeTime(DateUtil.parse(map.get("time_end").toString(), DatePattern.PURE_DATETIME_PATTERN));
                result.setSync(Boolean.TRUE);
            }
            result.success(tradeResultResponse);
        } catch (Exception e) {
            result.errorWithException(e.getMessage(), TradeResultCode.PAY_ERROR_CODE, e);
        }
        return result;
    }

    private String generateSignature(final Map<String, String> data) throws Exception {
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[0]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if (k.equals("sign")) {
                continue;
            }
            if (!data.get(k).trim().isEmpty()) {
                // 参数值为空，则不参与签名
                sb.append(k).append("=").append(data.get(k).trim()).append("&");
            }
        }
        sb.append("key=").append(tradeConfigDataCache.getTradeConfig().getV2Key());
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        for (byte item : array) {
            builder.append(Integer.toHexString((item & 0xFF) | 0x100), 1, 3);
        }
        return builder.toString().toUpperCase();
    }

    public Map<String, Object> requestWithoutCert(Map<String, String> reqData) throws Exception {
        reqData.put("nonce_str", IdUtil.simpleUUID());
        reqData.put("sign", generateSignature(reqData));
        String UTF8 = "UTF-8";
        String reqBody = XmlUtil.mapToXmlStr(reqData);
        java.net.URL httpUrl = new URL(URL);
        HttpURLConnection httpURLConnection = (HttpURLConnection) httpUrl.openConnection();
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setConnectTimeout(8000);
        httpURLConnection.setReadTimeout(10000);
        httpURLConnection.connect();
        OutputStream outputStream = httpURLConnection.getOutputStream();
        outputStream.write(reqBody.getBytes(UTF8));
        InputStream inputStream = httpURLConnection.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, UTF8));
        final StringBuilder stringBuffer = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuffer.append(line);
        }
        String resp = stringBuffer.toString();
        bufferedReader.close();
        inputStream.close();
        outputStream.close();
        return XmlUtil.xmlToMap(resp);
    }
}
