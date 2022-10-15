package com.offcn.payseckill.service;

import java.util.Map;

public interface PayService {
    /**
     * 生成二维码
     * @param out_trade_no
     * @param total_fee
     * @return
     */
    Map<String,String> createNative(Map<String,String> parameters);
}
