package com.offcn.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.offcn.seckill.service.SeckillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RabbitListener(queues = "${mq.pay.queue.seckillorder}")
public class SeckillOrderPayMessageListener {

    @Autowired
    private SeckillOrderService seckillOrderService;

    /**
     * @Payload:获取请求体中的信息
     * @param message
     */
    @RabbitHandler
    public void consumeMessage(@Payload String message) {
        Map<String, String> resultMap = JSON.parseObject(message, Map.class);
        String trade_status = resultMap.get("trade_status");
        //判断交易状态是否等于成功
        if (trade_status != null && trade_status.equalsIgnoreCase("TRADE_SUCCESS")) {
            String body = resultMap.get("body");
            Map<String, String> bodyMap = new HashMap<>();
            if (resultMap.get("body") != null) {
                String[] splits = body.split("&");
                for (String split : splits) {
                    String[] vs = split.split("=");
                    bodyMap.put(vs[0], vs[1]);
                }
            }
            //更新秒杀订单
            seckillOrderService.updatePayStatus(bodyMap.get("out_trade_no"),bodyMap.get("trade_no"),bodyMap.get("username"));
        } else {
            //关闭订单
        }
    }
}
