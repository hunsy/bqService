package com.bingqiong.bq.comm.exception;


import com.bingqiong.bq.comm.constants.ErrorCode;

/**
 * 业务处理自定义异常类
 */
public class BizException extends Exception {

    /**
     * 异常码
     */
    private ErrorCode code;

    public BizException(ErrorCode errorCode) {
        this.code = errorCode;
    }

    public BizException() {
        super();
    }

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }

    public BizException(Throwable cause) {
        super(cause);
    }

    public ErrorCode getCode() {
        return code;
    }
}
