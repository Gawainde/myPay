package moonlit.chill.ownpay.service.impl.ali;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayDataDataserviceBillDownloadurlQueryModel;
import com.alipay.api.domain.AlipayTradeFastpayRefundQueryModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayDataDataserviceBillDownloadurlQueryRequest;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayDataDataserviceBillDownloadurlQueryResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import lombok.extern.slf4j.Slf4j;
import moonlit.chill.ownpay.cache.TradeConfigDataCache;
import moonlit.chill.ownpay.constants.PayType;
import moonlit.chill.ownpay.constants.TradeResultCode;
import moonlit.chill.ownpay.exception.PayException;
import moonlit.chill.ownpay.service.PayStrategy;
import moonlit.chill.ownpay.util.ZipUtil;
import moonlit.chill.ownpay.vo.TradeConfig;
import moonlit.chill.ownpay.vo.TradeParam;
import moonlit.chill.ownpay.vo.TradeResult;
import moonlit.chill.ownpay.vo.TradeResultResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付宝异步回调、查询、退款、退款查询、下载账单
 * @author Gawaind
 * @date 2024/11/7 13:04
 */
@Slf4j
@Service("aliOtherMethodStrategy")
public class AliOtherMethodStrategyImpl implements PayStrategy {

    @Resource
    private TradeConfigDataCache tradeConfigDataCache;

    @Resource(name = "aliConfig")
    private Map<String, Object> clientMap;

    private static final String TRADE_FINISHED = "TRADE_FINISHED";

    private static final String TRADE_SUCCESS = "TRADE_SUCCESS";

    private static final String TRADE_CLOSED = "TRADE_CLOSED";

    @Override
    public TradeResult<String> notify(HttpServletRequest request, HttpServletResponse response) {
        TradeResult<String> result = new TradeResult<>();
        Map<String, String[]> aliParams = request.getParameterMap();
        Map<String, String> conversionParams = new HashMap<>();
        for (String key : aliParams.keySet()) {
            String[] values = aliParams.get(key);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            // valueStr = new String(valueStr.getBytes("ISO-8859-1"), "UTF-8");
            conversionParams.put(key, valueStr);
        }
        log.info(String.format("支付宝异步回调返回数据:%s", JSONUtil.toJsonStr(conversionParams)));
        try {
            TradeConfig tradeConfig = tradeConfigDataCache.getTradeConfig();
            boolean signVerified;
            String aliPayPublicKey = clientMap.get(tradeConfig.getCode() + "_aliPayCertPath").toString();
            signVerified = AlipaySignature.rsaCertCheckV1(conversionParams, aliPayPublicKey, "utf-8", "RSA2");
            log.info("支付宝异步回调验签结果:" + signVerified);
            if (signVerified) {
                this.check(conversionParams, result);
                //验签通过 获取交易状态
                String tradeStatus = conversionParams.get("trade_status");
                if (result.isSuccess() && tradeStatus.equals(TRADE_SUCCESS)) {
                    TradeResultResponse resultResponse = new TradeResultResponse();
                    resultResponse.setPayType(PayType.ALI);
                    resultResponse.setTransNum(conversionParams.get("out_trade_no"));
                    resultResponse.setTotalAmount(new BigDecimal(conversionParams.get("receipt_amount")));
                    resultResponse.setTradeNo(conversionParams.get("trade_no"));
                    resultResponse.setTradeTime(DateUtil.parseDateTime(conversionParams.get("gmt_payment")));
                    resultResponse.setAdditionalInfo(conversionParams.get("body"));
                    result.setTradeResult(JSONUtil.toJsonStr(resultResponse));
                }
                if (tradeStatus.equals(TRADE_FINISHED) || tradeStatus.equals(TRADE_CLOSED)) {
                    result.error("支付宝异步回调 状态为:" + tradeStatus, TradeResultCode.PAY_FAIL_CODE);
                }
            } else {
                result.error("支付宝异步回调验签失败", TradeResultCode.PAY_FAIL_CODE);
            }
        } catch (AlipayApiException e) {
            result.errorWithException(e.getErrMsg(), TradeResultCode.PAY_ERROR_CODE, e);
        }
        return result;
    }

