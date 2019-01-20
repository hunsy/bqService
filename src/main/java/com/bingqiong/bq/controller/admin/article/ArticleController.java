package com.bingqiong.bq.controller.admin.article;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.interceptor.PageInterceptor;
import com.bingqiong.bq.model.Article;
import com.bingqiong.bq.utils.EncodeUtils;
import com.bingqiong.bq.utils.RequestUtil;
import com.bingqiong.bq.vo.PageRequest;
import com.bingqiong.bq.vo.ResponseDataVo;
import com.bingqiong.bq.vo.ResponseEmptyVo;
import com.bingqiong.bq.vo.ResponseMobileDataVo;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

import java.util.*;

/**
 * 提供后台管理的article相关服务。
 * <p>
 * Created by hunsy on 2017/4/7.
 */
public class ArticleController extends BaseArticleController {

    private Logger logger = LoggerFactory.getLogger(ArticleController.class);
    private static final String TYPE_ARTICLE = "article";

    /**
     * 获取文章类型
     */
    public void types() {

        List<String> set = new ArrayList<>();
        Prop prop = PropKit.use("article_types.txt");
        Enumeration<Object> enu = prop.getProperties().keys();
        while (enu.hasMoreElements()) {
            Object key = enu.nextElement();
            String val = prop.getProperties().getProperty(key.toString());
            set.add(val);
        }
        logger.info(JsonKit.toJson(set));
        renderJson(ResponseDataVo.success(set));
    }

    /**
     * 按主键获取详情
     * ->param [id]
     */
    public void get() {
        get(TYPE_ARTICLE);
    }

    /**
     * 获取分页
     * ->param [pageNo,pageSize,param_id,param_title,param_article_type,param_author,param_status]
     */
    @Before(PageInterceptor.class)
    public void page() {
        try {
//            int page = getParaToInt("pageNo", 1);
//            int size = getParaToInt("pageSize", 10);
            PageRequest pageRequest = getAttr("pageRequest");
            logger.info("pageRequest:{}", JsonKit.toJson(pageRequest));
            Map<String, String> params = RequestUtil.getParams(getRequest());
            String ps = Article.dao.articlePage(true, pageRequest.getPageNo(), pageRequest.getPageSize(), params, null);
            renderJson(ResponseDataVo.success(JSONObject.parseObject(ps)));
        } catch (Exception e) {
            handleException(e, "");
        }
    }

    /**
     * 新增文章
     * ->param [title,article_type,author,content,thumb_url,status]
     */
    public void save() {
        String errMsg = "";
        try {
            Article article = getModel(Article.class);
            logger.info("article:{}", JsonKit.toJson(article));
            if (article.getStr("title") == null
                    || article.getStr("thumb_url") == null
                    || article.getStr("article_type") == null) {
                errMsg = "缺少参数->title|thumb_url|article_type";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            article.set("type", "article");
            Article.dao.saveArticle(article);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }


    /**
     * 更新文章
     * ->param [id,title,article_type,author,content,thumb_url,status]
     */
    public void update() {
        String errMsg = "";
        try {
            Article article = getModel(Article.class);
            if (article.getLong("id") == null) {
                errMsg = "缺少参数->id";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Article.dao.updateArticle(article);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 删除
     * ->param [id]
     */
    public void delete() {
        delete(TYPE_ARTICLE);
    }

    /**
     * 批量删除
     * ->param [ids]
     */
    public void batchdelete() {
        batchdelete(TYPE_ARTICLE);
    }


    /**
     * 热点排序，资讯排序
     * 排序
     * ->param [ids]
     */
    public void sort() {
        sort(TYPE_ARTICLE);
    }

    /**
     * 热点排序
     */
    public void hotsort() {
        String errMsg = "";
        try {
            String[] ids = getParaValues("ids");
            if (ids[0].indexOf(",") > 0) {
                ids = ids[0].split(",");
            }
            if (ids == null || ids.length == 0) {
                errMsg = "没有排序的文章";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            //更新idx字段
            for (int i = 0; i < ids.length; i++) {
                Article r = Article.dao.getById(Long.parseLong(ids[i]));
                Article.dao.checkArticle(r, TYPE_ARTICLE);
                r.set("hot_idx", ids.length - i);
                Article.dao.updateArticle(r);
            }
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }


    /**
     * 新增热点
     * ->param [ids]
     */
    public void addhot() {
        String errMsg = "";
        try {
            String[] ids = getParaValues("ids");

            if (ids == null || ids.length == 0) {
                errMsg = "缺少参数";
                logger.error("缺少新增热点参数->ids");
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            for (String id : ids) {
                Article article = Article.dao.getById(Long.parseLong(id));
                Article.dao.checkArticle(article, TYPE_ARTICLE);
                article.set("is_hot", 1);
                Article.dao.updateArticle(article);
            }
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 删除热点
     * ->param [id]
     */
    public void removehot() {
        String errMsg = "";
        try {
            Long id = getParaToLong("id");
            if (id == null) {
                errMsg = "缺少参数->id";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            Article article = Article.dao.getById(id);
            Article.dao.checkArticle(article, TYPE_ARTICLE);
            article.set("is_hot", 0);
            Article.dao.updateArticle(article);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }


    /**
     * 文章下架/上架
     * <p>
     * ->param [id,status]
     */
    public void sold() {
        sold(TYPE_ARTICLE);
    }

    public void init() {
        Article.dao.init();
    }

}
