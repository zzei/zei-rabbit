package com.zei.rabbit.dao;

import com.zei.rabbit.entity.RabbitConsumerMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * <p>
 *
 * </p>
 *
 * @author zei
 * @since 2020年05月23日
 */
@Repository
public interface RabbitConsumerMapper {

    @Insert("insert into rabbit_consumer_message(message_id, mq_state, ack, create_time, update_time) " +
            "values (#{messageId}, #{mqState}, #{ack}, #{createTime}, #{updateTime})")
    int insert(RabbitConsumerMessage message);

    @Update("update rabbit_consumer_message set mq_state = #{mqState}, ack = #{ack}, update_time = #{updateTime} where message_id = #{messageId}")
    int update(RabbitConsumerMessage message);

    @Select("select id, message_id, mq_state from rabbit_consumer_message where message_id = #{messageId} and mq_state != 0")
    List<RabbitConsumerMessage> getById(String messageId);

}
