package com.atguigu.gmall.activity.service.impl;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author yizhixld
 * @create 2020-05-11-20:06
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<SeckillGoods> findAll() {
        List<SeckillGoods> seckillGoodsList = redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).values();
        return seckillGoodsList;
    }

    @Override
    public SeckillGoods getSeckillGoods(Long skuId) {
        return (SeckillGoods) redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId.toString());
    }

    @Override
    public void seckillOrder(Long skuId, String userId) {
        // 再次验证产品状态栏，1：可以秒杀，0：结束秒杀
        String state = (String) CacheHelper.get(skuId.toString());
        if ("0".equals(state)) {
            return;
        }
        // 判断用户是否下单
        Boolean isExist = redisTemplate.opsForValue().setIfAbsent(RedisConst.SECKILL_USER + userId, skuId, RedisConst.SECKILL__TIMEOUT, TimeUnit.SECONDS);
        if (!isExist) {
            return;
        }
        // 获取队列中的商品，如果能够获取，则商品存在，可以下单
        String goodsId = (String) redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX + skuId).rightPop();
        if (StringUtils.isEmpty(goodsId)) {
            // 商品售罄 更新状态位
            redisTemplate.convertAndSend("seckillpush", skuId + "0");
            return;
        }
        // 订单记录
        OrderRecode orderRecode = new OrderRecode();
        orderRecode.setUserId(userId);
        orderRecode.setNum(1);
        orderRecode.setSeckillGoods(getSeckillGoods(skuId));
        orderRecode.setOrderStr(MD5.encrypt(userId));
        // 将订单记录存入缓存中
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).put(orderRecode.getUserId(), orderRecode);
        // 更新库存
        this.updateStockCount(orderRecode.getSeckillGoods().getSkuId());
    }

    @Override
    public Result checkOrder(Long skuId, String userId) {
        //用户在缓存中存在，有机会秒杀到商品
        Boolean isExist = redisTemplate.hasKey(RedisConst.SECKILL_USER + userId);
        if(isExist){
            Boolean hasKey = redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).hasKey(userId);
            if(hasKey){
                // 抢单成功
                OrderRecode  orderRecode = (OrderRecode) redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);
                return Result.build(orderRecode, ResultCodeEnum.SECKILL_SUCCESS);
            }
        }
        // 判断是否下单
        Boolean isExistOrder = redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).hasKey(userId);
        if(isExistOrder){
            String orderId = (String) redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).get(userId);
            return Result.build(orderId,ResultCodeEnum.SECKILL_ORDER_SUCCESS);
        }
        String state = (String) CacheHelper.get(skuId.toString());
        if("0".equals(state)){
            return Result.build(null,ResultCodeEnum.SECKILL_FAIL);
        }
        return Result.build(null,ResultCodeEnum.SECKILL_RUN);
    }

    private void updateStockCount(Long skuId) {
        //更新库存，批量更新，用于页面显示，以实际扣减库存为准
        Long stockCount = redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX + skuId).size();
        if (stockCount % 2 == 0) {
            SeckillGoods seckillGoods = getSeckillGoods(skuId);
            seckillGoods.setStockCount(stockCount.intValue());
            seckillGoodsMapper.updateById(seckillGoods);
            // 更新缓存
            redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).put(seckillGoods.getSkuId().toString(), seckillGoods);
        }
    }
}
