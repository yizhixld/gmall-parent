package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.atguigu.gmall.common.config.service.RabbitService;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author yizhixld
 * @create 2020-05-05-22:01
 */
@RestController
@RequestMapping("/mq")
@Slf4j
public class MqController {
    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("sendConfirm")
    public Result sendConfirm(){
        String message = "helloMQ!";
        rabbitService.sendMessage("exchange.confirm", "routing.confirm",message);
        return Result.ok();
    }

    @GetMapping("sendDeadLettle")
    public Result sendDeadLettle(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        rabbitTemplate.convertAndSend(DeadLetterMqConfig.exchange_dead,DeadLetterMqConfig.routing_dead_1,"helloMQ",message -> {
            message.getMessageProperties().setExpiration(1*10*1000 + "");
            System.out.println(simpleDateFormat.format(new Date() )+ "Delay sent");
            return message;
        });
        return Result.ok();
    }
    // 基于延迟插件实现延迟消息
    @GetMapping("sendDeadLettleCJ")
    public Result sendDeadLettleCJ(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        rabbitTemplate.convertAndSend(DelayedMqConfig.exchange_delay, DelayedMqConfig.routing_delay, "helloMq",new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setDelay(1*10*1000);
                System.out.println(simpleDateFormat.format(new Date() )+ "Delay sent");
                return message;
            }
        });
        return Result.ok();
    }


}
