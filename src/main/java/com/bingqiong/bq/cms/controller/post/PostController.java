package com.bingqiong.bq.cms.controller.post;

import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.utils.RichTextUtil;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.conf.BqCmsConf;
import com.bingqiong.bq.model.category.Group;
import com.bingqiong.bq.model.post.Post;
import com.bingqiong.bq.model.post.PostTags;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 帖子相关
 * Created by hunsy on 2017/6/23.
 */
public class PostController extends IBaseController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 分页
     */
    @Before(PageInterceptor.class)
    public void page() {

        try {
            PageRequest pageRequest = getAttr("pageRequest");
            Page<Record> pp = Post.dao.findPageAdmin(pageRequest);
            renderSuccess(pp);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 新增
     */
    public void save() {

        try {
            Post post = getModel(Post.class);
            post.set("user_id", BqCmsConf.ADMIN_ID);
            post.set("is_sys", 1);

            if (StringUtils.isNotEmpty(post.getStr("title"))) {

                if (post.getStr("title").length() > 30) {
                    throw new BizException(ErrorCode.POST_TITLE_LENGTH);
                }
            }

            //解析文本，获取图片地址
            if (StringUtils.isNotEmpty(post.getStr("content"))) {
//                post.set("thumb_url", RichTextUtil.parseImgs(post.getStr("content")));
                RichTextUtil.parseContent(post);
            }
            Post.dao.savePost(post);
//            if (flag) {
//                //新增帖子标签
//                PostTags.dao.saveTags(post.getLong("id"), PostTag.dao.findByName("官方").getLong("id"), 1);
//            }
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 更新
     */
    public void update() {

        try {
            Post post = getModel(Post.class);
            if (StringUtils.isNotEmpty(post.getStr("title"))) {

                if (post.getStr("title").length() > 30) {
                    throw new BizException(ErrorCode.POST_TITLE_LENGTH);
                }
            }

            Post dbPost = Post.dao.findById(post.getLong("id"));
            if (dbPost == null) {
                throw new BizException(ErrorCode.MISSING_PARM);
            }

            if (dbPost.getInt("is_sys") == 1) {
                //解析文本，获取图片地址
                if (StringUtils.isNotEmpty(post.getStr("content"))) {
//                post.set("thumb_url", RichTextUtil.parseImgs(post.getStr("content")));
                    RichTextUtil.parseContent(post);
                } else {
                    post.set("thumb_url", "");
                }
            }
            Post.dao.updatePost(post);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 删除帖子
     */
    public void delete() {

        try {
            Long id = getParaToLong(-1);
            Post.dao.deletePost(id);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 批量删除
     */
    public void batchdelete() {

        try {
            String[] ids = getParaValues("ids");
            List<Post> posts = findListByIds(ids);
            for (Post post : posts) {
                Post.dao.deletePost(post);
            }
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 按圈子排序
     */
    public void sort() {

        try {
            Long group_id = getParaToLong("group_id");
            String[] ids = getParaValues("ids");
            List<Post> posts = findListByIds(ids);

            List<Post> nps = new ArrayList<Post>();
            for (int i = 0; i < posts.size(); i++) {
                Post post = posts.get(i);
                if (post == null) {
                    logger.error("帖子不存在");
                    throw new BizException(ErrorCode.POST_NOT_EXIST);
                }
                logger.info("{}", post.getLong("group_id"));
                if (post.getLong("group_id") != group_id) {
                    logger.error("不支持不同圈子下的帖子混排");
                    throw new BizException(ErrorCode.POST_GROUP_NOT_SAME);
                }
                post.set("idx", posts.size() - i);
                nps.add(post);
            }

            for (Post post : nps) {
                Post.dao.updatePost(post);
            }
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 上下架
     */
    public void sold() {


        try {
            Long id = getParaToLong("id");
            Integer status = getParaToInt("status");
            if (id == null || status == null) {
                logger.error("缺少参数");
                throw new BizException(ErrorCode.MISSING_PARM);
            }
            Post post = Post.dao.findById(id);
            if (post == null) {
                logger.error("帖子不存在");
                throw new BizException(ErrorCode.POST_NOT_EXIST);
            }

            post.set("status", status);
            Post.dao.updatePost(post);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 置顶
     */
    public void addtop() {

        try {
            Long id = getParaToLong("id");
            Long group_id = getParaToLong("group_id");

            Group group = Group.dao.findById(group_id);
            if (group == null) {
                logger.error("圈子不存在");
                throw new BizException(ErrorCode.GROUP_NOT_EXIST);
            }

            List<Record> tops = Post.dao.findTops(group_id, true, "");
            if (CollectionUtils.isNotEmpty(tops) && tops.size() >= 3) {
                logger.error("置顶贴数量已满");
                throw new BizException(ErrorCode.POST_NOT_MORE_TOP);
            }

            Post post = Post.dao.findById(id);
            if (post == null) {
                logger.error("帖子不存在");
                throw new BizException(ErrorCode.POST_NOT_EXIST);
            }
            post.set("top", 1);
            Post.dao.updatePost(post);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 取消置顶
     */
    public void removetop() {

        try {
            Long id = getParaToLong("id");
            Post post = Post.dao.findById(id);
            if (post == null) {
                logger.error("帖子不存在");
                throw new BizException(ErrorCode.POST_NOT_EXIST);
            }
            post.set("top", 0);
            Post.dao.updatePost(post);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    /**
     * 根据id，获取板块列表
     *
     * @param ids
     * @return
     * @throws BizException
     */
    private List<Post> findListByIds(String[] ids) throws BizException {
        if (ids[0].indexOf(",") > 0) {
            ids = ids[0].split(",");
        }
        if (ids == null || ids.length == 0) {
            logger.error("缺少参数ids");
            throw new BizException(ErrorCode.MISSING_PARM);
        }
        //遍历查询
        //所有的板块都存在时，才进行遍历删除
        List<Post> posts = new ArrayList<Post>();
        for (String id : ids) {
            Post post = Post.dao.findById(Long.parseLong(id));
            if (post == null) {
                logger.error("帖子不存在->id:{}", id);
                throw new BizException(ErrorCode.POST_NOT_EXIST);
            }
            posts.add(post);
        }
        return posts;
    }


//    /**
//     * 审核精华
//     */
//    public void digest() {
//
//        try {
//            Long group_id = getParaToLong("group_id");
//            String[] ids = getParaValues("ids");
//            List<PostTags> ls = findListTagsByIds(ids);
//
//            for (PostTags tags : ls) {
//                tags.set("status", 1);
//                PostTags.dao.updateTags(tags);
//            }
//            renderSuccess();
//        } catch (Exception e) {
//            renderFailure(e);
//        }
//    }

//    /**
//     * 给帖子添加标签
//     */
//    public void addtag() {
//
//        try {
//            Long post_id = getParaToLong("post_id");
//            String names = getPara("names");
//
//        } catch (Exception e) {
//            renderFailure(e);
//        }
//    }


    /**
     * 根据id，获取板块列表
     *
     * @param ids
     * @return
     * @throws BizException
     */
    private List<PostTags> findListTagsByIds(String[] ids) throws BizException {
        if (ids[0].indexOf(",") > 0) {
            ids = ids[0].split(",");
        }
        if (ids == null || ids.length == 0) {
            logger.error("缺少参数ids");
            throw new BizException(ErrorCode.MISSING_PARM);
        }
        //遍历查询
        List<PostTags> tagses = new ArrayList<PostTags>();
        for (String id : ids) {
            PostTags tags = PostTags.dao.findById(Long.parseLong(id));
            if (tags == null) {
                logger.error("精华申请不存在->id:{}", id);
                throw new BizException(ErrorCode.GROUP_NOT_EXIST);
            }
            tagses.add(tags);
        }
        return tagses;
    }
}
