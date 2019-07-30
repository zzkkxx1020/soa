package com.atguigu.gmall0319.service;

import com.atguigu.gmall0319.bean.SkuLsInfo;
import com.atguigu.gmall0319.bean.SkuLsParams;
import com.atguigu.gmall0319.bean.SkuLsResult;

public interface ListService {

    //  保存skuInfo 数据到es中,传递参数应该是：skuLsInfo. 因为skuLsInfo 中的每个字段，都是直接使用的
    //  skuInfo 实体类中的属性，不是全部使用
    void saveSkuInfo(SkuLsInfo skuLsInfo);
    //   编写dsl语句查询返回的结果
    SkuLsResult search(SkuLsParams skuLsParams);
    // 给skuId 对应的商品增加热度平分
    void incrHotScore(String skuId);


}
