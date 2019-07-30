package com.atguigu.gmall0319.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0319.bean.CartInfo;
import com.atguigu.gmall0319.bean.SkuInfo;
import com.atguigu.gmall0319.cart.constant.CartConst;
import com.atguigu.gmall0319.cart.mapper.CartInfoMapper;
import com.atguigu.gmall0319.config.RedisUtil;
import com.atguigu.gmall0319.service.CartService;
import com.atguigu.gmall0319.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartServiceImpl implements CartService{

    @Autowired
    private CartInfoMapper cartInfoMapper;
    
    @Reference
    private ManageService manageService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {
        // 添加购物车，如何判断是否登录
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        // 判断当前的购物车中是否有该商品，如果有该商品，则数量+skuNum
        CartInfo cartInfoExist = cartInfoMapper.selectOne(cartInfo);
        if (cartInfoExist!=null){
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
        } else {
            // 新增 CartInfo 中 price，skuName,imgUrl 数据从skuInfo中得到 ，所以，需要根据skuId 查询出skuInfo的信息数据。
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo newCartInfo = new CartInfo();
            newCartInfo.setUserId(userId);
            newCartInfo.setSkuId(skuId);
            newCartInfo.setCartPrice(skuInfo.getPrice());
            newCartInfo.setSkuPrice(skuInfo.getPrice());
            newCartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            newCartInfo.setSkuName(skuInfo.getSkuName());
            newCartInfo.setSkuNum(skuNum);
            cartInfoMapper.insertSelective(newCartInfo);
            // 说明当前购物车中没有该商品，则cartInfoExist空的，所以借助当前对象，当做一个新的CartInfo
            cartInfoExist=newCartInfo;
        }
//      做缓存存储数据 hset(key,field,value) ;key=user:userId:cart
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        // value值，无论是更新，还是新增，都需要添加到缓存中！
        String cartInfoJson = JSON.toJSONString(cartInfoExist);
        jedis.hset(userCartKey,skuId,cartInfoJson);

//      更新一下购物车的过期时间，利用user的过期时间 user:2:info
        String userKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USERINFOKEY_SUFFIX;
        // 取得到当前用户登录key的过期时间
        Long ttl = jedis.ttl(userKey);
        jedis.expire(userCartKey,ttl.intValue());
        jedis.close();
    }

    @Override
    public List<CartInfo> getCartList(String userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
//      定义key ，redis hset(key,field,value);
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
//      取得redis中的数据 能否一次性取出所有数据
//      hget(key,field);
        Jedis jedis = redisUtil.getJedis();
        List<String> cartJsons = jedis.hvals(userCartKey);

        // 将redis中的数据，添加到 List<CartInfo> 中
        if (cartJsons!=null &&  cartJsons.size()>0){
            for (String cartJson : cartJsons) {
                // cartJson 每一个字符串，都应该是一个cartInfo 对象
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartInfoList.add(cartInfo);
            }
            // 集合的排序功能
            cartInfoList.sort(new Comparator<CartInfo>() {
                // 自定义比较，外部比较器 ,内部比较器
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    // equals(),length(),spilt(","),substring(),indexOf(),compareTo(),getBytes();
                    return o1.getId().compareTo(o2.getId());
                }
            });
            return cartInfoList;

        }else {
            // redis中没有从数据库中查询
            List<CartInfo>  cartInfoListCache =  loadCartCache(userId);
            return cartInfoListCache;
        }
    }

    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListCK, String userId) {

        // 先查询到数据库中的购物车列表
        List<CartInfo> cartInfoListDB = cartInfoMapper.selectCartListWithCurPrice(userId);
        List<CartInfo> cartInfoList = null;
        if (cartListCK!=null&& cartListCK.size()>0){
            // 循环CK ,准备启用boolean 类型变量，记录是否有配备数据，如果有，则赋值为true，没有则进行插入数据库
            for (CartInfo cartInfoCK : cartListCK) {
                boolean isMatch = false;
                for (CartInfo cartInfoDB : cartInfoListDB) {
                    if (cartInfoCK.getSkuId().equals(cartInfoDB.getSkuId())){
                        // 数量相加
                        cartInfoDB.setSkuNum(cartInfoCK.getSkuNum()+cartInfoDB.getSkuNum());
                        cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                        isMatch =true;
                    }
                }
                // 没有情况下，插入数据库
                // 再ck循环过程中没有找到匹配数据，则进行插入数据库
                if (!isMatch){
                    // cookie'中没有userId ，因为cookie中存放的是未登录的数据，所以再插入的时候，将cookie对象中的cartInfo中userId赋值
                    cartInfoCK.setUserId(userId);
                    cartInfoMapper.insertSelective(cartInfoCK);
                }
            }
            // 将合并之后的数据，通过userId,查询出来从新放入缓存
            cartInfoList = loadCartCache(userId);
            // 做一个被选中商品的合并 ,会将 cookieList 中被选中的商品丢失！
            for (CartInfo cartInfo : cartInfoList) {
                // cookie中的数据
                for (CartInfo info : cartListCK) {
                    // 27cartinfo = 27 info
                    if (cartInfo.getSkuId().equals(info.getSkuId())){
                        // “isChecked=1”
                        if (info.getIsChecked().equals("1")){
                            // user:userId:checked .更新一下
                            cartInfo.setIsChecked(info.getIsChecked());
                            checkCart(info.getSkuId(),info.getIsChecked(),userId);
                        }
                    }
                }
            }
        }
        return cartInfoList;
    }

    // skuId 变成数组，集合
    @Override
    public void checkCart(String skuId, String isChecked, String userId) {

        // 根据skuId 取出商品购物车，循环遍历将是否被选中更新到redis。 user:2:cart
        Jedis jedis = redisUtil.getJedis();
        // 定义key
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        // hget(key,field)
        String cartJson  = jedis.hget(userCartKey, skuId);
        // 将字符串转换CartInfo 对象
        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
        // 直接将选中状态赋给当前对象
        cartInfo.setIsChecked(isChecked);
        // 新的商品
        String cartCheckdJson  = JSON.toJSONString(cartInfo);
        // 将更新好的对象放入缓存
        jedis.hset(userCartKey,skuId,cartCheckdJson);


        // 将勾选中的商品，从新保存一份到redis user:2:checked -- 全部被选中的商品
        // 定义一个被选中商品的key
        String userCheckedKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CHECKED_KEY_SUFFIX;
        // 被选中的商品放入当前userCheckedKey
        if ("1".equals(isChecked)){
            // 被选中添加
            jedis.hset(userCheckedKey,skuId,cartCheckdJson);
        }else {
            // 没有被选中则删除
            jedis.hdel(userCheckedKey,skuId);
        }
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {

        List<CartInfo> cartInfoList = new ArrayList<>();
        // 从redis
        Jedis jedis = redisUtil.getJedis();
        // key
        String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        // 取出所有redis中的值
        List<String> cartJsonList = jedis.hvals(userCheckedKey);
        // 判断取出数据是否为空
        if (cartJsonList!=null && cartJsonList.size()>0){
            // 遍历
            for (String cartInfoJson : cartJsonList) {
                // 将cartInfoJson 变成cartInfo 对象
                CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
                cartInfoList.add(cartInfo);
            }
            // 快捷键ctrl+u返回上一个文件
        }
        return cartInfoList;
    }

    private List<CartInfo> loadCartCache(String userId) {
        // 根据userId 查询数据库，不能直接使用通用mapper。可能产生价格不统一的情况
        // cartInfo --- skuInfo 中的价格不匹配。
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
        if (cartInfoList==null || cartInfoList.size()==0){
            return  null;
        }
        // 准备放入缓存
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        // 创建Jedis对象
        Jedis jedis = redisUtil.getJedis();
        // hset(key,field,value);
        Map<String,String> map = new HashMap<>(cartInfoList.size());
        // 添加从数据库查询出来的数据
        for (CartInfo cartInfo : cartInfoList) {
            // 将cartInfo转换成字符串
            String cartJson  = JSON.toJSONString(cartInfo);
            map.put(cartInfo.getSkuId(),cartJson);
        }
        // map.put(key,value);
        jedis.hmset(userCartKey,map);

        return cartInfoList;
    }
}
