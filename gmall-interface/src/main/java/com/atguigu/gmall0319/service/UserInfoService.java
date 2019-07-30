package com.atguigu.gmall0319.service;

import com.atguigu.gmall0319.bean.UserInfo;

public interface UserInfoService {

//    登录信息查询
    UserInfo login(UserInfo userInfo);
//    通过userId查询redis中是否有用户信息
    UserInfo verify(String userId);
}
