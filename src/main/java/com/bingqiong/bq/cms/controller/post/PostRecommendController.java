package com.bingqiong.bq.cms.controller.post;

import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.post.Post;
import com.bingqiong.bq.model.post.PostRecommend;
import com.bingqiong.bq.model.user.User;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 推荐贴请求
 * <p>
 * Created by hunsy on 2017/6/25.
 */
public class PostRecommendController extends IBaseController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 分页
     */
    @Before(PageInterceptor.class)
    public void page() {

        try {

            PageRequest pageRequest = getAttr("pageRequest");
            Page<Record> prs = PostRecommend.dao.findPageAdmin(pageRequest);
            renderSuccess(prs);
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    /**
     * 新增推荐
     */
    public void save() {

        try {

            Long post_id = getParaToLong("post_id");
            PostRecommend.dao.saveRecommend(post_id);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    /**
     * 推荐排序
     */
    public void sort() {

        try {
            String[] ids = getParaValues("ids");
            List<PostRecommend> posts = findListByIds(ids);

            for (int i = 0; i < posts.size(); i++) {
                PostRecommend recommend = posts.get(i);
                recommend.set("idx", posts.size() - i);
                PostRecommend.dao.updateRecommend(recommend);
            }
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 推荐排序
     */
    public void batchdelete() {

        try {
            String[] ids = getParaValues("ids");
            List<PostRecommend> posts = findListByIds(ids);
            for (PostRecommend re : posts) {
                PostRecommend.dao.deleteRecommend(re.getLong("id"));
            }
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    /**
     * 推荐排序
     */
    public void delete() {

        try {
            Long id = getParaToLong(-1);
            PostRecommend.dao.deleteRecommend(id);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 获取排序的推荐贴
     *
     * @param ids
     * @return
     * @throws BizException
     */
    private List<PostRecommend> findListByIds(String[] ids) throws BizException {

        if (ids[0].indexOf(",") > 0) {
            ids = ids[0].split(",");
        }
        if (ids == null || ids.length == 0) {
            logger.error("缺少参数ids");
            throw new BizException(ErrorCode.MISSING_PARM);
        }

        List<PostRecommend> posts = new ArrayList<PostRecommend>();

        for (String id : ids) {
            PostRecommend recommend = PostRecommend.dao.findById(id);
            if (recommend == null) {
                logger.error("推荐帖子不存在->id:{}", id);
                throw new BizException(ErrorCode.POST_RECOMMEND_NOT_EXIST);
            }
            posts.add(recommend);
        }
        return posts;
    }

}
