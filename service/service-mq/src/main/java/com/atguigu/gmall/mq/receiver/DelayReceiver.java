package com.atguigu.gmall.mq.receiver;

import com.atguigu.gmall.mq.config.DelayedMqConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author yizhixld
 * @create 2020-05-07-10:18
 */
@Component
@Configuration
public class DelayReceiver {
    @RabbitListener(queues = DelayedMqConfig.queue_delay_1)
    public void get(String msg){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("Receive queue_delay_1: " + sdf.format(new Date()) + " Delay rece." + msg);
    }
}