    @Override
    public TradeResult<String> tradeQuery(TradeParam tradeParam) {
        log.info(String.format("支付宝支付查询参数:%s", JSONUtil.toJsonStr(tradeParam)));
        TradeConfig tradeConfig = tradeConfigDataCache.getTradeConfig();
        TradeResult<String> result = new TradeResult<>();
        AlipayClient alipayClient = getAliPayClient();
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(tradeParam.getTransNum());
        request.setBizModel(model);
        if (StrUtil.isNotEmpty(tradeConfig.getAppAuthToken())){
            request.putOtherTextParam("app_auth_token", tradeConfig.getAppAuthToken());
        }
        try {
            AlipayTradeQueryResponse queryResponse;
            queryResponse = alipayClient.certificateExecute(request);
            log.info(String.format("支付宝支付查询参数:%s  返回数据:%s", JSONUtil.toJsonStr(tradeParam), JSONUtil.toJsonStr(queryResponse)));
            if (queryResponse.isSuccess()) {
                if ("TRADE_SUCCESS".equals(queryResponse.getTradeStatus()) || "TRADE_FINISHED".equals(queryResponse.getTradeStatus())) {
                    TradeResultResponse resultForQuery = new TradeResultResponse();
                    resultForQuery.setPayType(PayType.ALI);
                    resultForQuery.setTradeTime(queryResponse.getSendPayDate());
                    resultForQuery.setTransNum(queryResponse.getOutTradeNo());
                    resultForQuery.setTradeNo(queryResponse.getTradeNo());
                    resultForQuery.setTotalAmount(new BigDecimal(queryResponse.getPayAmount()));
                    result.setTradeResult(JSONUtil.toJsonStr(resultForQuery));
                } else {
                    result.error("订单尚未支付成功", TradeResultCode.PAY_FAIL_CODE);
                }
            } else {
                result.error(queryResponse.getSubMsg(), TradeResultCode.PAY_FAIL_CODE);
            }
        } catch (AlipayApiException e) {
            result.errorWithException(e.getErrMsg(), TradeResultCode.PAY_ERROR_CODE, e);
        }
        return result;
    }

    @Override
    public TradeResult<?> refund(TradeParam tradeParam) {
        log.info(String.format("支付宝退款参数:%s", JSONUtil.toJsonStr(tradeParam)));
        TradeResult<String> result = new TradeResult<>();
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        AlipayClient alipayClient = getAliPayClient();
        model.setOutTradeNo(tradeParam.getTransNum());
        model.setOutRequestNo(tradeParam.getRefundNo());
        model.setRefundReason("正常退款");
        model.setRefundAmount(tradeParam.getRefundAmount().toString());
        request.setBizModel(model);
        TradeConfig tradeConfig = tradeConfigDataCache.getTradeConfig();
        if (StrUtil.isNotEmpty(tradeConfig.getAppAuthToken())){
            request.putOtherTextParam("app_auth_token", tradeConfig.getAppAuthToken());
        }
        try {
            AlipayTradeRefundResponse response;
            response = alipayClient.certificateExecute(request);
            log.info(String.format("支付宝退款参数:%s  返回数据:%s", JSONUtil.toJsonStr(tradeParam), JSONUtil.toJsonStr(response)));
            if (response.isSuccess()) {
                TradeResultResponse refundResult = new TradeResultResponse();
                refundResult.setTradeTime(DateUtil.date());
                refundResult.setRefundAmount(new BigDecimal(response.getRefundFee()));
                refundResult.setTransNum(response.getOutTradeNo());
                refundResult.setRefundNo(tradeParam.getRefundNo());
                refundResult.setTradeNo(response.getTradeNo());
                result.setTradeResult(JSONUtil.toJsonStr(refundResult));
            } else {
                result.error(response.getSubMsg(), TradeResultCode.PAY_FAIL_CODE);
            }
        } catch (Exception e) {
            result.errorWithException(e.getMessage(), TradeResultCode.PAY_ERROR_CODE, e);
        }
        return result;
    }

    @Override
    public TradeResult<?> refundQuery(TradeParam tradeParam) {
        log.info(String.format("支付宝退款查询参数:%s", JSONUtil.toJsonStr(tradeParam)));
        TradeResult<String> result = new TradeResult<>();
        AlipayClient alipayClient = getAliPayClient();
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        AlipayTradeFastpayRefundQueryModel model = new AlipayTradeFastpayRefundQueryModel();
        model.setOutRequestNo(tradeParam.getRefundNo());
        model.setOutTradeNo(tradeParam.getTransNum());
        List<String> list = new ArrayList<>();
        list.add("gmt_refund_pay");
        model.setQueryOptions(list);
        request.setBizModel(model);
        try {
            //证书模式
            TradeConfig tradeConfig = tradeConfigDataCache.getTradeConfig();
            AlipayTradeFastpayRefundQueryResponse response;
            response = alipayClient.certificateExecute(request);
            log.info(String.format("支付宝退款查询参数:%s  返回数据:%s", JSONUtil.toJsonStr(tradeParam), JSONUtil.toJsonStr(response)));
            if (response.isSuccess()) {
                TradeResultResponse refundResult = new TradeResultResponse();
                refundResult.setTradeNo(response.getTradeNo());
                refundResult.setTransNum(response.getOutTradeNo());
                refundResult.setRefundNo(response.getOutRequestNo());
                refundResult.setRefundAmount(new BigDecimal(response.getRefundAmount()));
                refundResult.setTradeTime(response.getGmtRefundPay());
                result.setTradeResult(JSONUtil.toJsonStr(refundResult));
            } else {
                result.error(response.getSubMsg(), TradeResultCode.PAY_FAIL_CODE);
            }
        } catch (Exception e) {
            result.errorWithException(e.getMessage(), TradeResultCode.PAY_ERROR_CODE, e);
        }
        return result;
    }

