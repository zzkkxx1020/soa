package com.atguigu.gmall0319.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0319.bean.UserInfo;
import com.atguigu.gmall0319.passport.util.JwtUtil;
import com.atguigu.gmall0319.service.UserInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    private UserInfoService userInfoService;

    @Value("${token.key}")
    private String key;
    /*http://localhost:8087/index 为什么 originUrl*/
    @RequestMapping("index")
    public String index(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");
        // 保存上
        request.setAttribute("originUrl",originUrl);
        return "index";
    }

    @RequestMapping(value = "login",method = RequestMethod.POST)
    @ResponseBody
    public String login(UserInfo userInfo,HttpServletRequest request) {
        // 接收到前台数据
//        调用服务层的登录方法
        UserInfo info = userInfoService.login(userInfo);
//      jwt生成token 需要key，map，salt= ip,服务器的ip地址
        String ip = request.getHeader("X-forwarded-for");

        if (info!=null){
//         登录成功 返回token.
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userId",info.getId());
            hashMap.put("nickName",info.getNickName());
            String token = JwtUtil.encode(key, hashMap, ip);
            System.out.println("tokenL="+token);
            return token;
        }else {
            return "fail";
        }
    }

//    做认证功能！http://passprot.atguigu.com/verify?token=xxxx&currentIp=xxx
    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        // 通过request对象，获取到token
        String token = request.getParameter("token");
//        不从linux服务器中取得！从url路径中取
//        String ip = request.getHeader("X-forwarded-for");
        String currentIp = request.getParameter("currentIp");
//      解密
        Map<String, Object> map = JwtUtil.decode(token, key, currentIp);
        if (map!=null){
//            从解密中取得用户Id
            String userId = (String) map.get("userId");
//            检查redis中是否有用户如果有，则返回succes，否则，返回fail
          UserInfo userInfo =  userInfoService.verify(userId);
          if (userInfo!=null){
              return "success";
          }else {
              return "fail";
          }
        }
        return "fail";
    }

}
