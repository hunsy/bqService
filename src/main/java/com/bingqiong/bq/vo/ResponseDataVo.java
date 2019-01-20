package com.bingqiong.bq.vo;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.utils.MyAesUtil;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Page;

/**
 * Created by hunsy on 2017/3/21.
 */
public class ResponseDataVo extends ResponseEmptyVo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7719887125392089065L;
    private Object data;

    public ResponseDataVo(int code, Object data, String msg) {
        super.setResponseCode(code);
        super.setErrorMsg(msg);
        if (data instanceof JSONObject) {
            JSONObject p = (JSONObject) data;
            data = new RespData(p.getInteger("totalRow"), p.getJSONArray("list"));
        } else if (data instanceof Page) {
            Page<?> page = (Page<?>) data;
            data = new RespData(page.getTotalRow(), page.getList() != null ? JSON.parseArray(JsonKit.toJson(page.getList())) : null);
        }
        this.data = data;
//        logger.info("response data:{}", JsonKit.toJson(this));
    }

    public ResponseDataVo(BqErrorCode bqErrorCode, Object data) {
        this(bqErrorCode.getCode(), data, bqErrorCode.getMessage());
    }

    /**
     * 获取有返回数据的成功。
     *
     * @param data
     * @return
     */
    public static ResponseDataVo success(Object data) {
        return new ResponseDataVo(BqErrorCode.SUCCESS, data);
    }

    public static ResponseDataVo success(Object data, boolean encode) throws Exception {
        if (encode) {
            data = MyAesUtil.getInstance().encode(JsonKit.toJson(data));
        }
        return new ResponseDataVo(BqErrorCode.SUCCESS, data);
    }

    /**
     * 获取有返回数据的成功。
     *
     * @param msg
     * @param data
     * @return
     */
    public static ResponseDataVo success(String msg, Object data) {
        return new ResponseDataVo(BqErrorCode.SUCCESS.getCode(), data, msg);
    }


    /**
     * 获取有返回数据的失败。
     *
     * @param bqErrorCode
     * @param data
     * @return
     */
    public static ResponseDataVo failure(BqErrorCode bqErrorCode, Object data) {
        return new ResponseDataVo(bqErrorCode, data);
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
        return new ResponseDataVo(code, data, errMsg);
    }


    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}