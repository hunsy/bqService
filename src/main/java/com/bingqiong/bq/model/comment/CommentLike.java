package com.bingqiong.bq.model.comment;

import com.bingqiong.bq.comm.constants.Constants;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.utils.MDateKit;
import com.bingqiong.bq.model.BaseModel;
import com.bingqiong.bq.model.post.Post;
import com.bingqiong.bq.model.post.PostLike;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Redis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 评论点赞记录
 * <p>
 * Created by hunsy on 2017/7/3.
 */
public class CommentLike extends BaseModel<CommentLike> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static CommentLike dao = new CommentLike();
    public static String TABLE_COMMENT_LIKE = "t_comment_likes";


    /**
     * 查询是否有点赞记录
     *
     * @param user_id
     * @param comment_id
     * @return
     */
    public CommentLike findByUserIdAndComment(String user_id, Long comment_id) {

        return findFirst("select * from t_comment_likes where user_id = ? and comment_id = ?", user_id, comment_id);
    }

    /**
     * 新增点赞记录
     *
     * @return
     */
    public boolean saveLike(Long comment_id, String user_id) throws BizException {

        Comment comment = Comment.dao.findById(comment_id);
        if (comment == null) {
            throw new BizException(ErrorCode.COMMENT_NOT_EXIST);
        }

        boolean flag = false;
        //已经点赞，取消点赞
        if (liked(comment_id, user_id)) {
            flag = deleteLike(comment_id, user_id);
            //添加点赞记录
        } else {
            CommentLike like = new CommentLike();
            like.set("comment_id", comment_id);
            like.set("user_id", user_id);
            like.set("created_at", MDateKit.getNow());
            flag = like.save();
            if (flag) {
                Redis.use().sadd(Constants.COMMENT_LIKE_PREFIX + comment_id, user_id);
                //点赞数+1
                Comment.dao.updateLikes(comment_id, 1);
            }
        }
        return flag;
    }

    /**
     * 删除点赞
     *
     * @return
     */
    public boolean deleteLike(Long comment_id, String user_id) throws BizException {

        if (!liked(comment_id, user_id)) {
            logger.error("没有点赞记录");
            throw new BizException(ErrorCode.COMMENT_LIKE_NOT_EXIST);
        }

        boolean flag = Db.update("delete from t_comment_likes where comment_id = ? and user_id = ?", comment_id, user_id) == 1;
        if (flag) {
            Redis.use().srem(Constants.COMMENT_LIKE_PREFIX + comment_id, user_id);
            Comment.dao.updateLikes(comment_id, -1);
        }
        return flag;
    }

    /**
     * 是否已经点过赞
     *
     * @return
     */
    public boolean liked(Long comment_id, String user_id) {
        //查看是否存在
        boolean flag = Redis.use().sismember(Constants.POST_LIKE_PREFIX + comment_id, user_id);
        //缓存不存在
        if (!flag) {
            Record record = Db.findFirst("select * from t_comment_likes where comment_id = ? and user_id = ?", comment_id, user_id);
            flag = record != null;
            if (flag) {
                Redis.use().sadd(Constants.COMMENT_LIKE_PREFIX + comment_id, user_id);
            }
        }
        return flag;
    }


}
