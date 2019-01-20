package com.bingqiong.bq.comm.vo;

import com.alibaba.fastjson.JSON;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.constants.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Created by hunsy on 2017/3/29.
 */
public class ResponseEmptyVo extends ResponseVoAdapter implements Serializable {

    private static Logger logger = LoggerFactory.getLogger(ResponseEmptyVo.class);
    /**
     *
     */
    private static final long serialVersionUID = 321467445037866072L;
    private int responseCode;
    private int code;
    private String errorMsg;



    public ResponseEmptyVo(int responseCode, int code, String msg) {
        this.responseCode = responseCode;
        this.code = code;
        this.errorMsg = msg;
//        logger.info("response data:{}", JSON.toJSONString(this));
    }

    public ResponseEmptyVo(ResponseCode response, ErrorCode errorCode) {
        this.responseCode = response.getCode();
        this.code = errorCode.getCode();
        this.errorMsg = errorCode.getMsg();
    }


    /**
     * 获取没有返回数据的成功。
     *
     * @return
     */
    public static ResponseEmptyVo success() {
        return new ResponseEmptyVo(ResponseCode.SUCCESS, ErrorCode.SUCCESS);
    }

    /**
     * 获取没有返回数据的失败。
     *
     * @param errorCode
     * @return
     */
    public static ResponseEmptyVo failure(ErrorCode errorCode) {
        return new ResponseEmptyVo(ResponseCode.CODE_FAILED, errorCode);
    }

    /**
     * 获取没有返回数据的失败。
     *
     * @param code
     * @param errMsg
     * @return
     */
    public static ResponseEmptyVo failure(int code, String errMsg) {

        return new ResponseEmptyVo(ResponseCode.CODE_FAILED.getCode(), code, errMsg);
    }

    /**
     * 没有权限
     *
     * @return
     */
    public static ResponseEmptyVo noauth() {

        return new ResponseEmptyVo(ResponseCode.ACCESS_REJECT.getCode(), -1, ResponseCode.ACCESS_REJECT.getMsg());
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

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
