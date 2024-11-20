package moonlit.chill.ownpay.service.impl.wx;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.http.AbstractHttpClient;
import com.wechat.pay.java.core.http.DefaultHttpClientBuilder;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.core.util.IOUtil;
import com.wechat.pay.java.service.billdownload.BillDownloadService;
import com.wechat.pay.java.service.billdownload.model.GetTradeBillRequest;
import com.wechat.pay.java.service.billdownload.model.QueryBillEntity;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.model.TransactionAmount;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.*;
import lombok.extern.slf4j.Slf4j;
import moonlit.chill.ownpay.cache.TradeConfigDataCache;
import moonlit.chill.ownpay.constants.PayType;
import moonlit.chill.ownpay.constants.TradeResultCode;
import moonlit.chill.ownpay.service.PayStrategy;
import moonlit.chill.ownpay.util.WxCertUtil;
import moonlit.chill.ownpay.vo.TradeConfig;
import moonlit.chill.ownpay.vo.TradeParam;
import moonlit.chill.ownpay.vo.TradeResult;
import moonlit.chill.ownpay.vo.TradeResultResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 微信异步回调、查询、退款、退款查询、下载账单
 * @author Gawaind
 * @date 2024/11/7 11:58
 */
@Slf4j
@Service("wxOtherMethodStrategy")
public class WxOtherMethodStrategyImpl implements PayStrategy {

    @Resource(name = "wxConfig")
    private Map<String, Object> configMap;

    @Resource
    private TradeConfigDataCache tradeConfigDataCache;

    private static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

    @Override
    public TradeResult<?> notify(HttpServletRequest request, HttpServletResponse response) {
        TradeResult<String> result = new TradeResult<>();
        Map<String, String> map = new HashMap<>(12);
        response.setStatus(500);
        map.put("code", "ERROR");
        map.put("message", "回调异常");
        BufferedReader reader = null;
        try {
            TradeConfig tradeConfig = tradeConfigDataCache.getTradeConfig();
            String wechatPaySerial = request.getHeader("Wechatpay-Serial");
            String wechatpayNonce = request.getHeader("Wechatpay-Nonce");
            String wechatTimestamp = request.getHeader("Wechatpay-Timestamp");
            String wechatSignature = request.getHeader("Wechatpay-Signature");
            log.info("微信异步回调serialNo：{}，nonce：{}，timestamp：{}，signature：{}", wechatPaySerial, wechatpayNonce, wechatTimestamp, wechatSignature);
            StringBuilder builder = new StringBuilder();
            ServletInputStream inputStream = request.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line = reader.readLine();
            while (line != null) {
                builder.append(line);
                line = reader.readLine();
            }
            reader.close();
            String reqBody = builder.toString();
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(wechatPaySerial)
                    .nonce(wechatpayNonce)
                    .signature(wechatSignature)
                    .timestamp(wechatTimestamp)
                    .body(reqBody)
                    .build();
            log.info("微信异步回调密文 {}", requestParam);
            NotificationConfig config = new RSAAutoCertificateConfig.Builder()
                    .merchantId(tradeConfig.getUId())
                    .privateKeyFromPath(configMap.get(tradeConfig.getCode() + "_keyCertPath").toString())
                    .merchantSerialNumber(WxCertUtil.getCertificateSerialNumber(configMap.get(tradeConfig.getCode() + "_certPath").toString()))
                    .apiV3Key(tradeConfig.getKey())
                    .build();
            NotificationParser parser = new NotificationParser(config);
            Transaction transaction = parser.parse(requestParam, Transaction.class);
            log.info("微信异步回调数据：{}", JSONUtil.toJsonStr(transaction));
            if (ObjectUtil.isNotEmpty(transaction)) {
                TradeResultResponse tradeResult = new TradeResultResponse();
                if (transaction.getTradeState().equals(Transaction.TradeStateEnum.SUCCESS)) {
                    tradeResult.setTradeTime(DateUtil.parse(transaction.getSuccessTime(), TIME_FORMAT));
                    tradeResult.setTransNum(transaction.getOutTradeNo());
                    tradeResult.setAdditionalInfo(transaction.getAttach());
                    tradeResult.setTradeNo(transaction.getTransactionId());
                    BigDecimal amountPay = (new BigDecimal(transaction.getAmount().getTotal()).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
                    tradeResult.setTotalAmount(amountPay);
                    tradeResult.setPayType(PayType.WX);
                    result.setTradeResult(JSONUtil.toJsonStr(tradeResult));
                } else {
                    result.error(transaction.getTradeStateDesc(), TradeResultCode.PAY_FAIL_CODE);
                }
                response.setStatus(HttpStatus.HTTP_OK);
                map.put("code", "SUCCESS");
                map.put("message", "SUCCESS");
            } else {
                result.error("微信异步回调失败", TradeResultCode.PAY_FAIL_CODE);
            }
        } catch (Exception e) {
            result.errorWithException("微信异步回调异常，请联系管理员", TradeResultCode.PAY_ERROR_CODE, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    response.setHeader("Content-type", ContentType.JSON.toString());
                    response.getOutputStream().write(JSONUtil.toJsonStr(map).getBytes(StandardCharsets.UTF_8));
                    response.flushBuffer();
                } catch (IOException e) {
                    log.error("微信异步回调IO异常", e);
                }
            }
        }
        return result;
    }

