package com.bingqiong.bq.exception;

/**
 * Created by hunsy on 2017/3/31.
 */
public class BizException extends Exception {

    private static final long serialVersionUID = -2494016349099779638L;
    /**
     * 异常码
     */
    private int errorCode;

    public int getErrorCode() {
        return errorCode;
    }

    public BizException() {
        super();
    }

    public BizException(Exception exception) {
        super(exception);
    }

    public BizException(String errorCode) {
        super(errorCode);
    }

    public BizException(int errorCode) {
        super(errorCode + "");
        this.errorCode = errorCode;
    }

    public BizException(String msg, Exception e) {
        super(msg, e);
    }

    public BizException(int errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }

    public BizException(int errorCode, String msg, Throwable e) {
        super(msg, e);
        this.errorCode = errorCode;
    }
}
