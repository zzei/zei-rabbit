package com.zei.rabbit.receive;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.zei.rabbit.config.RabbitQueueName;
import com.zei.rabbit.dao.RabbitConsumerMapper;
import com.zei.rabbit.entity.RabbitConsumerMessage;
import com.zei.rabbit.entity.RabbitMessage;
import com.zei.rabbit.enums.MessageReceiveState;
import com.zei.rabbit.sender.RabbitSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 消息消费端 消费消息api receiveXXX
 * </p>
 *
 * @author zei
 * @since 2020年05月23日
 */
@Slf4j
@Component
public class RabbitReceive {

    @Autowired
    RabbitSender rabbitSender;

    @Autowired
    RabbitConsumerMapper rabbitConsumerMapper;


    /**
     * 处理消费队列， 只进行消息确认
     *
     * @param message
     * @param channel
     * @param clazz
     * @param doMQService
     */
    public void receiveMQonlyCheck(org.springframework.amqp.core.Message message, Channel channel, TypeReference clazz, DoMQService<Boolean> doMQService) {
        byte[] body = message.getBody();
        String messageJson = null;
        try {
            messageJson = new String(body, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("mq信息格式不正确, cause:{}, message:{}", e.getCause(), e.getMessage());
        }
        //手工ack
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (doMQService.doMQService(JSON.parseObject(messageJson, clazz))) {
            log.info("消息消费成功！");
        } else {
            log.error("消息消费失败！");
        }
    }

    /**
     * 处理消费队列共用方法 onMessage使用
     * 1.解决消息幂等问题
     * 2.对发送成功的消息进行确认
     *
     * @param message
     * @param channel
     * @param clazz
     * @param doMQService
     */
    public void receiveMQ(org.springframework.amqp.core.Message message, Channel channel, TypeReference clazz, DoMQService<Boolean> doMQService) {
        byte[] body = message.getBody();
        String messageJson = null;
        try {
            messageJson = new String(body, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("mq信息格式不正确, cause:{}, message:{}", e.getCause(), e.getMessage());
        }
        RabbitMessage rabbitMessage = JSON.parseObject(messageJson, RabbitMessage.class);
        //手动ack
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        doMQ(rabbitMessage, clazz, doMQService);
    }


    /**
     * 处理消费队列共用方法
     * 1.解决消息幂等问题
     * 2.对发送成功的消息进行确认
     *
     * @param rabbitMessageJSON
     * @param doMQService
     */
    public void receiveMQ(String rabbitMessageJSON, Message message, Channel channel, TypeReference clazz, DoMQService<Boolean> doMQService) {
        RabbitMessage rabbitMessage = JSON.parseObject(rabbitMessageJSON, RabbitMessage.class);
        //手动ack
        try {
            channel.basicAck((Long) message.getHeaders().get(AmqpHeaders.DELIVERY_TAG), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        doMQ(rabbitMessage, clazz, doMQService);
    }

    /**
     * 消费端处理逻辑
     *
     * @param rabbitMessage
     * @param clazz
     * @param doMQService
     */
    private void doMQ(RabbitMessage rabbitMessage, TypeReference clazz, DoMQService<Boolean> doMQService) {
        log.info("处理 {} 消息, messageId: {}", rabbitMessage.getQueueName(), rabbitMessage.getMessageId());
        //消息幂等处理
        List<RabbitConsumerMessage> rabbitConsumerMessages = rabbitConsumerMapper.getById(rabbitMessage.getMessageId());
        if (rabbitConsumerMessages == null || rabbitConsumerMessages.size() == 0) {
            //新增一条消费记录
            RabbitConsumerMessage rabbitConsumerMessage = new RabbitConsumerMessage()
                    .setMessageId(rabbitMessage.getMessageId())
                    .setMqState(MessageReceiveState.TAKING.getCode())
                    .setAck(2)
                    .setCreateTime(new Date()).setUpdateTime(new Date());

            rabbitConsumerMapper.insert(rabbitConsumerMessage);
            //调用业务逻辑
            boolean flag = false;
            try {
                flag = doMQService.doMQService(JSON.parseObject(rabbitMessage.getData(), clazz));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (flag) {
                log.info("消息 {}, messageId: {} 消费成功", rabbitMessage.getQueueName(), rabbitMessage.getMessageId());
                rabbitConsumerMessage.setAck(1).setMqState(MessageReceiveState.TAKE_SUCCESS.getCode()).setUpdateTime(new Date());
                rabbitConsumerMapper.update(rabbitConsumerMessage);
            } else {
                log.error("消息 {}, messageId: {} 消费失败", rabbitMessage.getQueueName(), rabbitMessage.getMessageId());
                rabbitConsumerMessage.setAck(1).setMqState(MessageReceiveState.TAKE_FAIL.getCode()).setUpdateTime(new Date());
                rabbitConsumerMapper.update(rabbitConsumerMessage);
            }
        } else {
            log.info("消息messageId: {} 已接收，不进行重复处理!", rabbitMessage.getMessageId());
        }
    }
}
