package com.atguigu.gmall0319.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0319.bean.CartInfo;
import com.atguigu.gmall0319.bean.OrderDetail;
import com.atguigu.gmall0319.bean.OrderInfo;
import com.atguigu.gmall0319.bean.UserAddress;
import com.atguigu.gmall0319.bean.enums.OrderStatus;
import com.atguigu.gmall0319.bean.enums.ProcessStatus;
import com.atguigu.gmall0319.config.LoginRequire;
import com.atguigu.gmall0319.service.CartService;
import com.atguigu.gmall0319.service.OrderService;
import com.atguigu.gmall0319.service.UserAddService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {

//    @Autowired 改成从zk中取得
    @Reference
    private UserAddService userAddService;

    @Reference
    private CartService cartService;

    @Reference
    private OrderService orderService;

//    @RequestMapping("trade")
//    @ResponseBody
//    public List<UserAddress> trade(HttpServletRequest request){
//        String userId = request.getParameter("userId");
////        request.setAttribute("admin","范冰冰");
//        List<UserAddress> userAddressList = userAddService.getUserAddressList(userId);
//        return userAddressList;
//    }
//  userId

    @RequestMapping("trade")
    @LoginRequire(autoRedirect = true)
    public String tradeInit(HttpServletRequest request){

        // 先获取userId
        String userId = (String) request.getAttribute("userId");
        // 页面渲染！ 收货人信息 ，
        List<UserAddress> userAddressList = userAddService.getUserAddressList(userId);
        request.setAttribute("userAddressList",userAddressList);
        //   送货清单 --
        List<CartInfo> cartInfoList =  cartService.getCartCheckedList(userId);

        // 订单明细表，orderDetail,订单表：orderInfo.
        // 商品的详细信息来源于--cartInfo

        // 创建一个orderDetail 的集合
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (CartInfo cartInfo : cartInfoList) {
            // 创建一个OrderDetail 对象
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            // 添加到orderDetailList
            orderDetailList.add(orderDetail);
        }

        // 保存orderDetailList 信息，给前台使用
        request.setAttribute("orderDetailList",orderDetailList);
        // 将orderDetail , 赋给OrderInfo
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        // 总金额
        orderInfo.sumTotalAmount();
        // 保存总金额
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        // 生成流水号
        String tradeNo = orderService.getTradeNo(userId);
        request.setAttribute("tradeNo",tradeNo);

        return "trade";
    }


    // 需要登录
    @RequestMapping("submitOrder")
    @LoginRequire(autoRedirect = true)
    public String  submitOrder(OrderInfo orderInfo,HttpServletRequest request){
        // 获取userId
        String userId = (String) request.getAttribute("userId");

        // 获取页面传递过来的流水号
        String tradeNo = request.getParameter("tradeNo");
        // 调用验证流水号方法
        boolean flag = orderService.checkTradeCode(userId, tradeNo);

        if (!flag){
            request.setAttribute("errMsg","该页面已失效，请重新结算!");
            return "tradeFail";
        }

        // 验证库存：实际是验证
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            boolean result = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
            if (!result){
                request.setAttribute("errMsg","库存不足!");
                return "tradeFail";
            }
        }

        orderInfo.setUserId(userId);
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        // 总金额
        orderInfo.sumTotalAmount();
        // 添加到数据库
        String orderId = orderService.saveOrder(orderInfo);
        // 删除tradeNo-redis
        orderService.delTradeCode(userId);
        // 重定向
        return "redirect://payment.gmall.com/index?orderId="+orderId;

    }




}
