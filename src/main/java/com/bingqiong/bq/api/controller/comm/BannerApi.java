package com.bingqiong.bq.api.controller.comm;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.api.interceptor.ApiAuthInterceptor;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.conf.BqCmsConf;
import com.bingqiong.bq.model.comm.Banner;
import com.jfinal.aop.Clear;
import com.jfinal.plugin.activerecord.Record;

import java.util.List;

/**
 * 轮播
 * Created by hunsy on 2017/6/27.
 */
public class BannerApi extends IBaseController {


    @Clear(ApiAuthInterceptor.class)
    public void list() {
        try {

            JSONObject object = getAttr("mobileInfo");
            //请求来自哪个平台
            String platform = "";
            if (object.containsKey("platform")) {
                platform = object.getString("platform");
            }
            List<Record> records = Banner.dao.findList(platform);
            renderSuccess(records, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

}
