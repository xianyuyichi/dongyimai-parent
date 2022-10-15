package com.offcn.pay.controller;

import com.offcn.order.feign.OrderFeign;
import com.offcn.order.pojo.PayLog;
import com.offcn.pay.service.AliPayService;
import com.offcn.sellergoods.entity.Result;
import com.offcn.sellergoods.entity.StatusCode;
import com.offcn.sellergoods.utils.IdWorker;
import com.offcn.sellergoods.utils.TokenDecode;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Autowired
    private AliPayService aliPayService;
    @Autowired
    private TokenDecode tokenDecode;
    @Autowired
    private OrderFeign orderFeign;


    /**
     * 生成二维码
     *
     * @return
     */
    @GetMapping("/createNative")
    public Map createNative() {
        Map<String, String> map = tokenDecode.getUserInfo();
        //获取登录用户
        String username = map.get("username");
        //调用远程服务(获取订单服务中存放到redis中的支付日志对象)
        Result<PayLog> result = orderFeign.searchPayLogFromRedis(username);
        PayLog payLog = result.getData();
        if(!ObjectUtils.isEmpty(payLog)){
            return aliPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee()+"");
        }
        return null;
    }

    /**
     *
     * 查询支付结果状态
     * @param out_trade_no: 订单号
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result result = null;
        Map<String,String> map = aliPayService.queryPayStatus(out_trade_no);
        int x = 0;
        while (true) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            if(x>=100){
                result = new Result(false, StatusCode.ERROR, "二维码超时");
            }

            if (map == null) {//出错
                result = new Result(false, StatusCode.ERROR, "支付出错");
                break;
            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_SUCCESS")) {//如果成功
                orderFeign.updateStatus(map.get("out_trade_no"),map.get("trade_no"));
                result = new Result(true, StatusCode.OK, "支付成功");
                break;
            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_CLOSED")) {//如果成功
                result = new Result(true, StatusCode.OK, "未付款交易超时关闭，或支付完成后全额退款");
                break;
            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_FINISHED")) {//如果成功
                result = new Result(true, StatusCode.OK, "交易结束，不可退款");
                break;
            }


        }
        return result;
    }
}
