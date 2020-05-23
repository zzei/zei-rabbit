package com.zei.rabbit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * <p>
 *  消费端消息存储实体
 * </p>
 *
 * @author zei
 * @since 2020年05月23日
 */
@Data
@Accessors(chain = true)
@TableName("rabbit_consumer_message")
public class RabbitConsumerMessage {

    @TableId(
            value = "id",
            type = IdType.AUTO
    )
    private Integer id;

    @TableField("message_id")
    private String messageId;

    /**
     * 0:处理失败 1:处理中 2:处理完成
     */
    @TableField("mq_state")
    private Integer mqState;

    /**
     * 0: no ack 1: ack 2:doing
     */
    @TableField("ack")
    private Integer ack;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

}
