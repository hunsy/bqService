package com.bingqiong.bq.api.controller.comm;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.api.interceptor.ApiAuthInterceptor;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.conf.BqCmsConf;
import com.bingqiong.bq.model.comm.BannerBg;
import com.jfinal.aop.Clear;

/**
 * 轮播背景
 * Created by hunsy on 2017/8/1.
 */
public class BannerBgApi extends IBaseController {


    @Clear(ApiAuthInterceptor.class)
    public void get() {

        try {
            JSONObject object = getAttr("mobileInfo");
            //请求来自哪个平台
            String platform = "";
            if (object.containsKey("platform")) {
                platform = object.getString("platform");
            }

            BannerBg bannerBg = BannerBg.dao.getByShowType(platform);
            renderSuccess(bannerBg, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

}
