package com.zei.rabbit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 *  发送端消息载体
 * </p>
 *
 * @author zei
 * @since 2020年05月23日
 */
@Data
@Accessors(chain = true)
@TableName("rabbit_message")
public class RabbitMessage implements Serializable {

    private static final long serialVersionUID = -8541052766905788937L;

    @TableId(
        value = "id",
        type = IdType.AUTO
    )
    private Integer id;

    @TableField("message_id")
    private String messageId;

    /**
     * 队列名
     */
    @TableField("queue_name")
    private String queueName;

    /**
     * 0:发送失败 1:初始状态 2:已发送 3:消费成功 4:消费失败
     */
    @TableField("mq_state")
    private Integer mqState;

    /**
     * 发送次数
     */
    @TableField("send_num")
    private Integer sendNum;

    /**
     * json数据
     */
    @TableField("data")
    private String data;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;
}
