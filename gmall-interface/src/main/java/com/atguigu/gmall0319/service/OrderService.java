package com.atguigu.gmall0319.service;

import com.atguigu.gmall0319.bean.OrderInfo;

public interface OrderService {
    // 保存订单信息
    String  saveOrder(OrderInfo orderInfo);
    // 生成流水号
    String getTradeNo(String userId);
    // 验证流水号
    boolean checkTradeCode(String userId,String tradeCodeNo);
    // 删除流水号
    void delTradeCode(String userId);
    // 查询库存数量
    boolean checkStock(String skuId, Integer skuNum);
}