    @Override
    public TradeResult<List<String[]>> bill(String date) {
        TradeConfig tradeConfig = tradeConfigDataCache.getTradeConfig();
        AlipayClient aliPayClient = getAliPayClient();
        AlipayDataDataserviceBillDownloadurlQueryRequest request = new AlipayDataDataserviceBillDownloadurlQueryRequest();
        AlipayDataDataserviceBillDownloadurlQueryModel model = new AlipayDataDataserviceBillDownloadurlQueryModel();
        model.setBillDate(date);
        model.setBillType("trade");
        request.setBizModel(model);
        if (StrUtil.isNotEmpty(tradeConfig.getAppAuthToken())){
            request.putOtherTextParam("app_auth_token", tradeConfig.getAppAuthToken());
        }
        TradeResult<List<String[]>> result = new TradeResult<>();
        try {
            int downLoadTryTime = 0;
            AlipayDataDataserviceBillDownloadurlQueryResponse response;
            response = aliPayClient.certificateExecute(request);
            while (!response.isSuccess() && downLoadTryTime < 3) {
                Thread.sleep(3000);
                downLoadTryTime++;
                response = aliPayClient.certificateExecute(request);
            }
            if (response.isSuccess()) {
                ZipUtil.makdirs(tradeConfig.getDownPath());
                HttpUtil.downloadFile(response.getBillDownloadUrl(), FileUtil.file(tradeConfig.getDownPath()));
                ZipUtil.unzip(new File(tradeConfig.getDownPath() + tradeConfig.getUId() + "0156_" + date.replaceAll("-", "") + ".csv.zip"), tradeConfig.getDownPath());
                //从文件中读取CSV数据
                DataInputStream dataInputStream = new DataInputStream(Files.newInputStream(new File(tradeConfig.getDownPath() + tradeConfig.getUId() + "0156_" + date.replaceAll("-", "") + "_业务明细.csv").toPath()));
                //设置格式
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream, "GBK"));
                //去标头
                for (int i = 0; i < 5; i++) {
                    bufferedReader.readLine();
                }
                String rowRecord;
                List<String[]> list = new ArrayList<>();
                while ((rowRecord = bufferedReader.readLine()) != null) {
                    if ("#-----------------------------------------业务明细列表结束------------------------------------".equals(rowRecord)) {
                        bufferedReader.close();
                        result.setTradeResult(list);
                        return result;
                    }
                    String[] split = rowRecord.split(",");
                    list.add(split);
                }
            } else {
                result.error(response.getSubMsg(), TradeResultCode.PAY_FAIL_CODE);
            }
        } catch (Exception e) {
            result.errorWithException("下载账单失败", TradeResultCode.PAY_ERROR_CODE, e);
        }
        return result;
    }


    private void check(Map<String, String> conversionParams, TradeResult<?> result) {
        TradeConfig tradeConfig = tradeConfigDataCache.getTradeConfig();
        //1 校验通知中的seller_id（或者seller_email)是否为out_trade_no这笔单据的对应的操作方
        if (!conversionParams.get("seller_id").equals(tradeConfig.getUId())) {
            result.error("收款方id不匹配", TradeResultCode.PAY_FAIL_CODE);
        }
        //2验证app_id是否为该商户本身。
        if (!conversionParams.get("app_id").equals(tradeConfig.getAppId())) {
            result.error("商户appId不匹配", TradeResultCode.PAY_FAIL_CODE);
        }
    }

    private DefaultAlipayClient getAliPayClient() {
        DefaultAlipayClient alipayClient;
        try {
            TradeConfig tradeConfig = tradeConfigDataCache.getTradeConfig();
            alipayClient = getCertClient(tradeConfig);
        } catch (Exception e) {
            log.error("支付宝获取DefaultAlipayClient异常", e);
            throw new PayException("支付异常，请联系管理员");
        }
        return alipayClient;
    }

    private DefaultAlipayClient getCertClient(TradeConfig tradeConfig) {
        return (DefaultAlipayClient) clientMap.get(tradeConfig.getCode());
    }
}
