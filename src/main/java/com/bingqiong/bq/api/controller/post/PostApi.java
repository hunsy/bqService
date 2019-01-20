package com.bingqiong.bq.api.controller.post;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.api.interceptor.ApiAuthInterceptor;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.conf.BqCmsConf;
import com.bingqiong.bq.model.category.Group;
import com.bingqiong.bq.model.comm.Sensitive;
import com.bingqiong.bq.model.post.Post;
import com.bingqiong.bq.model.post.PostLike;
import com.bingqiong.bq.model.user.User;
import com.bingqiong.bq.model.user.UserTags;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 帖子相关请求
 * Created by hunsy on 2017/6/25.
 */
public class PostApi extends IBaseController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 推荐帖子
     */
    @Before(PageInterceptor.class)
    public void page() {
        try {

            PageRequest pageRequest = getAttr("pageRequest");
            User user = getAttr("bq_user");
            String user_id = null;
            if (user != null) {
                user_id = user.getStr("user_id");
            }
            pageRequest.getParams().put("status", "1");

            JSONObject object = getAttr("mobileInfo");
            //请求来自哪个平台
            String platform;
            if (object.containsKey("platform")) {
                platform = object.getString("platform");
                pageRequest.getParams().put(platform + "_show", "1");
            }
            Page<Record> page = Post.dao.findPage(pageRequest, user_id);
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
     * 获取帖子详情
     */
    public void get() {

        try {

            JSONObject object = getAttr("params");
            Long id = object.getLong("id");
            User user = getAttr("bq_user");
            //获取详情
            Record post = Post.dao.findDetail(id);
            if (post == null || post.getInt("status") == 0) {
                throw new BizException(ErrorCode.POST_NOT_EXIST);
            }
            //已登录
            //查询是否点赞
            //新增用户标签
            if (user != null) {
                post.set("praised", PostLike.dao.liked(id, user.getStr("user_id")));
                //增加用户标签
                Group group = Group.dao.findById(post.getLong("group_id"));
                //新增用户标签，有则增加点击数量
                UserTags.dao.saveTags(user.getStr("user_id"), group.getLong("tag_id"));
                //// TODO: 2017/8/1 增加更多标签  查询PostTags
            } else {
                post.set("praised", false);
            }
            renderSuccess(post, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 新增帖子
     */
    public void save() {

        try {

            User user = getAttr("bq_user");
            JSONObject object = getAttr("params");

            logger.info("params:{}", object.toJSONString());

            Post post = new Post();
            post.set("content", object.getString("content"));
            post.set("thumb_url", object.getString("thumb_url"));
            post.set("group_id", object.getLongValue("group_id"));
            post.set("status", 1);
            post.set("is_sys", 0);
            post.set("user_id", user.getStr("user_id"));
            Post.dao.savePost(post);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 点赞，取消点赞.
     * 必须登录接口。
     * 未点赞 +点赞记录
     * 已点赞 -点赞记录
     */
    public void praise() {

        try {

            User user = getAttr("bq_user");
            JSONObject object = getAttr("params");
            if (object == null || object.get("id") == null) {
                throw new BizException(ErrorCode.MISSING_PARM);
            }
            Long post_id = object.getLong("id");
            PostLike.dao.saveLike(post_id, user.getStr("user_id"));
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    @Clear(ApiAuthInterceptor.class)
    public void share() {

        Long id = getParaToLong("id");
        Post post = Post.dao.findById(id);
        setAttr("post", post);
        render("post_pg.html");
    }


    /**
     * 跳转帖子内容
     */
    @Clear
    public void content() {

        try {
            Long id = getParaToLong(-1);
            Post post = Post.dao.findById(id);
            setAttr("content", post.getStr("content"));
        } catch (Exception e) {
            if (e instanceof BizException) {
                BizException ez = (BizException) e;
                setAttr("content", ez.getCode().getMsg());
            } else {
                setAttr("content", "系统异常");
            }
        }
        render("article_content.html");
    }

    /**
     * 获取圈子下的置顶帖
     */
    @Clear(ApiAuthInterceptor.class)
    public void tops() {

        try {

            JSONObject obj = getAttr("params");
            logger.info(JsonKit.toJson(obj));
            JSONObject object = getAttr("mobileInfo");
            //请求来自哪个平台
            String platform = "";
            if (object.containsKey("platform")) {
                platform = object.getString("platform");
            }
            List<Record> records = Post.dao.findTops(obj.getLong("group_id"), false, platform);
            renderSuccess(records, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

}
