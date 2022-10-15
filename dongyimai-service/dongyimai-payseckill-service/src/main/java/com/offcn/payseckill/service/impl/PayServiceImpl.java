package com.offcn.payseckill.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.offcn.payseckill.service.PayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PayServiceImpl implements PayService {
    /**
     * 支付宝gatewayUrl
     */
    @Value("${alipay.serverUrl}")
    private String serverUrl;
    /**
     * 商户应用id
     */
    @Value("${alipay.appId}")
    private String appId;
    /**
     * RSA私钥，用于对商户请求报文加签
     */
    @Value("${alipay.privateKey}")
    private String privateKey;
    /**
     * 支付宝RSA公钥，用于验签支付宝应答
     */
    @Value("${alipay.alipayPublicKey}")
    private String alipayPublicKey;
    /**
     * 签名类型
     */
    @Value("${alipay.signType}")
    private String signType = "RSA2";
    /**
     * 格式
     */
    @Value("${alipay.format}")
    private String formate = "json";
    /**
     * 编码
     */
    @Value("${alipay.charset}")
    private String charset = "UTF-8";

    /**
     * 异步地址
     */
    @Value("${alipay.notify-url}")
    private String notifyUrl;
    @Override
    public Map<String, String> createNative(Map<String, String> parameters) {
        Map<String, String> map = new HashMap<>();
        //创建阿里支付客户端请求对象
        DefaultAlipayClient alipayClient = new DefaultAlipayClient(serverUrl, appId, privateKey, formate, charset, alipayPublicKey, signType);
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        //设置回调地址
        request.setNotifyUrl(notifyUrl);
        //设置预下单请求参数
        request.setBizContent("{" +
                "    \"out_trade_no\":\""+parameters.get("out_trade_no")+"\"," +
                "    \"body\":\"queue="+parameters.get("queue")+"&username="+parameters.get("username")+"&routingkey="+parameters.get("routingkey")+"&exchange="+parameters.get("exchange")+"\"," +
                "    \"total_amount\":\""+parameters.get("total_fee")+"\"," +
                "    \"subject\":\"测试购买商品001\"," +
                "    \"store_id\":\"xa_001\"," +
                "    \"timeout_express\":\"90m\"}");//设置业务参数
        try {
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            String code = response.getCode();
            String body = response.getBody();
            System.out.println(body);
            if(code.equals("10000")){
                map.put("qrcode", response.getQrCode());
                map.put("out_trade_no", response.getOutTradeNo());
                map.put("total_fee",parameters.get("total_fee"));
            }else{
                System.out.println("预下单接口调用失败:"+body);
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        return map;
    }
}
