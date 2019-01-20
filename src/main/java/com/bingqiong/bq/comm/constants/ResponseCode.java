package com.bingqiong.bq.comm.constants;

/**
 * 响应返回的code
 * <p>
 * Created by hunsy on 2017/6/21.
 */
public enum ResponseCode {
    /**
     * 成功
     */
    SUCCESS(1000, ""),
    /**
     * 没有登录
     */
    ACCESS_REJECT(1004, "请重新登录"),
    /**
     * 系统异常
     */
    CODE_FAILED(1005, "系统异常");

    private int code;
    private String msg;

    ResponseCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
