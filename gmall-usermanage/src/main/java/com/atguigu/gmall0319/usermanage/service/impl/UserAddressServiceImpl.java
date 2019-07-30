package com.atguigu.gmall0319.usermanage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0319.bean.UserAddress;
import com.atguigu.gmall0319.service.UserAddService;
import com.atguigu.gmall0319.usermanage.mapper.UserAddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

// 相关的ip地址！
@Service
public class UserAddressServiceImpl implements UserAddService {

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        // 通用mapper 如何传递参数
        Example example = new Example(UserAddress.class);
        //  example.createCriteria() Select * from user_address
        // andEqualTo("userId",userId);where user_id = ?
        example.createCriteria().andEqualTo("userId",userId);
//        UserAddress userAddress = new UserAddress();
//        userAddress.setUserId(userId);
//        userAddressMapper.select(userAddress);
        return userAddressMapper.selectByExample(example);
    }
}
