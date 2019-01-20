package com.bingqiong.bq.cms.controller.post;

import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.post.Post;
import com.bingqiong.bq.model.post.PostTag;
import com.bingqiong.bq.model.post.PostTags;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 帖子标签管理
 * <p>
 * Created by hunsy on 2017/6/23.
 */
public class PostTagController extends IBaseController {


    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 获取标签分页
     */
    @Before(PageInterceptor.class)
    public void page() {

        try {

            PageRequest request = getAttr("pageRequest");
            Page<PostTag> pts = PostTag.dao.findPage(request);
            renderSuccess(pts);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 获取帖子标签列表
     */
    public void list() {

        try {

            List<Record> ps = PostTag.dao.findList();
            renderSuccess(ps);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 保存
     */
    public void save() {

        try {
            PostTag postTag = getModel(PostTag.class);
            PostTag.dao.saveTag(postTag);
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
            PostTag postTag = getModel(PostTag.class);
            PostTag.dao.updateTag(postTag);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 删除
     */
    public void delete() {

        try {
            Long id = getParaToLong(-1);
            if (id == null) {
                logger.error("缺少参数");
                throw new BizException(ErrorCode.MISSING_PARM);
            }

            PostTag.dao.deleteTag(id);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 系统标签排序
     */
    public void sort() {

        try {
            String[] ids = getParaValues("ids");
            List<PostTag> posts = findListByIds(ids);
            for (int i = 0; i < posts.size(); i++) {
                PostTag post = posts.get(i);
                PostTag.dao.updateTag(post.set("idx", posts.size() - i));
            }
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * @param ids
     * @return
     */
    private List<PostTag> findListByIds(String[] ids) throws BizException {

        if (ids[0].indexOf(",") > 0) {
            ids = ids[0].split(",");
        }
        if (ids == null || ids.length == 0) {
            logger.error("缺少参数ids");
            throw new BizException(ErrorCode.MISSING_PARM);
        }
        //遍历查询
        List<PostTag> tagses = new ArrayList<PostTag>();
        for (String id : ids) {
            PostTag tags = PostTag.dao.findById(Long.parseLong(id));
            if (tags == null) {
                logger.error("帖子标签不存在->id:{}", id);
                throw new BizException(ErrorCode.POST_TAG_NOT_EXIST);
            }
            if (tags.getInt("sys") == 0) {
                logger.error("非系统标签不排序->id:{}", id);
                throw new BizException(ErrorCode.POST_TAG_NOT_SYS);
            }
            tagses.add(tags);
        }
        return tagses;
    }


}
