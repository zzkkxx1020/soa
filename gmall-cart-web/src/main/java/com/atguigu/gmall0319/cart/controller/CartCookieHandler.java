package com.atguigu.gmall0319.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0319.bean.CartInfo;
import com.atguigu.gmall0319.bean.SkuInfo;
import com.atguigu.gmall0319.config.CookieUtil;
import com.atguigu.gmall0319.service.ManageService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartCookieHandler {

    // 定义购物车名称
    private String cookieCartName = "CART";
    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE=7*24*3600;

    @Reference
    private ManageService manageService;

    public void  addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, String userId, Integer skuNum){
        // 也需要判断一下当前cookie中是否有该商品，如果有该商品，则数量+skuNum
        // 从cookie中取得商品数据
        String cartJson  = CookieUtil.getCookieValue(request, cookieCartName, true);
        List<CartInfo> cartInfoList = new ArrayList<>();
        // boolean 类型的变量。
        boolean flag = false;
        if (cartJson!=null&&!"".equals(cartJson)){
            // cartJson 中的数据是什么样的？一条，还是多条
            cartInfoList = JSON.parseArray(cartJson, CartInfo.class);
            // 遍历集合
            for (CartInfo cartInfo : cartInfoList) {
                if (cartInfo.getSkuId().equals(skuId)){
                    // 商品的价格需要添加上
//                    cartInfo.setCartPrice(); ?
                    cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
                    flag=true;
                    break;
                }
            }
        }
        // 购物车中没有该商品
        if (!flag){
            //  添加，根据skuId 查询skuInfo ,赋给cartInfo.
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo=new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());

            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(skuNum);
            // 添加到集合中
            cartInfoList.add(cartInfo);
        }
        // 将cartInfoList 变成字符串
        String newCartJson  = JSON.toJSONString(cartInfoList);
        // 添加到cookie 中
        CookieUtil.setCookie(request,response,cookieCartName,newCartJson,COOKIE_CART_MAXAGE,true);
    }

    public List<CartInfo> getCartList(HttpServletRequest request) {

        // 从cookie 中取得数据

        String cartJson  = CookieUtil.getCookieValue(request, cookieCartName, true);

        // 将字符串转换成List<CartInfo>
        List<CartInfo> cartInfoList = JSON.parseArray(cartJson, CartInfo.class);
        return  cartInfoList;

    }

    // 删除cookie中的数据
    public  void deleteCartCookie(HttpServletRequest request,HttpServletResponse response){
        CookieUtil.deleteCookie(request,response,cookieCartName);
    }

    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {
        // 先获取cookie中的购物车数据
        //  String cartJson = CookieUtil.getCookieValue(request, cookieCartName, true);
        List<CartInfo> cartInfoList =  getCartList(request);
        // 循环cartInfoList
        for (CartInfo cartInfo : cartInfoList) {
            // 如果cookie购物车中有当前的skuId ，则将被选中的状态赋给该商品
            if (cartInfo.getSkuId().equals(skuId)){ // 0
                // 只给传递过来的参数ischecked为1的赋值 , 0,1... // 判断0，1
                cartInfo.setIsChecked(isChecked);
            }
        }
        // 将集合从新序列化到cookieList中
        String jsonString = JSON.toJSONString(cartInfoList);

        // 从新放入cookie中
        CookieUtil.setCookie(request,response,cookieCartName,jsonString,COOKIE_CART_MAXAGE,true);
    }
}
