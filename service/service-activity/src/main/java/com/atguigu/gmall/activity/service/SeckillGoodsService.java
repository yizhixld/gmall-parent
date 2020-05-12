package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;

import java.util.List;

/**
 * @author yizhixld
 * @create 2020-05-11-20:05
 */
public interface SeckillGoodsService {
    /**
    * @Description: //查询所有秒杀的商品
    * @Return:
    **/
    List<SeckillGoods> findAll();
    /**
    * @Description: //根据id查询秒杀商品详情
    * @Return:
    **/
    SeckillGoods getSeckillGoods(Long skuId);
    /**
    * @Description: //根据商品id和用户id实现秒杀下单
    * @Return:
    **/
    void seckillOrder(Long skuId, String userId);
    /**
    * @Description: //根据商品id与用户id查看订单信息
    * @Return:
    **/
    Result checkOrder(Long skuId, String userId);
}
