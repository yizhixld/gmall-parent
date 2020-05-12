package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

/**
 * @author yizhixld
 * @create 2020-05-01-10:49
 */
public interface CartService {
    /**
    * @Description: //添加购物车
    * @Return:
    **/
    void addToCart(Long skuId,String userId,Integer skuNum);
    /**
    * @Description: //购物车列表展示
    * @Return:
    **/
    List<CartInfo> getCartList(String userId,String userTempId);
    /**
    * @Description: //更新选中状态
    * @Return:
    **/
    void checkCart(String userId,Integer isChecked, Long skuId);
    /**
    * @Description: //删除购物车
    * @Return:
    **/
    void deleteCart(Long skuId, String userId);
    /**
    * @Description: //根据id查询购物车列表
    * @Return:
    **/
    List<CartInfo> getCartCheckedList(String userId);
    /**
    * @Description: //根据用户Id查询购物车最新数据并放入缓存
    * @Return:
    **/
    List<CartInfo> loadCartCache(String userId);
}
