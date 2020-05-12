package com.atguigu.gmall.common.config.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author yizhixld
 * @create 2020-05-05-21:59
 */
@Service
public class RabbitService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
    * @Description: //发送消息(交换机，路由键，消息)
    * @Return:
    **/
    public boolean sendMessage(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        return true;
    }
    // 发送延迟消息
    public boolean sendDelayMessage(String exchange,String routingKey,Object message,int delayTime){
        rabbitTemplate.convertAndSend(exchange,routingKey,message,message1 -> {
            message1.getMessageProperties().setDelay(delayTime*1000);
            return message1;
        });
        return true;
    }
}
