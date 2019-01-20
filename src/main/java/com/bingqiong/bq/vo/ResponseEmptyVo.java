package com.bingqiong.bq.vo;

import com.bingqiong.bq.constant.BqErrorCode;
import com.jfinal.kit.JsonKit;

import java.io.Serializable;

/**
 * Created by hunsy on 2017/3/29.
 */
public class ResponseEmptyVo extends ResponseVoAdapter implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 321467445037866072L;
    private int responseCode;
    private String errorMsg;

    public ResponseEmptyVo() {
    }

    public ResponseEmptyVo(int code, String msg) {
        this.responseCode = code;
        this.errorMsg = msg;
        logger.info("response data:{}", JsonKit.toJson(this));
    }

    public ResponseEmptyVo(BqErrorCode bqErrorCode) {
        this.responseCode = bqErrorCode.getCode();
        this.errorMsg = bqErrorCode.getMessage();
        logger.info("response data:{}", JsonKit.toJson(this));
    }

    /**
     * 获取没有返回数据的成功。
     *
     * @return
     */
    public static ResponseEmptyVo success() {
        return new ResponseEmptyVo(BqErrorCode.SUCCESS);
    }

    /**
     * 获取没有返回数据的失败。
     *
     * @param bqErrorCode
     * @return
     */
    public static ResponseEmptyVo failure(BqErrorCode bqErrorCode) {
        return new ResponseEmptyVo(bqErrorCode);
    }

    /**
     * 获取没有返回数据的失败。
     *
     * @param code
     * @param errMsg
     * @return
     */
    public static ResponseEmptyVo failure(int code, String errMsg) {
        return new ResponseEmptyVo(code, errMsg);
    }


    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
