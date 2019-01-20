package com.bingqiong.bq.exception;

/**
 * **********************************************************
 * 内容摘要	：<p>
 * <p>
 * 作者	：94841
 * 创建时间	：2016年4月20日 下午4:45:16
 * 当前版本号：v1.0
 * 历史记录	:
 * 日期	: 2016年4月20日 下午4:45:16 	修改人：niuzan
 * 描述	:
 * **********************************************************
 */
public class UBizException extends RuntimeBaseException {
    /**
     * serialVersionUID:TODO
     * 字段
     *
     * @since Ver 1.1
     */

    private static final long serialVersionUID = -2912030318652131908L;

    /**
     * 异常码
     */
    private String errorCode;

    public String getErrorCode() {
        return errorCode;
    }

    public UBizException() {
        super();
    }

    public UBizException(Exception exception) {
        super(exception);
    }

    public UBizException(String errorCode) {
        this.errorCode = errorCode;
    }

    public UBizException(String msg, Exception e) {
        super(msg, e);
    }

    public UBizException(String errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }

    public UBizException(String errorCode, String msg, Throwable e) {
        super(msg, e);
        this.errorCode = errorCode;
    }

}
