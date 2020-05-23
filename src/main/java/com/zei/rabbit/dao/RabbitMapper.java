package com.zei.rabbit.dao;

import com.zei.rabbit.entity.RabbitMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
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
 * @since 2020年05月23日23:01:57
 */
@Repository
public interface RabbitMapper {
    
    @Insert("insert into rabbit_message(message_id, queue_name, mq_state, send_num, data, create_time, update_time) " +
            "values (#{messageId}, #{queueName}, #{mqState}, #{sendNum}, #{data}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    int insert(RabbitMessage message);

    @Update("update rabbit_message set mq_state = #{mqState}, send_num = send_num + 1, update_time = #{updateTime} where message_id = #{messageId}")
    int update(RabbitMessage message);

    @Select("select id, message_id from rabbit_message where message_id = #{messageId}")
    RabbitMessage getById(String messageId);

    @Select("select id, message_id, queue_name, mq_state, data from rabbit_message where mq_state = #{reSendState} and send_num < #{sendNum}")
    List<RabbitMessage> queryUnMessage(Integer reSendState, Integer sendNum);

}
