package com.atguigu.gmall0319.service;

import com.atguigu.gmall0319.bean.UserAddress;

import java.util.List;

public interface UserAddService {
    // 根据userId 查询用户收获地址
    List<UserAddress> getUserAddressList(String userId);
}
