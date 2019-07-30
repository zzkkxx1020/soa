package com.atguigu.gmall0319.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0319.bean.CartInfo;
import com.atguigu.gmall0319.bean.SkuInfo;
import com.atguigu.gmall0319.config.LoginRequire;
import com.atguigu.gmall0319.service.CartService;
import com.atguigu.gmall0319.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class CartController {

    @Reference
    private CartService cartService;

    @Autowired
    private CartCookieHandler cartCookieHandler;
    @Reference
    private ManageService manageService;

    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response){
        String skuNum = request.getParameter("skuNum");
        String skuId = request.getParameter("skuId");
        String userId = (String) request.getAttribute("userId");

        // 当前用户是否处于登录状态
        if (userId!=null){
            cartService.addToCart(skuId,userId,Integer.parseInt(skuNum));
        }else{
            cartCookieHandler.addToCart(request,response,skuId,userId,Integer.parseInt(skuNum));
        }
    //  保存：skuInfo 根据skuId 查询
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        request.setAttribute("skuInfo",skuInfo);
    // 商品数据
        request.setAttribute("skuNum",skuNum);

        return "success";
    }

    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request,HttpServletResponse response){
        // 取得userId
        String userId = (String) request.getAttribute("userId");

        if (userId!=null){
            // 合并购物车,cookie 中的购物车集合跟mysql的购物车进行合并
            List<CartInfo> cartListCK =  cartCookieHandler.getCartList(request);
            List<CartInfo> cartInfoList = null;
            if (cartListCK!=null && cartListCK.size()>0){
                // 开始准备合并
                cartInfoList = cartService.mergeToCartList(cartListCK,userId);
                // 将cookie 中的购物车进行删除
                cartCookieHandler.deleteCartCookie(request,response);
            }else{
                // 从数据库取得, redis中 user:userId:cart
                cartInfoList = cartService.getCartList(userId);
            }
          request.setAttribute("cartList",cartInfoList);
        }else {
            // 从cookie中取的数据
            List<CartInfo> cartList = cartCookieHandler.getCartList(request);
            request.setAttribute("cartList",cartList);
        }
        return "cartList";
    }

    @RequestMapping("checkCart")
    @ResponseBody
    @LoginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request,HttpServletResponse response){

        // 获取传入的参数
        String isChecked = request.getParameter("isChecked");
        String skuId = request.getParameter("skuId");
        // 获取userId
        String userId = (String) request.getAttribute("userId");

        // 判断用户是否登录
        if (userId!=null){
            // mysql-redis
            cartService.checkCart(skuId,isChecked,userId);
        }else{
            // cookie request,response,
            cartCookieHandler.checkCart(request,response,skuId,isChecked);

        }
    }

    // 点击去结算
    @RequestMapping("toTrade")
    @LoginRequire(autoRedirect = true)
    public String toTrade(HttpServletRequest request,HttpServletResponse response){
        // 点击结算的时候，需要合并一下购物车 -- ,userId -- 拦截器中取得，还可以去缓存中取得！user:userId:info-db.
        String userId = (String) request.getAttribute("userId");
        // 取得cookieList中的数据
        List<CartInfo> cartListCK =  cartCookieHandler.getCartList(request);
        if (userId!=null){
            // 直接合并
            cartService.mergeToCartList(cartListCK,userId);
            // 将cookie 中的购物车进行删除
            cartCookieHandler.deleteCartCookie(request,response);
        }

        // 重定向到一个控制器trade。
        return "redirect://order.gmall.com/trade";
    }



}
