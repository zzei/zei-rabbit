package com.zei.rabbit.receive;

/**
 * <p>
 *  消费端消息处理实现共用接口
 * </p>
 *
 * @author zei
 * @since 2020年05月23日23:02:13
 */
public interface DoMQService<T> {

    boolean doMQService(Object data);
}
