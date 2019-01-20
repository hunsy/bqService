package com.bingqiong.bq.controller.api.article;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.controller.admin.BaseController;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.model.Article;
import com.bingqiong.bq.model.ArticleStat;
import com.bingqiong.bq.utils.EncodeUtils;
import com.bingqiong.bq.utils.RequestUtil;
import com.bingqiong.bq.vo.ResponseEmptyVo;
import com.bingqiong.bq.vo.ResponseMobileDataVo;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * Created by hunsy on 2017/4/27.
 */
public class BaseArticleApi extends BaseController {

    protected final String TYPE_ARTICLE = "article";
    protected final String TYPE_POST = "post";

    protected void get(String type) {

        String errMsg = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            Long id = obj.getLong("id");
            String user_id = obj.getString("user_id");
            if (id == null) {
                errMsg = "缺少参数->id";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Article record = Article.dao.getById(id);
            if (record == null){
                errMsg = type + "不存在!id:" + id;
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            //不是文章类型 不返回
            if (record != null
                    && (!record.getStr("type").equals(type)
                    || Integer.parseInt(record.get("status").toString()) == 0)) {
                errMsg = type + "不存在!id:" + id;
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            if (record != null)
                record.remove(new String[]{"is_top", "type", "is_hot", "idx", "group_id", "status", "content"});
            Record ret = new Record();
            //查询用户是否点赞文章
            if (StringUtils.isNotEmpty(user_id)) {
                Record r = Db.findFirst("select * from t_article_praise_rec where article_id = ? and user_id = ?", id, user_id);
                if (r != null) {
                    ret.set("praised", true);
                }
            }
            Record stat = ArticleStat.dao.getByArticle(id);
            ret.set("comment_num", stat.get("comment_num"));
            ret.set("praise_num", stat.get("praise_num"));
            ret.set("id", record.get("id"));
            ret.set("article_type", record.get("article_type"));
            ret.set("thumb_url", Article.dao.parseThumbUrl(record.getStr("thumb_url")));
            ret.set("author", record.get("author"));
            ret.set("title", record.get("title"));
            ret.set("updated_at", record.get("updated_at"));
            ret.set("created_at", record.get("created_at"));
            ret.set("intro", record.get("intro"));

            renderJson(ResponseMobileDataVo.success(ret, EncodeUtils.isEncode()));
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 点赞
     */
    public void praise() {
        String errMsg = "";
        try {
            JSONObject object = RequestUtil.getDecodeParams(getRequest());
            Long article_id = object.getLong("article_id");
            String user_id = object.getString("user_id");
            if (StringUtils.isEmpty(user_id) || article_id == null) {
                errMsg = "缺少参数user_id|article_id";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            if (Article.dao.getById(article_id) == null) {
                errMsg = "文章|帖子不在了";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Record r = Db.findFirst("select * from t_article_praise_rec where article_id = ? and user_id = ? ", article_id, user_id);
            //已点赞，取消点赞
            if (r != null) {
//                errMsg = "该用户已点过赞";
//                logger.error("该用户没有点过赞--->user_id:{},article_id:{}", user_id, article_id);
//                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
                ArticleStat.dao.praiseIncr(article_id, user_id, -1);
            //未点赞，进行点赞
            }else{
                ArticleStat.dao.praiseIncr(article_id, user_id, 1);
            }
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

//    /**
//     * 取消 点赞
//     */
//    public void cancelpraise() {
//        String errMsg = "";
//        try {
//            JSONObject object = RequestUtil.getDecodeParams(getRequest());
//            Long article_id = object.getLong("article_id");
//            String user_id = object.getString("user_id");
////            Long article_id = getParaToLong("article_id");
//            if (StringUtils.isEmpty(user_id) || article_id == null) {
//                errMsg = "缺少参数user_id|article_id";
//                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
//            }
//
//            Record r = Db.findFirst("select * from t_article_praise_rec where article_id = ? and user_id = ? ", article_id, user_id);
//            if (r == null) {
//                errMsg = "该用户没点过赞";
//                logger.error("该用户没有点过赞--->user_id:{},article_id:{}", user_id, article_id);
//                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
//            }
//            if (Article.dao.getById(article_id) == null) {
//                errMsg = "文章|帖子不在了";
//                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
//            }
//            ArticleStat.dao.praiseIncr(article_id, user_id, -1);
//            renderJson(ResponseEmptyVo.success());
//        } catch (Exception e) {
//            handleException(e, errMsg);
//        }
//    }

    /**
     * 获取内容
     */
    public void content() {
        Long id = getParaToLong(-1);
        try {
            logger.info("id:{}", id);
            Article article = Article.dao.getById(id);
            setAttr("content", article.getStr("content"));
//            String html = "<!doctype html>" +
//                    "<html>" +
//                    "<head>" +
//                    "<meta charset='utf-8'>" +
//                    "</head>" +
//                    "<body>"
//                    +
//                    article.getStr("content")
//                    +
//                    "</body>" +
//                    "</html>";
            render("article_content.html");
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


}
