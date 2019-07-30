package com.atguigu.gmall0319.cart.mapper;

import com.atguigu.gmall0319.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfoMapper extends Mapper<CartInfo> {
    // 进行多表查询 skuInfo - cartInfo
    List<CartInfo> selectCartListWithCurPrice(String userId);
}
