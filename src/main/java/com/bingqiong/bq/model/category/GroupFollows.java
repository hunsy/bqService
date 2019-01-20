package com.bingqiong.bq.model.category;

import com.bingqiong.bq.comm.constants.Constants;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.utils.MDateKit;
import com.bingqiong.bq.model.BaseModel;
import com.bingqiong.bq.model.user.User;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Redis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 圈子的关注记录
 * Created by hunsy on 2017/6/28.
 */
public class GroupFollows extends BaseModel<GroupFollows> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final GroupFollows dao = new GroupFollows();
    public static final String TABLE_GROUP_FOLLOWS = "t_group_follows";

    /**
     * 保存用户关注记录
     *
     * @param groupFollows 圈子的关注记录
     * @return 返回新增结果
     */
    @Before(Tx.class)
    public boolean saveGroupFollows(GroupFollows groupFollows) throws Exception {

        Long group_id = groupFollows.getLong("group_id");
        String user_id = groupFollows.getStr("user_id");
        Group dbGroup = Group.dao.findById(group_id);
        if (dbGroup == null) {
            logger.error("圈子不存在->group_id:{}", group_id);
            throw new BizException(ErrorCode.GROUP_NOT_EXIST);
        }

        GroupFollows dbfollows = userFollowed(user_id, group_id);
        if (dbfollows != null) {
            logger.error("用户已经关注圈子了->user_id:{},group_id:{}", user_id, group_id);
            throw new BizException(ErrorCode.GROUP_FOLLOWS_EXIST);
        }
        groupFollows.set("created_at", MDateKit.getNow());
        boolean flag = groupFollows.save();
        if (flag) {
            //增加圈子的关注数
            Group.dao.updateFollows(group_id, 1);
            //增加用户的关注的圈子数
            User.dao.updateGroups(groupFollows.getStr("user_id"), 1);
            //已 user_id + group_id为field缓存
            Redis.use().hset(Constants.REDIS_GROUP_FOLLOWS_KEY, user_id + group_id, groupFollows);
        }
        return flag;
    }

    /**
     * 删除关注记录
     *
     * @param user_id  关注人
     * @param group_id 关注圈子
     * @return 返回删除结果
     */
    @Before(Tx.class)
    public boolean deleteFollows(String user_id, Long group_id) throws Exception {

        GroupFollows follows = userFollowed(user_id, group_id);
        if (follows == null) {
            logger.error("不存在关注记录->user_id:{},group_id:{}", user_id, group_id);
            throw new BizException(ErrorCode.GROUP_FOLLOWS_NOT_EXIST);
        }
        boolean flag = follows.delete();
        if (flag) {
            //减少圈子的关注数
            Group.dao.updateFollows(follows.getLong("group_id"), -1);
            //减少用户的关注的帖子数
            User.dao.updateGroups(user_id, -1);
            //已用户id为field缓存
            Redis.use().hdel(Constants.REDIS_GROUP_FOLLOWS_KEY, user_id + group_id);
        }
        return flag;
    }

    /**
     * 用户是否关注圈子
     *
     * @return 返回关注记录
     */
    public GroupFollows userFollowed(String user_id, Long group_id) {

        GroupFollows follows = Redis.use().hget(Constants.REDIS_GROUP_FOLLOWS_KEY, user_id + group_id);
        if (follows == null) {
            follows = findFirst("select * from t_group_follows where user_id = ? and group_id = ?", user_id, group_id);
            if (follows != null) {
                //已用户id为field缓存
                Redis.use().hset(Constants.REDIS_GROUP_FOLLOWS_KEY, user_id + group_id, follows);
            }
        }
        return follows;
    }

}
