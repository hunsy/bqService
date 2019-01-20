package com.bingqiong.bq.model.user;

import com.bingqiong.bq.comm.constants.Constants;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.utils.MDateKit;
import com.bingqiong.bq.model.BaseModel;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Redis;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户关注
 * <p>
 * Created by hunsy on 2017/6/28.
 */
public class UserFollows extends BaseModel<UserFollows> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final UserFollows dao = new UserFollows();
    public static final String TABLE_USER_FOLLOWS = "t_user_follows";

    public static final String msg = "我已关注你了";

    /**
     * 新增follow
     *
     * @param follows 关注记录
     * @return 返回保存关注结果
     */
    @Before(Tx.class)
    public boolean saveFollows(UserFollows follows) throws Exception {

        String followed_id = follows.getStr("followed_id");
        if (StringUtils.isEmpty(followed_id)) {

            throw new BizException(ErrorCode.USER_FOLLOWED_NOT_NULL);
        }
        User followed = User.dao.findByUserId(followed_id);
        if (followed == null) {
            logger.error("已经关注");
            throw new BizException(ErrorCode.USER_FOLLOWED_NOT_EXIST);
        }

        String user_id = follows.getStr("user_id");
        UserFollows dbFollows = findByUserIdAndFollowed(user_id, followed_id);
        if (dbFollows != null) {

            throw new BizException(ErrorCode.USER_FOLLOWED_EXIST);
        }
        follows.set("created_at", MDateKit.getNow());
        boolean flag = follows.save();
        if (flag) {
            //更新用户的关注人数
            User.dao.updateFollows(user_id, 1);
            //更新用户的被关注人数
            User.dao.updateFolloweds(followed_id, 1);
            Redis.use().hset(Constants.REDIS_USER_FOLLOWS_KEY, user_id + followed_id, findByUserIdAndFollowed(user_id, followed_id));
        }
        return false;
    }

    /**
     * 更新
     *
     * @param follows
     * @return
     */
    @Before(Tx.class)
    public boolean updateFollows(UserFollows follows) {

        boolean flag = follows.update();
        String user_id = follows.getStr("user_id");
        String followed_id = follows.getStr("followed_id");
        if (flag) {
            Redis.use().hset(Constants.REDIS_USER_FOLLOWS_KEY, user_id + followed_id, findByUserIdAndFollowed(user_id, followed_id));
        }
        return flag;
    }


    /**
     * 获取
     *
     * @param user_id     用户id
     * @param followed_id 关注id
     * @return 返回查询结果
     */
    public UserFollows findByUserIdAndFollowed(String user_id, String followed_id) {

        UserFollows follows = Redis.use().hget(Constants.REDIS_USER_FOLLOWS_KEY, user_id + followed_id);
        if (follows == null) {
            follows = findFirst("select * from t_user_follows where user_id = ? and followed_id = ? ", user_id, followed_id);
            if (follows != null) {
                Redis.use().hset(Constants.REDIS_USER_FOLLOWS_KEY, user_id + followed_id, follows);
            }
        }
        return follows;
    }

    /**
     * 取消关注
     */
    @Before(Tx.class)
    public boolean deleteFollows(String user_id, String followed_id) throws Exception {

        UserFollows follows = findByUserIdAndFollowed(user_id, followed_id);
        if (follows == null) {
            throw new BizException(ErrorCode.USER_FOLLOWED_NOT_EXIST);
        }

        boolean flag = follows.delete();
        if (flag) {
            Redis.use().hdel(Constants.REDIS_USER_FOLLOWS_KEY, user_id + followed_id);
            //更新用户的关注人数
            User.dao.updateFollows(user_id, -1);
            //更新用户的被关注人数
            User.dao.updateFolloweds(followed_id, -1);
        }
        return flag;
    }

}
