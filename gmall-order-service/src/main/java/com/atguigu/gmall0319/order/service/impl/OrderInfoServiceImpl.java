package com.atguigu.gmall0319.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0319.bean.OrderDetail;
import com.atguigu.gmall0319.bean.OrderInfo;
import com.atguigu.gmall0319.config.RedisUtil;
import com.atguigu.gmall0319.order.mapper.OrderDetailMapper;
import com.atguigu.gmall0319.order.mapper.OrderInfoMapper;
import com.atguigu.gmall0319.service.OrderService;
import com.atguigu.gmall0319.util.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class OrderInfoServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public String saveOrder(OrderInfo orderInfo) {
        // 既要保存OrderInfo, 还要保存OrderDetail.
        // 过期时间
        orderInfo.setCreateTime(new Date());
        Calendar calendar = Calendar.getInstance();
        // 多添加一天,2018-8-8
        calendar.add(Calendar.DATE,1);
        // 2018-8-9
        orderInfo.setExpireTime(calendar.getTime());
        // 生成第三方交易编号
        String outTradeNo="ATGUIGU"+System.currentTimeMillis()+""+new Random().nextInt(1000);

        orderInfo.setOutTradeNo(outTradeNo);

        // 插入数据库
        orderInfoMapper.insertSelective(orderInfo);

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
        // 为了跳转到支付页面使用。支付会根据订单id进行支付。
        String orderId = orderInfo.getId();
        return orderId;
    }

    // 生成流水号
    public String getTradeNo(String userId){
        //  利用redis 将生成的流水号存上
        Jedis jedis = redisUtil.getJedis();
        // 生成一个key
        String tradeNoKey="user:"+userId+":tradeCode";
        // 生成一个流水号
        String tradeNo= UUID.randomUUID().toString();
        // 将流水号放入redis
        String result = jedis.setex(tradeNoKey, 10 * 60, tradeNo);
        if ("OK".equals(result)){
            return  tradeNo;
        }else {
            return null;
        }
    }
    // 做一个验证流水号的方法

    /**
     *
     * @param userId 组成redis中的key
     * @param tradeCodeNo 传入进来的流水号
     * @return
     */
    public  boolean checkTradeCode(String userId,String tradeCodeNo){
        // 从前台页面传递过来的流水号，跟redis中做比较，如果一样则返回true，否则false
        Jedis jedis = redisUtil.getJedis();
        // 生成一个key
        String tradeNoKey="user:"+userId+":tradeCode";
        // 取出流水号
        String tradeNo = jedis.get(tradeNoKey);

        if (tradeCodeNo.equals(tradeNo)){
            return true;
        }else {
            return false;
        }
    }

    public void delTradeCode(String userId){
        //  利用redis 将生成的流水号存上
        Jedis jedis = redisUtil.getJedis();
        // 生成一个key
        String tradeNoKey="user:"+userId+":tradeCode";
        // 删除没有毛病！
        jedis.del(tradeNoKey);
        jedis.close();

    }

    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        // 如果库存系统返回1 则是true，0 是false；
        // 调用接口 ，借助工具类httpClientUtils
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);

        if("1".equals(result)){
            return true;
        }else {
            return false;
        }
    }
}
