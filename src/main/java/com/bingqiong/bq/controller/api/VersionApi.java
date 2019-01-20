package com.bingqiong.bq.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.controller.admin.BaseController;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.model.AppVersion;
import com.bingqiong.bq.utils.EncodeUtils;
import com.bingqiong.bq.utils.RequestUtil;
import com.bingqiong.bq.vo.ResponseMobileDataVo;
import com.jfinal.plugin.activerecord.Record;

/**
 * Created by hunsy on 2017/5/15.
 */
public class VersionApi extends BaseController {


    /**
     * 检查版本
     */
    public void check() {

        String errMsg = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());

            if (obj.getString("package") == null
                    || obj.getString("channelCode") == null
                    || obj.getString("versionName") == null
                    || obj.getInteger("versionCode") == null) {
                errMsg = "缺少参数";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            Record cache = AppVersion.dao.getByNameAndChannel(obj.getString("package"), obj.getString("channelCode"));
            if (cache == null) {
                errMsg = "应用不存在";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            boolean hasUpdate = false;
            if (obj.getIntValue("versionCode") < Integer.valueOf(cache.getStr("version_code"))) {
                hasUpdate = true;
            }
            cache.set("hasUpdate", hasUpdate);
            renderJson(ResponseMobileDataVo.success(cache, EncodeUtils.isEncode()));
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

}
