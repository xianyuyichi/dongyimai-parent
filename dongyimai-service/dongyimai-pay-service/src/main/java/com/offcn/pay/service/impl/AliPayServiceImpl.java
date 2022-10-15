package com.offcn.pay.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.offcn.pay.service.AliPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class AliPayServiceImpl implements AliPayService {

    @Autowired
    private AlipayClient alipayClient;

    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        Map<String, String> map = new HashMap<String, String>();
        //创建阿里支付的请求对象
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        long total = Long.parseLong(total_fee);
        BigDecimal bigTotal = BigDecimal.valueOf(total);
        BigDecimal yuan = bigTotal.divide(new BigDecimal(100d));
        request.setBizContent("{" +
                "    \"out_trade_no\":\"" + out_trade_no + "\"," +
                "    \"total_amount\":\"" + yuan + "\"," +
                "    \"subject\":\"测试购买商品001\"," +
                "    \"store_id\":\"xa_001\"," +
                "    \"timeout_express\":\"90m\"}");
        try {
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            //从相应对象读取相应结果
            String code = response.getCode();
            System.out.println("响应码:" + code);
            //全部的响应结果
            String body = response.getBody();
            //System.out.println("返回结果:" + body);
            if (code.equals("10000")) {
                map.put("qrcode", response.getQrCode());
                map.put("out_trade_no", response.getOutTradeNo());
                map.put("total_fee", total_fee);
              /*  System.out.println("qrcode:" + response.getQrCode());
                System.out.println("out_trade_no:" + response.getOutTradeNo());
                System.out.println("total_fee:" + total_fee);*/
            } else {
                System.out.println("预下单接口调用失败:" + body);
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        return map;
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {
        Map<String,String> map = new HashMap<>();
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" +
                "    \"out_trade_no\":\""+out_trade_no+"\"," +
                "    \"trade_no\":\"\"}"); //设置业务参数
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if(response.getCode().equals("10000")){
                //订单号
                map.put("out_trade_no", out_trade_no);
                map.put("tradestatus", response.getTradeStatus());
                //交易流水号
                map.put("trade_no",response.getTradeNo());
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return map;
    }
}
