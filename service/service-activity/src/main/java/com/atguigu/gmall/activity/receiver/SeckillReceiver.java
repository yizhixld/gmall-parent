package com.atguigu.gmall.activity.receiver;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.activity.util.DateUtil;
import com.atguigu.gmall.common.config.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.activity.UserRecode;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * @author yizhixld
 * @create 2020-05-11-19:11
 */
@Component
public class SeckillReceiver {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_1, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = {MqConst.ROUTING_TASK_1}
    ))
    public void importItemToRedis(Message message, Channel channel) {
        // 先查询秒杀商品详情（status为1，库存大于0，秒杀开始时间限制）
        QueryWrapper<SeckillGoods> seckillGoodsQueryWrapper = new QueryWrapper<>();
        seckillGoodsQueryWrapper.eq("status", 1);
        seckillGoodsQueryWrapper.gt("stock_count", 0);
        seckillGoodsQueryWrapper.eq("DATE_FORMAT(start_time,'%Y-%m-%d')", DateUtil.formatDate(new Date()));
        List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectList(seckillGoodsQueryWrapper);
        // 查出的商品存入redis
        if (!CollectionUtils.isEmpty(seckillGoods)) {
            for (SeckillGoods seckillGood : seckillGoods) {
                //使用hash 数据类型保存商品
                //key = seckill:goods field = skuId
                //先判断缓存中有没有当前key
                Boolean hasKey = redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).hasKey(seckillGood.getSkuId().toString());
                if(hasKey){
                    continue;
                }
                // 缓存中没有，将当前秒杀商品存入缓存
                redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).put(seckillGood.getSkuId().toString(),seckillGood);
                //根据每一个商品的数量把商品按队列的形式放进redis中
                for (int i = 0; i < seckillGood.getStockCount() ; i++) {
                    redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX + seckillGood.getSkuId()).leftPush(seckillGood.getSkuId().toString());
                }
                // 通知添加与更新状态位，更新为开启
                redisTemplate.convertAndSend("seckillpush",seckillGood.getSkuId()+":1");
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }
    }

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value =MqConst.QUEUE_SECKILL_USER,durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_SECKILL_USER),
            key = {MqConst.ROUTING_SECKILL_USER}
    ))
    public void seckill(UserRecode userRecode, Message message, Channel channel){
        if(null != userRecode){
            // 下单
            seckillGoodsService.seckillOrder(userRecode.getSkuId(), userRecode.getUserId());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }
    }

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_18, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK ),
            key = {MqConst.ROUTING_TASK_18}
    ))
    public void clearRedis(Message message,Channel channel){
        QueryWrapper<SeckillGoods> seckillGoodsQueryWrapper = new QueryWrapper<>();
        seckillGoodsQueryWrapper.eq("status",1);
        seckillGoodsQueryWrapper.lt("end_time",new Date());
        List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(seckillGoodsQueryWrapper);
        // 清空缓存
        for (SeckillGoods seckillGoods : seckillGoodsList) {
            redisTemplate.delete(RedisConst.SECKILL_STOCK_PREFIX + seckillGoods.getSkuId());
        }
            redisTemplate.delete(RedisConst.SECKILL_GOODS);
            redisTemplate.delete(RedisConst.SECKILL_ORDERS);
            redisTemplate.delete(RedisConst.SECKILL_ORDERS_USERS);
            // 将状态更新为结束
            SeckillGoods seckillGoodsUp = new SeckillGoods();
            seckillGoodsUp.setStatus("2");
            seckillGoodsMapper.update(seckillGoodsUp,seckillGoodsQueryWrapper);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
