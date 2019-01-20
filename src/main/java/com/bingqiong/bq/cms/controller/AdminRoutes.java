package com.bingqiong.bq.cms.controller;

import com.bingqiong.bq.cms.controller.category.GroupController;
import com.bingqiong.bq.cms.controller.category.PlateController;
import com.bingqiong.bq.cms.controller.comm.*;
import com.bingqiong.bq.cms.controller.comment.CommentController;
import com.bingqiong.bq.cms.controller.post.*;
import com.bingqiong.bq.cms.controller.user.CardController;
import com.jfinal.config.Routes;

/**
 * Created by hunsy on 2017/6/28.
 */
public class AdminRoutes {


    public static void _routes(Routes me) {

        me.add("/admin", IndexController.class, "/");
        me.add("/admin/plate", PlateController.class);
        me.add("/admin/group", GroupController.class);

        me.add("/admin/post/tag", PostTagController.class);
        me.add("/admin/post/type", PostTypeController.class);
        me.add("/admin/post", PostController.class);
        me.add("/admin/post/tags", PostTagsController.class);
        me.add("/admin/post/recommend", PostRecommendController.class);
        me.add("/admin/comment", CommentController.class);

        //搜索热词
        me.add("/admin/hotword", SearchHotWordController.class);
        me.add("/admin/banner", BannerController.class, "/banner");
        me.add("/admin/banner/bg", BannerBgController.class);
        me.add("/admin/sensitive", SensitiveController.class);
        me.add("/admin/sysmsg", SysmsgController.class);
        me.add("/admin/version", AppVersionController.class);
        me.add("/admin/card", CardController.class);
        me.add("/admin/feedback", FeedbackController.class);
    }
}
