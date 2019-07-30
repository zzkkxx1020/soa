package com.atguigu.gmall0319.passport;

import com.atguigu.gmall0319.passport.util.JwtUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPassportWebApplicationTests {

	@Test
	public void contextLoads() {
	}

	@Test
	public void  test01(){
		// 共有部分
		String key = "atguigu";
		// 盐
		String ip="192.168.67.203";
		// 私有部分
		Map map = new HashMap();
		map.put("userId","1001");
		map.put("nickName","marry");

		String token = JwtUtil.encode(key, map, ip);
		System.out.println("token:="+token);
//		解密
		Map<String, Object> objectMap = JwtUtil.decode(token, key, "1002");
		if (objectMap!=null){
			System.out.println(objectMap.get("userId"));
		}

	}


}
