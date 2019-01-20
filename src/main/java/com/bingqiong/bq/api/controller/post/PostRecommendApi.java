package com.bingqiong.bq.api.controller.post;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.conf.BqCmsConf;
import com.bingqiong.bq.model.post.PostRecommend;
import com.bingqiong.bq.model.user.User;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * 推荐贴
 * Created by hunsy on 2017/6/27.
 */
public class PostRecommendApi extends IBaseController {


    /**
     *
     */
    @Before(PageInterceptor.class)
    public void page() {

        try {
            User user = getAttr("bq_user");
            String user_id = null;
            if (user != null) {
                user_id = user.getStr("user_id");
            }

            PageRequest pageRequest = getAttr("pageRequest");
            JSONObject object = getAttr("mobileInfo");
            //请求来自哪个平台
            String platform;
            if (object.containsKey("platform")) {
                platform = object.getString("platform");
                pageRequest.getParams().put(platform + "_show", "1");
            }
            //上架的帖子
            pageRequest.getParams().put("status", "1");
            Page<Record> ps = PostRecommend.dao.findPage(pageRequest, user_id);
            renderSuccess(ps, BqCmsConf.enc);
        } catch (Exception e) {
            handleException(e);
        }
    }


}
