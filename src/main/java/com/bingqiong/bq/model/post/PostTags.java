package com.bingqiong.bq.model.post;

import com.bingqiong.bq.comm.constants.Constants;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.utils.MDateKit;
import com.bingqiong.bq.model.BaseModel;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Redis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 帖子与帖子标签的关系
 * Created by hunsy on 2017/6/23.
 */
public class PostTags extends BaseModel<PostTags> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final PostTags dao = new PostTags();
    public static final String TABLE_POST_TAGS = "t_post_tags";

    /**
     * 获取帖子下的标签
     * status = 1时取通过的标签
     *
     * @param post_id 帖子标签
     * @return 返回该帖子的标签记录列表
     */
    public List<Record> findTagsByPost(Long post_id) {

        List<Record> list = Redis.use().get(Constants.REDIS_POST_TAGS_LIST_KEY);
        if (list == null) {
            String sql = "select tpts.id,tpt.name as tag_name,tpts.tag_id,tpts.post_id,tpts.created_at,tpts.status " +
                    "from t_post_tags tpts " +
                    "left join t_post_tag tpt on tpts.tag_id = tpt.id " +
                    "where tpts.post_id = ? ";
            sql += " order by tpt.sys desc,tpt.idx desc,tpts.created_at desc";
            list = Db.find(sql, post_id);
            if (list != null) {
                Redis.use().setex(Constants.REDIS_POST_TAGS_LIST_KEY + post_id, 60 * 60 * 24 * 7, list);
            }
        }
        return list;
    }

    /**
     * 新增帖子与标签关系
     *
     * @param postTags 帖子与标签的关系记录
     * @return 返回保存结果
     */
    @Before(Tx.class)
    public boolean saveTags(PostTags postTags) throws BizException {
        Long tag_id = postTags.getLong("tag_id");
        PostTag dbTag = PostTag.dao.findById(tag_id);
        if (dbTag == null) {
            logger.error("标签不存在");
            throw new BizException(ErrorCode.POST_TAG_NOT_EXIST);
        }
        Long post_id = postTags.getLong("post_id");
        Post dbPost = Post.dao.findById(post_id);
        if (dbPost == null) {
            logger.error("帖子不存在");
            throw new BizException(ErrorCode.POST_NOT_EXIST);
        }
        postTags.set("created_at", MDateKit.getNow());
        boolean flag = postTags.save();
        if (flag) {
            //查询最新的记录，同时会缓存
            findByTagAndPost(tag_id, post_id);
            //清除该帖子的列表缓存
            removeListCache(post_id);
        }
        return flag;
    }

    /**
     * 删除帖子与标签关系
     *
     * @param tag_id  标签id
     * @param post_id 帖子id
     * @return 返回删除结果
     * @throws BizException
     */
    @Before(Tx.class)
    public boolean deleteTags(Long tag_id, Long post_id) throws BizException {

        PostTags tags = findByTagAndPost(tag_id, post_id);
        if (tags == null) {
            logger.error("不存在该标签记录");
            throw new BizException(ErrorCode.POST_NOT_SOUCH_TAG);
        }
        boolean flag = tags.delete();
        if (flag) {
            Redis.use().hdel(Constants.REDIS_POST_TAGS_KEY, post_id + "" + tag_id);
            removeListCache(post_id);
        }
        return flag;
    }


    /**
     * @param tag_id  帖子id
     * @param post_id 标签id
     * @return 返回查询到的帖子对应标签的记录结果
     */
    private PostTags findByTagAndPost(Long tag_id, Long post_id) {
        if (tag_id == null || post_id == null) return null;
        PostTags tags = Redis.use().hget(Constants.REDIS_POST_TAGS_KEY, post_id + "" + tag_id);
        if (tags == null) {
            tags = findFirst("select * from t_post_tags where post_id = ? and tag_id = ? ", post_id, tag_id);
            if (tags != null) {
                Redis.use().hset(Constants.REDIS_POST_TAGS_KEY, post_id + "" + tag_id, tags);
            }
        }
        return tags;
    }

    /**
     * 清除帖子下的所有标签
     *
     * @param post_id 帖子id
     */
    public void deleteByPost(Long post_id) throws BizException {

        List<Record> records = findTagsByPost(post_id);
        if (records != null) {
            for (Record record : records) {
                deleteTags(record.getLong("tag_id"), record.getLong("post_id"));
            }
        }
    }

    /**
     * 删除帖子下的标签列表缓存
     *
     * @param post_id 帖子id
     */
    private void removeListCache(Long post_id) {
        Redis.use().del(Constants.REDIS_POST_TAGS_LIST_KEY + post_id);
    }
}
