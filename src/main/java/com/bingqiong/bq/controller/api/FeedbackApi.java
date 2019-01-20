package com.bingqiong.bq.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.controller.admin.BaseController;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.utils.RequestUtil;
import com.bingqiong.bq.vo.ResponseEmptyVo;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Created by hunsy on 2017/5/19.
 */
public class FeedbackApi extends BaseController {

    /**
     * 保存反馈
     */
    public void save() {
        String err = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            if (StringUtils.isEmpty(obj.getString("content"))) {
                err = "反馈内容不能为空";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Record record = new Record()
                    .set("content", obj.getString("content"))
                    .set("mobile",obj.getString("mobile"))
                    .set("created_at", new Date());
            Db.save("t_feedback", record);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, err);
        }

    }
}
