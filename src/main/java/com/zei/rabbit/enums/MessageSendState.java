package com.zei.rabbit.enums;

import lombok.Getter;

/**
 * <p>
 *  发送端消息状态
 * </p>
 *
 * @author zei
 * @since 2020年05月23日
 */
@Getter
public enum MessageSendState {

    SEND_FAIL(0, "发送失败"),
    NORMAL(1, "初试状态"),
    SEND_SUCCESS(2, "已发送");

    private Integer code;

    private String msg;

    MessageSendState(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
