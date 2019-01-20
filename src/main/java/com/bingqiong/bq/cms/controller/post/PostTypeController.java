package com.bingqiong.bq.cms.controller.post;

import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.post.Post;
import com.bingqiong.bq.model.post.PostTag;
import com.bingqiong.bq.model.post.PostType;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 帖子分类
 * Created by hunsy on 2017/6/30.
 */
public class PostTypeController extends IBaseController {


    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 获取类型列表
     */
    @Before(PageInterceptor.class)
    public void page() {

        try {
            PageRequest request = getAttr("pageRequest");
            Page<Record> page = PostType.dao.findPage(request);
            renderSuccess(page);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    public void list() {

        try {
            List<Record> ls = PostType.dao.findList(1);
            renderSuccess(ls);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 保存
     */
    public void save() {

        try {
            PostType type = getModel(PostType.class);
            PostType.dao.saveType(type);
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
            PostType type = getModel(PostType.class);
            PostType.dao.updateType(type);
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

            PostType.dao.deleteType(id);
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
            List<PostType> posts = findListByIds(ids);
            for (int i = 0; i < posts.size(); i++) {
                PostType post = posts.get(i);
                PostType.dao.updateType(post.set("idx", posts.size() - i));
            }
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 系统标签排序
     */
    public void sold() {

        try {
            Long id = getParaToLong("id");
            Integer status = getParaToInt("status");
            if (id == null || status == null) {
                logger.error("缺少参数");
                throw new BizException(ErrorCode.MISSING_PARM);
            }
            PostType post = PostType.dao.findById(id);
            if (post == null) {
                logger.error("帖子类型不存在");
                throw new BizException(ErrorCode.POST_TYPE_NAME_NOT_EXIST);
            }

            post.set("status", status);
            PostType.dao.updateType(post);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * @param ids
     * @return
     */
    private List<PostType> findListByIds(String[] ids) throws BizException {

        if (ids[0].indexOf(",") > 0) {
            ids = ids[0].split(",");
        }
        if (ids == null || ids.length == 0) {
            logger.error("缺少参数ids");
            throw new BizException(ErrorCode.MISSING_PARM);
        }
        //遍历查询
        List<PostType> tagses = new ArrayList<PostType>();
        for (String id : ids) {
            PostType tags = PostType.dao.findById(Long.parseLong(id));
            if (tags == null) {
                logger.error("类型不存在->id:{}", id);
                throw new BizException(ErrorCode.POST_TYPE_NAME_NOT_EXIST);
            }
            tagses.add(tags);
        }
        return tagses;
    }
}
