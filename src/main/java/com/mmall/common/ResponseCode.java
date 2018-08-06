package com.mmall.common;

/**
 * Created by guo on 2018/8/1.
 */
public enum ResponseCode {
    SUCCESS(0, "success"), ERROR(1, "ERROR"), NEED_LOGIN(10, "NEED_LOGIN"), ILLGEL_ARGUMENT(2, "ILLGEL_ARGUMENT");
    int code;
    String desc;

    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
