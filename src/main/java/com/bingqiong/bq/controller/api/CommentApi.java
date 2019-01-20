package com.bingqiong.bq.controller.api;

import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.controller.admin.BaseController;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.model.Article;
import com.bingqiong.bq.model.Comment;
import com.bingqiong.bq.model.CommentPraiseRec;
import com.bingqiong.bq.model.CommentStat;
import com.bingqiong.bq.utils.EncodeUtils;
import com.bingqiong.bq.utils.RequestUtil;
import com.bingqiong.bq.vo.ResponseEmptyVo;
import com.bingqiong.bq.vo.ResponseMobileDataVo;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * 评论的api。
 * <p>
 * Created by hunsy on 2017/4/10.
 */
public class CommentApi extends BaseController {

    /**
     * 保存评论。
     */
    public void save() {
        String errMsg = "";
        try {
            Comment record = (Comment) RequestUtil.parseRecordDecode(new Comment(), getRequest());
            if (record.get("article_id") == null
                    || record.get("content") == null
                    || record.get("user_id") == null
                    || record.get("user_name") == null) {
                errMsg = "缺少参数article_id|content|user_id|user_name";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Article article = Article.dao.getById(Long.parseLong(record.get("article_id").toString()));
            if (article == null) {
                errMsg = "文章|帖子不在了";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            record.set("content", EmojiParser.parseToAliases(record.getStr("content")));
            record.set("group_id", article.get("group_id"));
            Comment.dao.saveComment(record);
            record.set("content", Comment.dao.hasSensitive(record.getStr("content")));
            renderJson(ResponseMobileDataVo.success(record, EncodeUtils.isEncode()));
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 获取评论分页
     * -> param [article_id]
     */
    public void page() {
        String errMsg = "";
        try {
            Long article_id = getParaToLong(-1);
            if (article_id == null) {
                errMsg = "缺少参数/{article_id}";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Article record = Article.dao.getById(article_id);
            if (record == null) {
                errMsg = "文章/帖子不在了";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            int page = obj.getIntValue("pageNo") == 0 ? 1 : obj.getIntValue("pageNo");
            int size = obj.getIntValue("pageSize") == 0 ? 10 : obj.getIntValue("pageSize");
            String user_id = obj.getString("user_id");
//            if (StringUtils.isEmpty(user_id)){
//                errMsg = "缺少参数->{user_id}";
//                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
//            }
            Page<Record> pr = Comment.dao.findApiPage(page, size, article_id.toString(), user_id);
            renderJson(ResponseMobileDataVo.success(pr, EncodeUtils.isEncode()));
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }


    /**
     * 获取评论及其下级评论列表
     * ->param [comment_id]
     */
    public void list() {

        String errMsg = "";
        try {
            Long comment_id = getParaToLong(-1);
            if (comment_id == null) {
                errMsg = "缺少参数/{comment_id}";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Comment record = Comment.dao.getById(comment_id);
            if (record == null) {
                errMsg = "评论不在了";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            int page = obj.getIntValue("pageNo") == 0 ? 1 : obj.getIntValue("pageNo");
            int size = obj.getIntValue("pageSize") == 0 ? 10 : obj.getIntValue("pageSize");
            Page<Record> ret = Comment.dao.childPage(page, size, comment_id);
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
            Long comment_id = object.getLong("comment_id");
            String user_id = object.getString("user_id");

            if (StringUtils.isEmpty(user_id) || comment_id == null) {
                errMsg = "缺少参数user_id|article_id";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            if (Comment.dao.getById(comment_id) == null) {
                errMsg = "评论不在了";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }


            //点过赞
            if (CommentPraiseRec.dao.praiseed(comment_id, user_id)) {
                CommentStat.dao.praiseIncr(user_id, comment_id, -1);
            //没点赞
            } else {
                CommentStat.dao.praiseIncr(user_id, comment_id, 1);
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
//            Long comment_id = object.getLong("comment_id");
//            String user_id = object.getString("user_id");
//            if (StringUtils.isEmpty(user_id) || comment_id == null) {
//                errMsg = "缺少参数user_id|article_id";
//                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
//            }
//            if (Comment.dao.getById(comment_id) == null) {
//                errMsg = "评论不在了";
//                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
//            }
//
//            if (CommentPraiseRec.dao.praiseed(comment_id, user_id)) {
//                CommentStat.dao.praiseIncr(user_id, comment_id, -1);
//            } else {
//                errMsg = "没有点过赞";
//                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
//            }
//            renderJson(ResponseEmptyVo.success());
//        } catch (Exception e) {
//            handleException(e, errMsg);
//        }
//    }

    /**
     *
     */
    public void mylist() {
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            String user_id = obj.getString("user_id");
            int page = obj.getIntValue("pageNo") == 0 ? 1 : obj.getIntValue("pageNo");
            int size = obj.getIntValue("pageSize") == 0 ? 10 : obj.getIntValue("pageSize");
            Page<Record> ret = Comment.dao.myPage(page, size, user_id);
            renderJson(ResponseMobileDataVo.success(ret, EncodeUtils.isEncode()));
        } catch (Exception e) {
            handleException(e, "");
        }
    }

}
