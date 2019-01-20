package com.bingqiong.bq.controller.admin.article;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.interceptor.PageInterceptor;
import com.bingqiong.bq.model.Article;
import com.bingqiong.bq.model.Category;
import com.bingqiong.bq.utils.PostContentUtil;
import com.bingqiong.bq.utils.RequestUtil;
import com.bingqiong.bq.vo.PageRequest;
import com.bingqiong.bq.vo.ResponseDataVo;
import com.bingqiong.bq.vo.ResponseEmptyVo;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;

/**
 * 提供后台管理的post相关服务。
 * <p>
 * Created by hunsy on 2017/4/7.
 */
public class PostController extends BaseArticleController {

    private static final String TYPE_POST = "post";

    /**
     * 按主键获取详情
     * -> param [id]
     */
    public void get() {
        get(TYPE_POST);
    }

    /**
     * 获取分页
     * ->param [pageNo,pageSize,param_id,param_title,param_status,param_is_top]
     */
    @Before(PageInterceptor.class)
    public void page() {
        String errMsg = "";
        try {
            Long group_id = getParaToLong(-1);
            if (group_id == null) {
                errMsg = "缺少参数,/{group_id}";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            Category group = Category.dao.getById(group_id);
            if (group == null || group.get("parent_id").equals("0")) {
                errMsg = "圈子不存在->group_id:" + group_id;
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

//            int page = getParaToInt("pageNo", 1);
//            int size = getParaToInt("pageSize", 10);
            PageRequest pageRequest = getAttr("pageRequest");
            logger.info("pageRequest:{}", JsonKit.toJson(pageRequest));
            Map<String, String> params = RequestUtil.getParams(getRequest());
            String ps = Article.dao.postPage(true, group_id, pageRequest.getPageNo(), pageRequest.getPageSize(), params, null);
            renderJson(ResponseDataVo.success(JSONObject.parseObject(ps)));
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 新增帖子
     * ->param [group_id,title,content,author,status]
     */
    public void save() {
        String errMsg = "";
        try {
            Article post = getModel(Article.class);

            if (post.get("group_id") == null ||
                    post.get("title") == null ||
                    post.get("content") == null) {
                errMsg = "缺少参数->group_id|title|content";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            Long group_id = post.getLong("group_id");
            Category group = Category.dao.getById(group_id);
            Category.dao.checkCategory(group, "group");
            post.set("type", "post");
            post.set("thumb_url", PostContentUtil.parseImgs(post.getStr("content")));
            Article.dao.saveArticle(post);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 更新帖子
     * ->param [id,group_id,title,content,author,status]
     */
    public void update() {
        String errMsg = "";
        try {
            Article record = getModel(Article.class);
            if (record.getLong("id") == null
                    || record.get("title") == null
                    || record.get("content") == null) {
                errMsg = "缺少参数->id,title|content";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }


            if (record.get("group_id") != null) {
                Long group_id = Long.parseLong(record.get("group_id").toString());
                Category group = Category.dao.getById(group_id);
                Category.dao.checkCategory(group, "group");
            }
            if (record.getStr("content") != null) {
                record.set("thumb_url", PostContentUtil.parseImgs(record.getStr("content")));
            }

            Article.dao.updateArticle(record);
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
        delete(TYPE_POST);
    }

    /**
     * 批量删除
     * ->param [ids]
     */
    public void batchdelete() {
        batchdelete(TYPE_POST);
    }


    /**
     * 帖子排序
     * 排序
     * ->param [ids]
     */
    public void sort() {
        sort(TYPE_POST);
    }

    /**
     * 新增置顶
     * ->param [id]
     */
    public void addtop() {
        String errMsg = "";
        try {
            Long id = getParaToLong("id");
            Long group_id = getParaToLong("group_id");
            if (id == null || group_id == null) {
                errMsg = "缺少参数";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Article article = Article.dao.getById(id);
            Article.dao.checkArticle(article, TYPE_POST);
            long count = Db.queryLong("select count(id) from t_article" +
                    " where valid = 1 and type = 'post' and is_top = 1 " +
                    " and group_id = ? ", group_id);
            if (count >= 3) {
                errMsg = "置顶的帖子数已满3";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            article.set("is_top", 1);
            Article.dao.updateArticle(article);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 删除置顶
     * ->param [id]
     */
    public void removetop() {
        String errMsg = "";
        try {
            Long id = getParaToLong("id");
            if (id == null) {
                errMsg = "缺少参数->id";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Article article = Article.dao.getById(id);
            Article.dao.checkArticle(article, TYPE_POST);
            article.set("is_top", 0);
            Article.dao.updateArticle(article);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 帖子下架/上架
     * <p>
     * ->param [id,status]
     */
    public void sold() {
        sold(TYPE_POST);
    }
}
