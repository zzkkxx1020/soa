package com.atguigu.gmall0319.service;

import com.atguigu.gmall0319.bean.CartInfo;

import java.util.List;

public interface CartService {
    // 添加购物车！
    void  addToCart(String skuId,String userId,Integer skuNum);
    // 根据userId 查询购物车信息
    List<CartInfo> getCartList(String userId);
    // 合并购物车
    List<CartInfo> mergeToCartList(List<CartInfo> cartListCK, String userId);
    // 变更购物车选中的商品状态
    void checkCart(String skuId, String isChecked, String userId);
    // 根据userId查询选中的商品列表
    List<CartInfo> getCartCheckedList(String userId);
}
