package com.bingqiong.bq.conf;

import com.bingqiong.bq.controller.admin.*;
import com.bingqiong.bq.controller.admin.article.ArticleController;
import com.bingqiong.bq.controller.admin.article.ArticleTypeController;
import com.bingqiong.bq.controller.admin.article.PostController;
import com.bingqiong.bq.controller.admin.category.GroupController;
import com.bingqiong.bq.controller.admin.category.PlateController;
import com.jfinal.config.Routes;

/**
 * Created by hunsy on 2017/4/24.
 */
public class AdminRoutes extends Routes {
    @Override
    public void config() {
        add("/admin", IndexController.class);
        add("/admin/articleType", ArticleTypeController.class);
        add("/admin/article", ArticleController.class);
        add("/admin/plate", PlateController.class);
        add("/admin/group", GroupController.class);
        add("/admin/post", PostController.class);
        add("/admin/comment", CommentController.class);
        add("/admin/banner", BannerController.class);

        add("/admin/sensitive", SensitiveController.class);
        add("/admin/card", CardController.class);
        add("/admin/version", VersionController.class);
    }
}
