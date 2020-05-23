package com.zei.rabbit.enums;

import lombok.Getter;

/**
 * <p>
 *  消费端消息状态
 * </p>
 *
 * @author zei
 * @since 2020-05-23
 */
@Getter
public enum MessageReceiveState {

    TAKE_FAIL(0, "处理失败"),
    TAKING(1, "处理中"),
    TAKE_SUCCESS(2, "处理完成");

    private Integer code;

    private String msg;

    MessageReceiveState(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
