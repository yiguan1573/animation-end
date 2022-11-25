package com.yiguan.bean.enums;

/**
 * @Author: lw
 * @CreateTime: 2022-10-06  15:34
 * @Description: TODO
 * @Version: 1.0
 */
public enum ResponseCode {
    SUCCESS(200,"SUCCESS"),
    ERROR(400,"ERROR");
    private final int code;
    private final String desc;

    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc =desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}