    @Override
    public TradeResult<?> tradeQuery(TradeParam tradeParam) {
        log.info("微信支付查询参数：{}", JSONUtil.toJsonStr(tradeParam));
        TradeResult<String> result = new TradeResult<>();
        try {
            Config config = getConfig();
            NativePayService service = new NativePayService.Builder().config(config).build();
            QueryOrderByOutTradeNoRequest queryRequest = new QueryOrderByOutTradeNoRequest();
            TradeConfig payConfig = tradeConfigDataCache.getTradeConfig();
            queryRequest.setMchid(payConfig.getUId());
            queryRequest.setOutTradeNo(tradeParam.getTransNum());
            log.info("微信支付查询请求参数：{}", JSONUtil.toJsonStr(queryRequest));
            Transaction transaction = service.queryOrderByOutTradeNo(queryRequest);
            log.info("微信支付查询响应参数：{}", JSONUtil.toJsonStr(transaction));
            if (ObjectUtil.isNotEmpty(transaction)) {
                Transaction.TradeStateEnum state = transaction.getTradeState();
                if (Objects.equals(state, Transaction.TradeStateEnum.SUCCESS)) {
                    TradeResultResponse query = new TradeResultResponse();
                    query.setPayType(PayType.WX);
                    query.setTradeTime(DateUtil.parse(transaction.getSuccessTime(), TIME_FORMAT));
                    query.setTransNum(transaction.getOutTradeNo());
                    query.setTradeNo(transaction.getTransactionId());
                    TransactionAmount amount = transaction.getAmount();
                    query.setTotalAmount(new BigDecimal(amount.getTotal()).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
                    query.setAdditionalInfo(transaction.getAttach());
                    result.setTradeResult(JSONUtil.toJsonStr(query));
                } else if (state.equals(Transaction.TradeStateEnum.USERPAYING) || state.equals(Transaction.TradeStateEnum.NOTPAY)) {
                    result.error("用户尚未支付", TradeResultCode.PAY_USER_PAYING);
                } else if (state.equals(Transaction.TradeStateEnum.PAYERROR)) {
                    result.error("支付失败，请重新发起支付", TradeResultCode.PAY_FAIL_CODE);
                } else {
                    result.error("支付尚未成功", TradeResultCode.PAY_FAIL_CODE);
                }
            } else {
                result.error("查询失败，请重试", TradeResultCode.PAY_FAIL_CODE);
            }
        } catch (Exception e) {
            result.errorWithException(e.getMessage(), TradeResultCode.PAY_ERROR_CODE, e);
        }
        return result;
    }

    @Override
    public TradeResult<?> refund(TradeParam tradeParam) {
        log.info("微信退款参数：{}", JSONUtil.toJsonStr(tradeParam));
        TradeResult<String> result = new TradeResult<>();
        try {
            Config config = getConfig();
            RefundService service = new RefundService.Builder().config(config).build();
            CreateRequest param = getCreateRequest(tradeParam);
            log.info("微信退款请求参数：{}", JSONUtil.toJsonStr(param));
            Refund refund = service.create(param);
            log.info("微信退款响应参数：{}", JSONUtil.toJsonStr(refund));
            if (ObjectUtil.isNotEmpty(refund)) {
                Status status = refund.getStatus();
                String refundTime;
                if (status.equals(Status.SUCCESS)) {
                    refundTime = refund.getSuccessTime();
                } else if (status.equals(Status.ABNORMAL)) {
                    result.error("退款异常，请联系管理员", TradeResultCode.PAY_FAIL_CODE);
                    return result;
                } else {
                    refundTime = refund.getCreateTime();
                }
                TradeResultResponse refundResult = new TradeResultResponse();
                refundResult.setTransNum(refund.getOutTradeNo());
                refundResult.setRefundNo(refund.getOutRefundNo());
                refundResult.setTradeNo(refund.getTransactionId());
                refundResult.setOutRefundNo(refund.getRefundId());
                Long refundFee = refund.getAmount().getRefund();
                BigDecimal amountPay = (new BigDecimal(refundFee).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
                refundResult.setRefundAmount(amountPay);
                refundResult.setTradeTime(DateUtil.parse(refundTime, TIME_FORMAT));
                result.setTradeResult(JSONUtil.toJsonStr(refundResult));
            } else {
                result.error("退款异常，请联系管理员", TradeResultCode.PAY_FAIL_CODE);
            }
        } catch (Exception e) {
            result.errorWithException("退款异常，请联系管理员", TradeResultCode.PAY_ERROR_CODE, e);
        }
        return result;
    }

    @Override
    public TradeResult<?> refundQuery(TradeParam tradeParam) {
        TradeResult<String> result = new TradeResult<>();
        try {
            Config config = getConfig();
            QueryByOutRefundNoRequest refundQuery = new QueryByOutRefundNoRequest();
            refundQuery.setOutRefundNo(tradeParam.getRefundNo());
            RefundService service = new RefundService.Builder().config(config).build();
            Refund refund = service.queryByOutRefundNo(refundQuery);
            if (ObjectUtil.isNotEmpty(refund)) {
                TradeResultResponse refundResult = new TradeResultResponse();
                Status status = refund.getStatus();
                if (status.equals(Status.SUCCESS)) {
                    refundResult.setTradeTime(DateUtil.parse(refund.getSuccessTime(), TIME_FORMAT));
                } else if (status.equals(Status.PROCESSING)) {
                    refundResult.setTradeTime(DateUtil.parse(refund.getCreateTime(), TIME_FORMAT));
                } else {
                    result.error("退款查询异常", TradeResultCode.PAY_FAIL_CODE);
                }
                refundResult.setTransNum(refund.getOutTradeNo());
                refundResult.setRefundNo(refund.getRefundId());
                refundResult.setTradeNo(refund.getTransactionId());
                refundResult.setOutRefundNo(refund.getOutRefundNo());
                Long refundFee = refund.getAmount().getRefund();
                BigDecimal amountPay = (new BigDecimal(refundFee).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
                refundResult.setRefundAmount(amountPay);
                result.setTradeResult(JSONUtil.toJsonStr(refundResult));
            } else {
                result.error("退款查询异常，请联系管理员", TradeResultCode.PAY_FAIL_CODE);
            }
        } catch (Exception e) {
            result.errorWithException(e.getMessage(), TradeResultCode.PAY_ERROR_CODE, e);
        }
        return result;
    }

    @Override
    public TradeResult<List<String[]>> bill(String date) {
        TradeResult<List<String[]>> result = new TradeResult<>();
        InputStream inputStream = null;
        try {
            Config config = getConfig();
            BillDownloadService service = new BillDownloadService.Builder().config(config).build();
            GetTradeBillRequest billRequest = new GetTradeBillRequest();
            billRequest.setBillDate(date);
            QueryBillEntity tradeBill = service.getTradeBill(billRequest);
            if (ObjectUtil.isNotEmpty(tradeBill)) {
                String downloadUrl = tradeBill.getDownloadUrl();
                DefaultHttpClientBuilder builder = new DefaultHttpClientBuilder();
                AbstractHttpClient build = builder.config(config).build();
                inputStream = build.download(downloadUrl);
                String respBody = IOUtil.toString(inputStream);
                String[] split = respBody.split("\n");
                List<String[]> list = new ArrayList<>();
                for (int i = 1; i < split.length - 2; i++) {
                    String s = split[i];
                    s = s.substring(1);
                    String[] strings = s.split(",`");
                    list.add(strings);
                }
                result.setTradeResult(list);
            } else {
                result.error("下载微信账单异常", TradeResultCode.PAY_FAIL_CODE);
            }
        } catch (Exception e) {
            result.errorWithException(e.getMessage(), TradeResultCode.PAY_FAIL_CODE, e);
        } finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("下载微信账单IO关闭异常", e);
                    result.errorWithException(e.getMessage(), TradeResultCode.PAY_FAIL_CODE, e);
                }
            }
        }
        return result;
    }

    @NotNull
    private static CreateRequest getCreateRequest(TradeParam tradeParam) {
        CreateRequest param = new CreateRequest();
        param.setOutTradeNo(tradeParam.getTransNum());
        param.setOutRefundNo(tradeParam.getRefundNo());
        AmountReq amount = new AmountReq();
        amount.setRefund(tradeParam.getRefundAmount().multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).longValue());
        amount.setTotal(tradeParam.getTotalAmount().multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).longValue());
        amount.setCurrency("CNY");
        param.setAmount(amount);
        return param;
    }

    private Config getConfig() {
        TradeConfig tradeConfig = tradeConfigDataCache.getTradeConfig();
        return (Config) configMap.get(tradeConfig.getCode());
    }
}
