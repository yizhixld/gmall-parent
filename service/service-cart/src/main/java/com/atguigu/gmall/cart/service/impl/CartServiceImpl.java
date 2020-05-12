package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author yizhixld
 * @create 2020-05-01-10:51
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartInfoMapper cartInfoMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ProductFeignClient productFeignClient;


    @Override
    public void addToCart(Long skuId, String userId, Integer skuNum) {
        //获取购物车key
        String cartKey = getCartKey(userId);
        if(!redisTemplate.hasKey(cartKey)){
            loadCartCache(userId);
        }
        // 获取数据库对象
        QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id", skuId).eq("user_id", userId);
        CartInfo cartInfoExist = cartInfoMapper.selectOne(queryWrapper);
        // 判断数据库中有无数据，有数据， num+skunum； 没有数据，添加
        if (null != cartInfoExist) {
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum() + skuNum);
            // 查询最新价格
            cartInfoExist.setCartPrice(productFeignClient.getSkuPrice(skuId));
            cartInfoMapper.updateById(cartInfoExist);
        } else {
            CartInfo cartInfo1 = new CartInfo();
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            cartInfo1.setSkuId(skuId);
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setSkuNum(skuNum);
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setSkuName(skuInfo.getSkuName());
            cartInfo1.setUserId(userId);
            cartInfoMapper.insert(cartInfo1);
            cartInfoExist = cartInfo1;
        }
        // 更新缓存
        redisTemplate.boundHashOps(cartKey).put(skuId.toString(), cartInfoExist);
        // 设置缓存的过期时间
        setCartKeyExpire(cartKey);

    }

    @Override
    public List<CartInfo> getCartList(String userId, String userTempId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        if(StringUtils.isEmpty(userId)){
            cartInfoList = getCartList(userTempId);
            return cartInfoList;
        }
        // 登录状态判断未登录临时用户id购物车是否有数据
        if(!StringUtils.isEmpty(userId)){
            List<CartInfo> userTempCartList = getCartList(userTempId);
            // 如果未登录购物车中有数据，则进行合并 合并的条件：skuId 相同
            if(!CollectionUtils.isEmpty(userTempCartList)){
                cartInfoList = this.mergeToCartList(userTempCartList, userId);
                // 删除未登录购物车数据
                deleteCartList(userTempId);
            }
            if(StringUtils.isEmpty(userTempId) || CollectionUtils.isEmpty(userTempCartList)){
                cartInfoList = getCartList(userId);
            }
        }
        return cartInfoList;
    }

    @Override
    public void checkCart(String userId, Integer isChecked, Long skuId) {
        // 先修改数据库，再修改缓存
        // 1 修改数据库
        CartInfo cartInfo = new CartInfo();
        cartInfo.setIsChecked(isChecked);
        QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        queryWrapper.eq("sku_id",skuId);
        cartInfoMapper.update(cartInfo,queryWrapper);
        // 2 修改缓存
        String cartKey = getCartKey(userId);
        BoundHashOperations boundHashOperations = redisTemplate.boundHashOps(cartKey);
        if(boundHashOperations.hasKey(skuId.toString())){
            CartInfo cartInfoUpd =(CartInfo) boundHashOperations.get(skuId.toString());
            cartInfoUpd.setIsChecked(isChecked);
            boundHashOperations.put(skuId.toString(),cartInfoUpd);
            setCartKeyExpire(cartKey);
        }
    }

    @Override
    public void deleteCart(Long skuId, String userId) {
        // 先删除缓存，再删除数据库
        // 1先删除缓存
        String cartKey = getCartKey(userId);
        BoundHashOperations boundHashOperations = redisTemplate.boundHashOps(cartKey);
        if(boundHashOperations.hasKey(skuId.toString())){
            boundHashOperations.delete(skuId.toString());
        }
        // 2删除数据库
        QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id",skuId);
        queryWrapper.eq("user_id",userId);
        cartInfoMapper.delete(queryWrapper);
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        // 从缓存查询
        String cartKey = getCartKey(userId);
        List<CartInfo> cartCacheInfoList  = redisTemplate.opsForHash().values(cartKey);
        if(!CollectionUtils.isEmpty(cartCacheInfoList)){
            for (CartInfo cartInfo : cartCacheInfoList) {
                if (cartInfo.getIsChecked().intValue() == 1){
                    cartInfoList.add(cartInfo);
                }
            }
        }
        return cartInfoList;
    }

    /**
    * @Description: //删除购物车
    * @Return:
    **/
    private void deleteCartList(String userTempId) {
        // 先删除缓存
        String cartKey = getCartKey(userTempId);
        Boolean redisHasKey = redisTemplate.hasKey(cartKey);
        if(redisHasKey){
            redisTemplate.delete(cartKey);
        }
        // 删除数据库
        cartInfoMapper.delete(new QueryWrapper<CartInfo>().eq("user_id",userTempId));

    }

    /**
    * @Description: //合并购物车
    * @Return:
    **/
    private List<CartInfo> mergeToCartList(List<CartInfo> userTempCartList, String userId) {
        List<CartInfo> cartList = getCartList(userId);
        Map<Long, CartInfo> cartInfoMap = cartList.stream().collect(Collectors.toMap(CartInfo::getSkuId, cartInfo -> cartInfo));
        for (CartInfo userTempCartInfo : userTempCartList) {
            Long skuId = userTempCartInfo.getSkuId();
            // 如果未登录skuId在已登录账户的数据库已存在，更新数量
            if(cartInfoMap.containsKey(skuId)){
                CartInfo userLoginCartInfo = cartInfoMap.get(skuId);
                userLoginCartInfo.setSkuNum(userLoginCartInfo.getSkuNum()+userTempCartInfo.getSkuNum());
                // 勾选状态更新
                if(userTempCartInfo.getIsChecked().intValue() == 1 ){
                    userLoginCartInfo.setIsChecked(1);
                }
                cartInfoMapper.updateById(userLoginCartInfo);
            }
            // 如果未登录skuId在已登录账户的数据库不存在，存入数据库
            else {
                userTempCartInfo.setId(null);
                userTempCartInfo.setUserId(userId);
                cartInfoMapper.insert(userTempCartInfo);
            }
        }
        List<CartInfo> cartInfoList = loadCartCache(userId);
        return cartInfoList;
    }

    /**
    * @Description: //根据用户id获取购物车信息
    * @Return:
    **/
    private List<CartInfo> getCartList(String userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        if(StringUtils.isEmpty(userId)){
            return cartInfoList;
        }
        // 先查缓存，缓存没有，再查数据库
        String cartKey = getCartKey(userId);
        cartInfoList = redisTemplate.opsForHash().values(cartKey);
        if(!CollectionUtils.isEmpty(cartInfoList)){
            // 购物车列表显示有顺序：按照商品的更新时间 降序，项目中未设置更新时间，采取id排序
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o1.getSkuId().toString().compareTo(o2.getSkuId().toString());
                }
            });
        }
        // 缓存没有数据，查询数据库
        return loadCartCache(userId);
    }
    /**
    * @Description: //缓存中没有数据，查询数据库，并放入缓存
    * @Return:
    **/
    public List<CartInfo> loadCartCache(String userId) {
        QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        List<CartInfo> cartInfoList = cartInfoMapper.selectList(queryWrapper);
        if(CollectionUtils.isEmpty(cartInfoList)){
            return cartInfoList;
        }else{
            // 将查询的数据放入缓存
            HashMap<Object, Object> map = new HashMap<>();
            for (CartInfo cartInfo : cartInfoList) {
                BigDecimal skuPrice = productFeignClient.getSkuPrice(cartInfo.getSkuId());
                cartInfo.setSkuPrice(skuPrice);
                map.put(cartInfo.getSkuId().toString(),cartInfo);
            }
            String cartKey = getCartKey(userId);
            redisTemplate.opsForHash().putAll(cartKey,map);
            setCartKeyExpire(cartKey);
            return cartInfoList;
        }
    }

    /**
    * @Description: //设置缓存的过期时间
    * @Return:
    **/
    private void setCartKeyExpire(String cartKey) {
        redisTemplate.expire(cartKey,RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }

    /**
     * @Description: //获取购物车key
     * @Return:
     **/
    private String getCartKey(String userId) {
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }
}
