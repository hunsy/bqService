package com.bingqiong.bq.api.controller;

import com.bingqiong.bq.api.controller.category.GroupApi;
import com.bingqiong.bq.api.controller.comm.*;
import com.bingqiong.bq.api.controller.post.CommentApi;
import com.bingqiong.bq.api.controller.post.PostApi;
import com.bingqiong.bq.api.controller.post.PostRecommendApi;
import com.bingqiong.bq.api.controller.post.PostTypeApi;
import com.bingqiong.bq.api.controller.user.PrivateMsgApi;
import com.bingqiong.bq.api.controller.user.UserApi;
import com.jfinal.config.Routes;

/**
 * Created by hunsy on 2017/6/27.
 */
public class ApiRoutes {


    public static void _route(Routes routes) {

        //common
        routes.add("/api", IndexApi.class);
        routes.add("/api/banner", BannerApi.class);
        routes.add("/api/banner/bg", BannerBgApi.class);
        routes.add("/api/domeStore", DomestoreApi.class);
        routes.add("/api/version", AppVersionApi.class);
        routes.add("/api/search", SearchApi.class);
        routes.add("/api/feedback", FeedbackApi.class);

        //category
        routes.add("/api/group", GroupApi.class);

        //post
        routes.add("/api/post/type", PostTypeApi.class);
        routes.add("/api/recommend", PostRecommendApi.class);
        routes.add("/api/post", PostApi.class, "/article");

        routes.add("/api/comment", CommentApi.class);

        routes.add("/api/user", UserApi.class);
        //私信
        routes.add("/api/pm", PrivateMsgApi.class);
        routes.add("/api/sysmsg", SysMsgApi.class);
    }
}
