package com.bingqiong.bq.vo;

import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.utils.DESUtil;
import com.jfinal.kit.JsonKit;

import java.io.Serializable;

/**
 * Created by hunsy on 2017/3/21.
 */
public class ResponseMobileDataVo extends ResponseEmptyVo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3321789784808509787L;
    private Object data;

    public ResponseMobileDataVo(int code, Object data, String msg) {
        super.setResponseCode(code);
        super.setErrorMsg(msg);
        this.data = data;
//        logger.info("response data:{}", JsonKit.toJson(this));
    }

    public ResponseMobileDataVo(BqErrorCode bqErrorCode, Object data) {
        this(bqErrorCode.getCode(), data, bqErrorCode.getMessage());
    }

    /**
     * 获取有返回数据的成功。
     *
     * @param data
     * @return
     */
    public static ResponseMobileDataVo success(Object data) {
        return new ResponseMobileDataVo(BqErrorCode.SUCCESS, data);
    }

    public static ResponseMobileDataVo success(Object data, boolean encode) throws Exception {
        String str = JsonKit.toJson(data);
//        logger.info("response before encrypt:{}", str);
        if (encode) {
            data = DESUtil.encrypt(str);
        } else {
            data = str;
        }
        return new ResponseMobileDataVo(BqErrorCode.SUCCESS, data);
    }

    /**
     * 获取有返回数据的成功。
     *
     * @param msg
     * @param data
     * @return
     */
    public static ResponseMobileDataVo success(String msg, Object data) {
        return new ResponseMobileDataVo(BqErrorCode.SUCCESS.getCode(), data, msg);
    }


    /**
     * 获取有返回数据的失败。
     *
     * @param bqErrorCode
     * @param data
     * @return
     */
    public static ResponseMobileDataVo failure(BqErrorCode bqErrorCode, Object data) {
        return new ResponseMobileDataVo(bqErrorCode, data);
    }

    /**
     * 获取有返回数据的失败。
     *
     * @param code
     * @param errMsg
     * @param data
     * @return
     */
    public static ResponseMobileDataVo failure(int code, String errMsg, Object data) {
        return new ResponseMobileDataVo(code, data, errMsg);
    }


    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}