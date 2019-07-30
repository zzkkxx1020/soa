package com.atguigu.gmall0319.usermanage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.ArraySerializer;
import com.atguigu.gmall0319.bean.UserInfo;
import com.atguigu.gmall0319.config.RedisUtil;
import com.atguigu.gmall0319.service.UserInfoService;
import com.atguigu.gmall0319.usermanage.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    public String userKey_prefix = "user:";
    public String userinfoKey_suffix = ":info";
    public int userKey_timeOut = 60 * 60;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public UserInfo login(UserInfo userInfo) {
        /*密码是加密后的！ userInfo.getPasswd() =123 */
        userInfo.setPasswd(DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes()));
        UserInfo info = userInfoMapper.selectOne(userInfo);
//        当用户不为空的时候添加到缓存中
        if (info != null) {
//      取得Jedis
            Jedis jedis = redisUtil.getJedis();
//       定义key user:UserId:info ,sku:skuId:info user:1:info
            String userKey = userKey_prefix + info.getId() + userinfoKey_suffix;
//      将数据放入redis
//       将info 对象转换成String
            String userJson = JSON.toJSONString(info);
            jedis.setex(userKey, userKey_timeOut, userJson);
            return info;
        } else {
            return null;
        }
    }

    @Override
    public UserInfo verify(String userId) {
//          redis对象
        Jedis jedis = redisUtil.getJedis();
//        key user:userId:info
        String userKey =userKey_prefix+userId+userinfoKey_suffix;
//      设置一个延迟时间,看产品经理的需要！
        jedis.expire(userKey,userKey_timeOut);

        String userJson = jedis.get(userKey);
        if (userJson!=null && !"".equals(userJson)){
            // 将userJson转换成UserInfo
            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
            return userInfo;
        }
        return null;
    }


}
