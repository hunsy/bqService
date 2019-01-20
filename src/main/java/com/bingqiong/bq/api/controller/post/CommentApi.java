package com.bingqiong.bq.api.controller.post;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.comm.constants.Constants;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.conf.BqCmsConf;
import com.bingqiong.bq.model.comm.Sensitive;
import com.bingqiong.bq.model.comment.Comment;
import com.bingqiong.bq.model.comment.CommentLike;
import com.bingqiong.bq.model.user.User;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Redis;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 评论相关请求
 * Created by hunsy on 2017/6/25.
 */
public class CommentApi extends IBaseController {


    /**
     * 新增评论
     */
    public void save() {

        try {

            JSONObject object = getAttr("params");
            User user = getAttr("bq_user");
            String user_id = Redis.use().get(Constants.COMMENT_LIMIT_PREFIX + user.getStr("user_id"));
            //获取发言限制
            if (StringUtils.isNotEmpty(user_id)) {
                throw new BizException(ErrorCode.COMMENT_LIMIT);
            }
            Comment comment = new Comment();
            //上级消息
            Comment parent = null;
            Long parent_id = object.getLong("parent_id");
            if (parent_id != null) {
                parent = Comment.dao.findById(parent_id);
                //上一级评论
                comment.set("parent_id", parent_id);
                if (object.getLong("parent_id") != null) {
                    //第一级评论
                    comment.set("fparent_id", object.getLong("parent_id"));
                } else {
                    if (parent.getLong("fparent_id") != null) {
                        comment.set("fparent_id", parent.getLong("fparent_id"));
                    }
                }
            }
            // 评论人
            comment.set("user_id", user.getStr("user_id"));
            //帖子id
            comment.set("post_id", object.getLong("post_id"));
            comment.set("content", object.getString("content"));

            String device_model = getRequest().getHeader("device_model");
            String factory_name = getRequest().getHeader("factory_name");

            if (object.getString("device_model") != null) {
                device_model = object.getString("device_model");
            }
            if (object.getString("factory_name") != null) {
                factory_name = object.getString("factory_name");
            }

            comment.set("device_model", device_model);
            comment.set("factory_name", factory_name);

            Comment.dao.saveComment(comment);

            comment.put("user_name", user.getStr("user_name"));
            comment.put("avatar_url", user.getStr("avatar_url"));
            comment.put("content", Sensitive.dao.filterSensitive(object.getString("content")));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            comment.put("created_at", sdf.format(comment.getDate("created_at")));
            comment.put("updated_at", sdf.format(comment.getDate("updated_at")));

            if (parent != null) {
                User replyer = User.dao.findByUserId(parent.getStr("user_id"));
                comment.put("reply_user_id", replyer.getStr("user_id"));
                comment.put("reply_user_name", replyer.getStr("user_name"));
                comment.put("reply_user_avatar", replyer.getStr("avatar_url"));
            }

            //评论频率限制 15s
            Redis.use().setex(Constants.COMMENT_LIMIT_PREFIX + user.getStr("user_id"), 15, user.getStr("user_id"));
            renderSuccess(comment, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 获取分页
     */
    @Before(PageInterceptor.class)
    public void page() {

        try {

            PageRequest pageRequest = getAttr("pageRequest");
            String user_id = null;
            User user = getAttr("bq_user");
            if (user != null) {
                user_id = user.getStr("user_id");
            }

            Page<Record> page = Comment.dao.findPage(pageRequest, user_id);
//            List<Record> records = page.getList();
//            if (CollectionUtils.isNotEmpty(records)) {
//                for (Record record : records) {
//                    record.set("content", Sensitive.dao.filterSensitive(record.getStr("content")));
//                }
//            }
            renderSuccess(page, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 获取回复列表
     */
    public void replies() {

        try {

            JSONObject object = getAttr("params");
            if (object == null || object.getLong("parent_id") == null) {

                throw new BizException(ErrorCode.MISSING_PARM);
            }
            List<Record> records = Comment.dao.findList(object.getLong("parent_id"), false);
            renderSuccess(records, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 删除评论
     */
    public void delete() {

        try {
            JSONObject object = getAttr("params");
            Long id = object.getLong("id");

            Comment comment = Comment.dao.findById(id);
            User user = getAttr("bq_user");

            if (!comment.getStr("user_id").equals(user.getStr("user_id"))) {
                throw new BizException(ErrorCode.COMMENT_NO_AUTH);
            }
            Comment.dao.deleteComment(comment);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    /**
     * 点赞
     */
    public void praise() {

        try {

            User user = getAttr("bq_user");
            JSONObject object = getAttr("params");
            if (object == null || object.get("comment_id") == null) {
                throw new BizException(ErrorCode.MISSING_PARM);
            }
            Long comment_id = object.getLong("comment_id");
            CommentLike.dao.saveLike(comment_id, user.getStr("user_id"));
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }


}
