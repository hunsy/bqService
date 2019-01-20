package com.bingqiong.bq.model.user;

import com.bingqiong.bq.comm.constants.Constants;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.utils.MDateKit;
import com.bingqiong.bq.model.BaseModel;
import com.bingqiong.bq.model.post.PostTag;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Redis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 用户所拥有的标签(PostTag帖子标签)
 * Created by hunsy on 2017/6/30.
 */
public class UserTags extends BaseModel<UserTags> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final UserTags dao = new UserTags();
    public static final String TABLE_USER_TAGS = "t_user_tags";

    /**
     * 新增用户标签
     *
     * @param user_id 用户id
     * @param tag_id  标签id
     * @return 返回保存结果
     */
    @Before(Tx.class)
    public boolean saveTags(String user_id, Long tag_id) throws BizException {

        PostTag tag = PostTag.dao.findById(tag_id);
        if (tag == null) {
            logger.info("标签不存在");
            throw new BizException(ErrorCode.POST_TAG_NOT_EXIST);
        }

        UserTags dbTags = findByUserAndTag(user_id, tag_id);
        //已经存在标签，则新增标签数量
        if (dbTags != null) {
            return updateTagsCount(dbTags);
        }
        dbTags = new UserTags();
        dbTags.set("tag_id", tag_id);
        dbTags.set("user_id", user_id);
        dbTags.set("created_at", MDateKit.getNow());
        boolean flag = dbTags.save();
        if (flag) {
            dbTags = findByUserAndTag(user_id, tag_id);
            Redis.use().hset(Constants.REDIS_USER_TAGS_KEY, user_id + tag_id, dbTags);
        }
        return flag;
    }

    /**
     * 获取用户与标签关系
     *
     * @param user_id 用户id
     * @param tag_id  标签id
     * @return 返回查询结果
     */
    private UserTags findByUserAndTag(String user_id, Long tag_id) {

        UserTags tags = Redis.use().hget(Constants.REDIS_USER_TAGS_KEY, user_id + tag_id);
        if (tags == null) {
            tags = findFirst("select * from t_user_tags where user_id = ? and tag_id = ? ", user_id, tag_id);
            if (tags != null) {
                Redis.use().hset(Constants.REDIS_USER_TAGS_KEY, user_id + tag_id, tags);
            }
        }
        return tags;
    }

    /**
     * 更新用户标签
     *
     * @param tags 用户标签关系记录
     * @return 返回更新结果
     */
    @Before(Tx.class)
    private boolean updateTags(UserTags tags) {

        boolean flag = tags.update();
        if (flag) {
            tags = findByUserAndTag(tags.getStr("user_id"), tags.getLong("tag_id"));
            Redis.use().hset(Constants.REDIS_USER_TAGS_KEY, tags.getStr("user_id") + tags.getLong("tag_id"), tags);
        }
        return flag;
    }

    /**
     * 新增用户与标签关系的次数
     *
     * @param tags 用户标签关系记录
     * @return 返回更新结果
     */
    private boolean updateTagsCount(UserTags tags) {

        tags.set("count", tags.getInt("count") + 1);
        return updateTags(tags);
    }

    /**
     * 查询用户下的标签，12个或所有
     *
     * @param user_id 用户id
     * @param limit   是否有限数量
     * @return 返回用户标签记录列表结果
     */
    public List<Record> findList(String user_id, boolean limit) {

        String sql = "select tu.tag_id,tut.name as tag_name,tu.count " +
                "from t_user_tags tu " +
                "left join t_post_tag tut on tu.tag_id = tut.id " +
                "where tu.user_id = ? order by tu.count desc ";

        if (limit) {
            return Db.find(sql + " limit 0,12", user_id);
        }
        return Db.find(sql, user_id);
    }

    /**
     * 删除用户标签
     *
     * @param user_id 用户id
     * @param tag_id  标签id
     * @return 返回删除结果
     */
    public boolean deleteTags(String user_id, Long tag_id) throws BizException {

        UserTags tags = findByUserAndTag(user_id, tag_id);
        if (tags == null) {
            throw new BizException(ErrorCode.USER_TAG_NOT_EXIST);
        }

        boolean flag = tags.delete();
        if (flag) {
            //清缓存
            Redis.use().hdel(Constants.REDIS_USER_TAGS_KEY, user_id + tag_id);
        }
        return flag;
    }
}
