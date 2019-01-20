package com.bingqiong.bq.cms.controller.post;

import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.model.post.Post;
import com.bingqiong.bq.model.post.PostTags;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 帖子与标签关系请求
 * <p>
 * Created by hunsy on 2017/6/26.
 */
public class PostTagsController extends IBaseController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 帖子下的所有标签
     */
    public void list() {

        try {

            Long post_id = getParaToLong("post_id");
            if (Post.dao.findById(post_id) == null) {
                logger.error("帖子不存在");
                throw new BizException(ErrorCode.POST_NOT_EXIST);
            }
            List<Record> records = PostTags.dao.findTagsByPost(post_id);
            renderSuccess(records);
        } catch (Exception e) {

            renderFailure(e);
        }
    }

    /**
     * 给帖子增加标签
     */
    public void save() {

        try {

            Long post_id = getParaToLong("post_id");
            String[] tag_ids = getParaValues("tag_ids");
            if (post_id == null) {

                throw new BizException(ErrorCode.MISSING_PARM);
            }

            //清除原有
            PostTags.dao.deleteByPost(post_id);

            if (StringUtils.isNotEmpty(tag_ids[0])) {
                if (tag_ids[0].indexOf(",") > 0) {
                    tag_ids = tag_ids[0].split(",");
                }

                for (int i = 0; i < tag_ids.length; i++) {
                    PostTags tags = new PostTags();
                    tags.set("post_id", post_id);
                    tags.set("tag_id", Long.parseLong(tag_ids[i]));
                    PostTags.dao.saveTags(tags);
                }
            }
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
            PostTags tags = PostTags.dao.findById(id);

            PostTags.dao.deleteTags(tags.getLong("tag_id"), tags.getLong("post_id"));
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

}
