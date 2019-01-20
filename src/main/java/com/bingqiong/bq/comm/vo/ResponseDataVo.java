package com.bingqiong.bq.comm.vo;

import com.alibaba.fastjson.JSON;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.constants.ResponseCode;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Created by hunsy on 2017/3/21.
 */
public class ResponseDataVo extends ResponseEmptyVo implements Serializable {

    private static Logger logger = LoggerFactory.getLogger(ResponseDataVo.class);
    /**
     *
     */
    private static final long serialVersionUID = -7719887125392089065L;
    private Object data;

    public ResponseDataVo(Object data, int responseCode, int code, String msg) {

        super(responseCode, code, msg);
        this.data = data;
//        logger.info("response data:{}", JSON.toJSONString(this));
    }

    public ResponseDataVo(Object data, ResponseCode responseCode, ErrorCode errorCode) {
        super(responseCode, errorCode);
        if (data instanceof Page) {
            Page<?> page = (Page<?>) data;
            data = new RespData(page.getTotalRow(), page.getList() != null ? JSON.parseArray(JsonKit.toJson(page.getList())) : null);
        }
        this.data = data;
//        logger.info("response data:{}", JSON.toJSONString(this));
    }

    /**
     * 获取有返回数据的成功。
     *
     * @param data
     * @return
     */
    public static ResponseDataVo success(Object data) {
        return new ResponseDataVo(data, ResponseCode.SUCCESS, ErrorCode.SUCCESS);
    }


    /**
     * 获取有返回数据的失败。
     *
     * @param errorCode
     * @param data
     * @return
     */
    public static ResponseDataVo failure(ErrorCode errorCode, Object data) {
        return new ResponseDataVo(data, ResponseCode.CODE_FAILED, errorCode);
    }

    /**
     * 获取有返回数据的失败。
     *
     * @param code
     * @param errMsg
     * @param data
     * @return
     */
    public static ResponseDataVo failure(int code, String errMsg, Object data) {
        return new ResponseDataVo(data, ResponseCode.CODE_FAILED.getCode(), code, errMsg);
    }


    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}