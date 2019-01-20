package com.bingqiong.bq.model.post;

import com.bingqiong.bq.comm.constants.Constants;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.utils.MDateKit;
import com.bingqiong.bq.model.BaseModel;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Redis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 点赞记录
 * Created by hunsy on 2017/6/28.
 */
public class PostLike extends BaseModel<PostLike> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static PostLike dao = new PostLike();
    public static final String TABLE_POST_LIKE = "t_post_likes";

    /**
     * 新增帖子点赞记录
     *
     * @param post_id 待点赞的帖子id
     * @param user_id 进行点赞的人的id
     * @return 保存点赞记录的结果
     */
    public boolean saveLike(Long post_id, String user_id) throws BizException {

        Post post = Post.dao.findById(post_id);
        if (post == null) {
            logger.error("点赞帖子不存在->post_id:{}", post_id);
            throw new BizException(ErrorCode.POST_NOT_EXIST);
        }

        boolean flag;
        //已经点赞，取消点赞
        if (liked(post_id, user_id)) {
            flag = deleteLike(post_id, user_id);
            //添加点赞记录
        } else {
            PostLike postLike = new PostLike();
            postLike.set("post_id", post_id);
            postLike.set("user_id", user_id);
            postLike.set("created_at", MDateKit.getNow());
            flag = postLike.save();
            if (flag) {
                Redis.use().sadd(Constants.POST_LIKE_PREFIX + post_id, user_id);
                //点赞数+1
                Post.dao.updateLikes(post_id, 1);
            }
        }

        return flag;
    }


    /**
     * 删除点赞
     *
     * @param post_id 删除点赞记录所在的帖子id
     * @param user_id 删除点赞记录人的id
     * @return 删除点赞记录的结果
     */
    private boolean deleteLike(Long post_id, String user_id) throws BizException {

        if (!liked(post_id, user_id)) {
            logger.error("没有点赞记录");
            throw new BizException(ErrorCode.POST_LIKED_NOT_EXIST);
        }

        boolean flag = Db.update("delete from t_post_likes where post_id = ? and user_id = ?", post_id, user_id) == 1;
        if (flag) {
            Redis.use().srem(Constants.POST_LIKE_PREFIX + post_id, user_id);
            Post.dao.updateLikes(post_id, -1);
        }
        return flag;
    }

    /**
     * 查询用户是否对该帖子点过赞
     *
     * @param post_id 帖子id
     * @param user_id 用户id
     * @return 返回查询结果
     */
    public boolean liked(Long post_id, String user_id) {
        //查看是否存在
        boolean flag = Redis.use().sismember(Constants.POST_LIKE_PREFIX + post_id, user_id);
        //缓存不存在
        if (!flag) {
            Record record = Db.findFirst("select * from t_post_likes where post_id = ? and user_id = ?", post_id, user_id);
            flag = record != null;
            if (flag) {
                Redis.use().sadd(Constants.POST_LIKE_PREFIX + post_id, user_id);
            }
        }
        return flag;
    }
}
