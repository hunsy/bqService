package com.bingqiong.bq.api.controller.comm;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.api.interceptor.ApiAuthInterceptor;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.model.comm.FeedBack;
import com.jfinal.aop.Clear;

/**
 * Created by hunsy on 2017/7/12.
 */
public class FeedbackApi extends IBaseController {

    @Clear(ApiAuthInterceptor.class)
    public void save() {

        try {

            JSONObject object = getAttr("params");
            FeedBack fb = new FeedBack();
            fb.set("mobile", object.getString("mobile"));
            fb.set("content", object.getString("content"));
            FeedBack.dao.saveFb(fb);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }
}
