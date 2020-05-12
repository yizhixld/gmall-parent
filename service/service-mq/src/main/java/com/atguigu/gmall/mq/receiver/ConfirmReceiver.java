package com.atguigu.gmall.mq.receiver;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author yizhixld
 * @create 2020-05-05-22:08
 */
@Component
@Configuration
public class ConfirmReceiver {
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "queue.confirm",autoDelete = "false"),
            exchange = @Exchange(value = "exchange.confirm",autoDelete = "ture"),
            key = {"routing.confirm"}
    ))
    public void process(Message message, Channel channel){
        System.out.println("RabbitListener:"+new String(message.getBody()));
        try {
            //int i = 1/0;
            // false 确认一个消息，true 批量确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            //判断是否已经处理过一次消息
           if(message.getMessageProperties().getRedelivered()){
               System.out.println("消息已重复处理,拒绝再次接收");
                // 拒绝消息，requeue=false 表示不再重新入队
               channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
           }else{
               System.out.println("消息即将再次返回队列处理");
               // 数二：是否批量， 参数三：为是否重新回到队列，true重新入队
               channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
           }
        }
    }
}
