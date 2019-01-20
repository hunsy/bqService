package com.bingqiong.bq.conf;

import com.bingqiong.bq.controller.admin.category.PlateController;
import com.bingqiong.bq.controller.api.*;
import com.bingqiong.bq.controller.api.article.ArticleApi;
import com.bingqiong.bq.controller.api.article.ArticleTypeApi;
import com.bingqiong.bq.controller.api.article.PostApi;
import com.bingqiong.bq.controller.api.user.UserApi;
import com.jfinal.config.Routes;

/**
 * 前端接口APi
 * Created by hunsy on 2017/4/24.
 */
public class ApiRoutes extends Routes {
    @Override
    public void config() {
        add("/api/articleType", ArticleTypeApi.class);
        add("/api/article", ArticleApi.class, "/article");
        add("/api/plate", PlateController.class);
        add("/api/group", GroupApi.class);
        add("/api/post", PostApi.class);
        add("/api/comment", CommentApi.class);
        add("/api/collection", CollectionApi.class);
        add("/api/banner", BannerApi.class);
        add("/api/version", VersionApi.class);

        add("/api/domeStore", DomestoreApi.class);

        add("/api/user", UserApi.class);
        add("/api/feedback", FeedbackApi.class);
        add("/api", IndexController.class);
    }
}
