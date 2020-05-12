package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author yizhixld
 * @create 2020-05-04-21:44
 */
public interface OrderService extends IService<OrderInfo> {
    /**
    * @Description: //保存订单
    * @Return:
    **/
    Long saveOrderInfo(OrderInfo orderInfo);
    /**
    * @Description: //生成流水号
    * @Return:
    **/
    String getTradeNo(String userId);
    /**
    * @Description: //比较流水号，用来防止回退重复提交订单
    * @Return:
    **/
    boolean checkTradeCode(String userId, String tradeCodeNo);
    /**
    * @Description: //删除缓存中流水号
    * @Return:
    **/
    void deleteTradeNo(String userId);
    /**
    * @Description: //验证库存
    * @Return:
    **/
    boolean checkStock(Long skuId, Integer skuNum);
    /**
    * @Description: //处理过期订单
    * @Return:
    **/
    void execExpiredOrder(Long orderId);
    /**
    * @Description: //查询订单信息
    * @Return:
    **/
    OrderInfo getOrderInfo(Long orderId);
    /**
    * @Description: //通知库存系统减库存
    * @Return:
    **/
    void sendOrderStatus(Long orderId);
    /**
    * @Description: //根据订单id，更新订单状态
    * @Return:
    **/
    void updateOrderStatus(Long orderId, ProcessStatus paid);
    /**
    * @Description: //拆单
    * @Return:
    **/
    List<OrderInfo> orderSplit(long orderId, String wareSkuMap);
    /**
    * @Description: //将orderInfo转为map
    * @Return:
    **/
    Map initWareOrder(OrderInfo orderInfo);
    /**
    * @Description: //更新过期订单
    * @Return:
    **/
    void execExpiredOrder(Long orderId, String flag);
}
