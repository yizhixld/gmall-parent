package com.atguigu.gmall.order.receiver;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.config.constant.MqConst;
import com.atguigu.gmall.common.config.service.RabbitService;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.config.OrderCanelMqConfig;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.payment.client.PaymentFeignClient;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;


/**
 * @author yizhixld
 * @create 2020-05-07-10:57
 */
@Component
public class OrderReceiver {
    @Autowired
    private OrderService orderService;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private PaymentFeignClient paymentFeignClient;
    /**
    * @Description: //取消订单（延迟队列）
    * @Return:
    **/
    @SneakyThrows
    @RabbitListener(queues = MqConst.QUEUE_ORDER_CANCEL)
    public void orderCancel(Long orderId, Message message, Channel channel){
        if(null != orderId){
            OrderInfo orderInfo = orderService.getById(orderId);
            if(null != orderInfo && orderInfo.getOrderStatus().equals(ProcessStatus.UNPAID.getOrderStatus().name())){
                // 先关闭paymentInfo 后关闭orderInfo,因为 支付成功之后，异步回调先修改的paymentInfo,然后在发送的异步通知修改订单的状态。
                // 关闭前先查看是否有交易数据，没有就不需要关闭paymentInfo
                PaymentInfo paymentInfo = paymentFeignClient.getPaymentInfo(orderInfo.getOutTradeNo());
                if(null != paymentInfo && paymentInfo.getPaymentStatus().equals(PaymentStatus.UNPAID.name())){
                    // 查看支付宝交易记录
                    Boolean flag = paymentFeignClient.checkPayment(orderId);
                    if(flag){
                        // 关闭支付宝交易
                        Boolean aBoolean = paymentFeignClient.closePay(orderId);
                        if(aBoolean){
                            // 关闭支付宝的订单成功 关闭 OrderInfo 表,paymentInfo
                            orderService.execExpiredOrder(orderId,"2");
                        }else {
                            // 关闭支付宝订单失败，说明已经支付
                            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_PAYMENT_PAY,MqConst.ROUTING_PAYMENT_PAY,orderId);
                        }
                    }
                    // 支付宝中无记录直接关闭OrderInfo 表,paymentInfo
                    else {
                        orderService.execExpiredOrder(orderId,"2");
                    }
                }
                // paymentInfo中无数据，直接关闭OrderInfo
                else {
                    orderService.execExpiredOrder(orderId,"1");
                }
            }
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_PAYMENT_PAY,durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_PAYMENT_PAY),
            key = {MqConst.ROUTING_PAYMENT_PAY}
    ))
    public void paySuccess(Long orderId,Message message,Channel channel){
        if(null != orderId){
            OrderInfo orderInfo = orderService.getById(orderId);
            // 防止重复消费
            if(null != orderInfo && orderInfo.getOrderStatus().equals(ProcessStatus.UNPAID.getOrderStatus().name())){
                // 修改订单状态为已支付
                orderService.updateOrderStatus(orderId, ProcessStatus.PAID);
                // 通知库存系统减库存
                orderService.sendOrderStatus(orderId);
            }
        }
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_WARE_ORDER,durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_WARE_ORDER),
            key = {MqConst.ROUTING_WARE_ORDER}
    ))
    public void updateOrderStatus(String msgJson, Message message, Channel channel){
        if(!StringUtils.isEmpty(msgJson)){
            Map<String,Object> map = JSON.parseObject(msgJson, Map.class);
            String orderId = (String) map.get("orderId");
            String status = (String) map.get("status");
            if("DEDUCTED".equals(status)){
                // 减库存成功！ 修改订单状态为已支付
                orderService.updateOrderStatus(Long.parseLong(orderId),ProcessStatus.WAITING_DELEVER);
            }else {
                // 减库存失败！远程调用其他仓库查看是否有库存
                orderService.updateOrderStatus(Long.parseLong(orderId),ProcessStatus.STOCK_EXCEPTION);
            }
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

}
