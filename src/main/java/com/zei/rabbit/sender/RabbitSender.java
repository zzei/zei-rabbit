package com.zei.rabbit.sender;

import com.alibaba.fastjson.JSON;
import com.zei.rabbit.dao.RabbitMapper;
import com.zei.rabbit.entity.RabbitMessage;
import com.zei.rabbit.enums.MessageSendState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;


/**
 * <p>
 *  生产者端消息发送api send
 * </p>
 *
 * @author zei
 * @since 2020年05月23日
 */
@Slf4j
@Component
public class RabbitSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMapper rabbitMapper;

    //消息确认
    final RabbitTemplate.ConfirmCallback confirmCallback = (correlationData, ack, cause) -> {
        if (ack) {
            log.info("messageId: {} 发送成功！", correlationData.getId());
            rabbitMapper.update(new RabbitMessage().setMessageId(correlationData.getId()).setMqState(MessageSendState.SEND_SUCCESS.getCode()).setUpdateTime(new Date()));
        } else {
            //消息确认失败，做消息状态变更，由定时器去进行重试
            log.error("messageId: {} 发送失败，原因：{}", correlationData.getId(), cause);
            rabbitMapper.update(new RabbitMessage().setMessageId(correlationData.getId()).setMqState(MessageSendState.SEND_FAIL.getCode()).setUpdateTime(new Date()));
        }
    };

    /**
     * 消息发送方法
     * @param queueName 队列名称
     * @param message   消息主体(json
     */
    public boolean send(String queueName, Object message) throws Exception {
        //消息主题转换json
        String messageData = JSON.toJSONString(message);
        //存储消息记录
        String messageId = UUID.randomUUID().toString();
        RabbitMessage sendMsg = new RabbitMessage();
        sendMsg.setMessageId(messageId).setQueueName(queueName).setData(messageData);
        if (!saveMessage(sendMsg)) {
            return false;
        }
        rabbitTemplate.setConfirmCallback(confirmCallback);
        //设置发送参数
        CorrelationData cd = new CorrelationData();
        cd.setId(messageId);
        rabbitTemplate.convertAndSend("", queueName, JSON.toJSONString(sendMsg), cd);
        return true;
    }

    /**
     * 消息重发送方法
     * @param queueName 队列名称
     * @param message   消息主体(json)
     */
    public void send(String queueName, Object message, String messageId) {
        //消息主题转换json
        String messageData = JSON.toJSONString(message);
        //存储消息记录
        RabbitMessage sendMsg = new RabbitMessage();
        sendMsg.setMessageId(messageId).setQueueName(queueName).setData(messageData);

        rabbitTemplate.setConfirmCallback(confirmCallback);
        //设置发送参数
        CorrelationData cd = new CorrelationData();
        cd.setId(messageId);
        rabbitTemplate.convertAndSend("", queueName, JSON.toJSONString(sendMsg), cd);
    }

    /**
     * 消息广播模式
     * @param exchange
     * @param message
     */
    public void sendFanout(String exchange, Object message) {
        //消息主题转换json
        String messageData = JSON.toJSONString(message);
        //存储消息记录
        String messageId = UUID.randomUUID().toString();
        RabbitMessage sendMsg = new RabbitMessage();
        sendMsg.setMessageId(messageId).setQueueName(exchange).setData(messageData);

        rabbitTemplate.setConfirmCallback(confirmCallback);
        //设置发送参数
        CorrelationData cd = new CorrelationData();
        cd.setId(messageId);
        rabbitTemplate.convertAndSend(exchange, "", JSON.toJSONString(sendMsg), cd);
    }

    private boolean saveMessage(RabbitMessage sendMsg) throws Exception{
        RabbitMessage message = rabbitMapper.getById(sendMsg.getMessageId());
        if (message == null) {
            sendMsg.setMqState(1);
            sendMsg.setSendNum(0);
            sendMsg.setCreateTime(new Date());
            sendMsg.setUpdateTime(new Date());
            return rabbitMapper.insert(sendMsg) > 0;
        }
        return true;
    }
}
