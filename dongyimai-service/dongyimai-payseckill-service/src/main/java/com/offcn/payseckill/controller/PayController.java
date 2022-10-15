package com.offcn.payseckill.controller;

import com.alibaba.fastjson.JSON;
import com.offcn.payseckill.service.PayService;
import com.offcn.sellergoods.entity.Result;
import com.offcn.sellergoods.entity.StatusCode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("pay")
public class PayController {

    @Autowired
    private PayService payService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 创建二维码连接地址返回给前端 生成二维码图片
     *
     * @param parameters  包含 订单号  包含 金额  包含 queue队列名称 交换机信息 路由信息 用户名
     * @return
     */
    @RequestMapping("/create/native")
    public Result<Map> createNative(@RequestParam Map<String,String> parameters) {
        //获取用户名

        Map<String, String> resultMap = payService.createNative(parameters);

        return new Result<Map>(true, StatusCode.OK, "二维码连接地址创建成功", resultMap);
    }

    @RequestMapping("/notify/url")
    public String reciveResult(HttpServletRequest request){
        Map<String,String> map = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while(parameterNames.hasMoreElements()){
            String name = parameterNames.nextElement();
            map.put(name,request.getParameter(name));
        }

        Map<String,String> bodyMap = new HashMap<>();
        String body = map.get("body");
        String[] strings = body.split("&"); //queuq= xxx   username=xxx    rountkey= xxx
        for (String string : strings) {
            String[] strings2 = string.split("=");
            bodyMap.put(strings2[0],strings2[1]);
        }
        String jsonString = JSON.toJSONString(map);
        rabbitTemplate.convertAndSend(bodyMap.get("exchange"),bodyMap.get("routingkey"),jsonString);

        return "pay-success";
    }
}
