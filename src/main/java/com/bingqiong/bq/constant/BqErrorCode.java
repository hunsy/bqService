
package com.bingqiong.bq.constant;

/**
 * **********************************************************
 * 内容摘要	：<p>
 * <p>
 * 作者	：94841
 * 创建时间	：2016年4月28日 上午11:37:57
 * 当前版本号：v1.0
 * 历史记录	:
 * 日期	: 2016年4月28日 上午11:37:57 	修改人：niuzan
 * 描述	:
 * **********************************************************
 */
public enum BqErrorCode {
    /**
     * 成功
     */
    SUCCESS(1000, ""),
    /**
     * 没有登录
     */
    ACCESS_REJECT(1004, "没有登录"),
    /**
     * 系统异常
     */
    CODE_FAILED(1005, "系统异常");


    public int code;

    public String message;

    private BqErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static final BqErrorCode getFromKey(int code) {
        for (BqErrorCode e : BqErrorCode.values()) {
            if (e.getCode() == code) {
                return e;
            }
        }
        return null;
    }

    public static final BqErrorCode getFromValue(String message) {
        for (BqErrorCode e : BqErrorCode.values()) {
            if (e.getMessage().equals(message)) {
                return e;
            }
        }
        return null;
    }

}
