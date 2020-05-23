package com.zei.rabbit.schedule;

import com.zei.rabbit.dao.RabbitConsumerMapper;
import com.zei.rabbit.dao.RabbitMapper;
import com.zei.rabbit.entity.RabbitMessage;
import com.zei.rabbit.enums.MessageSendState;
import com.zei.rabbit.sender.RabbitSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 *  消息补偿机制之定时任务
 * </p>
 *
 * @author zei
 * @since 2020年05月23日
 */
@Slf4j
@Component
public class RabbitSendSchedule {

    @Autowired
    RabbitSender rabbitSender;

    @Autowired
    RabbitMapper rabbitMapper;

    @Autowired
    RabbitConsumerMapper rabbitConsumerMapper;

    /**
     * 消息重发机制
     * 每隔30秒发送一次
     */
    @Scheduled(cron = "0/30 0/1 * * * ?")
    public void send() {
        //对发送失败和消费失败的消息进行补偿发送,超过10次则不再处理
        List<RabbitMessage> rabbitMessages = rabbitMapper.queryUnMessage(MessageSendState.SEND_FAIL.getCode(), 10);
        if (rabbitMessages != null && rabbitMessages.size() > 0) {
            for (RabbitMessage rabbitMessage : rabbitMessages) {
                log.info("发送未成功发送/消费的消息,messageId: {}, 状态state: {}", rabbitMessage.getMessageId(), rabbitMessage.getMqState());
                rabbitSender.send(rabbitMessage.getQueueName(), rabbitMessage.getData(), rabbitMessage.getMessageId());
            }
        }
    }

}
