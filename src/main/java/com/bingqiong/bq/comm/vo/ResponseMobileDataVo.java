package com.bingqiong.bq.comm.vo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.constants.ResponseCode;
import com.bingqiong.bq.comm.utils.DESUtil;
import com.bingqiong.bq.model.comm.Sensitive;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * 手机加密数据
 * Created by hunsy on 2017/3/21.
 */
public class ResponseMobileDataVo extends ResponseEmptyVo implements Serializable {

    private static Logger logger = LoggerFactory.getLogger(ResponseMobileDataVo.class);

    /**
     *
     */
    private static final long serialVersionUID = 3321789784808509787L;
    private Object data;


    public ResponseMobileDataVo(Object data, int responseCode, int code, String msg) {

        super(responseCode, code, msg);
        this.data = data;
//        logger.info("response data:{}", JSON.toJSONString(this));
    }

    public ResponseMobileDataVo(Object data, ResponseCode responseCode, ErrorCode errorCode) {
        super(responseCode, errorCode);
        this.data = data;
//        logger.info("response data:{}", JSON.toJSONString(this));
    }


    public static ResponseMobileDataVo success(Object data, boolean enc) {


//        try {
//            logger.info(Sensitive.dao.filterSensitive(JsonKit.toJson(data)));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        if (enc) {
            String str = JsonKit.toJson(data);
//            try {
//                str = Sensitive.dao.filterSensitive(str);
////                logger.info("过滤后的文字");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            logger.info("response before encrypt:{}", str);
            data = DESUtil.encrypt(str);
        }
        return new ResponseMobileDataVo(data, ResponseCode.SUCCESS, ErrorCode.SUCCESS);
    }


    /**
     * 获取有返回数据的失败。
     *
     * @param errorCode
     * @param data
     * @return
     */
    public static ResponseMobileDataVo failure(ErrorCode errorCode, Object data, boolean enc) {
        if (enc) {
            data = DESUtil.encrypt(JSON.toJSONString(data));
        }
        return new ResponseMobileDataVo(data, ResponseCode.CODE_FAILED, errorCode);
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
        return new ResponseMobileDataVo(data, ResponseCode.CODE_FAILED.getCode(), code, errMsg);
    }


    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}