package com.bingqiong.bq.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 评论的统计信息。
 * <p>
 * Created by hunsy on 2017/4/25.
 */
public class CommentStat extends Model<CommentStat> {

    /**
     *
     */
    private static final long serialVersionUID = 1340839914303154497L;
    private Logger logger = LoggerFactory.getLogger(CommentStat.class);
    public static CommentStat dao = new CommentStat();

    /**
     * 保存评论对应的统计信息。
     *
     * @param comment_id
     */
    public void saveStat(Long comment_id) {
        boolean flag = new CommentStat().set("comment_id", comment_id).save();
        if (flag) {
            logger.info("保存comment_stat成功，comment_id:{}", comment_id);
        }
    }

    /**
     * 点赞+-1
     *
     * @param comment_id
     * @param step
     */
    public boolean praiseIncr(String user_id, Long comment_id, int step) {
        CommentStat commentStat = CommentStat.dao.findFirst("select * from t_comment_stat where comment_id = ?", comment_id);
        boolean flag = false;
        if (commentStat != null) {
            logger.info("评论数->{},点赞数{}", comment_id, step);
            flag = commentStat.set("praise_num", commentStat.getInt("praise_num") + step).update();
            if (step > 0) {
                CommentPraiseRec.dao.saveOne(user_id, comment_id);
            } else {
                CommentPraiseRec.dao.deleteOne(user_id, comment_id);
            }
            Comment.dao.clearCache();
        }
        return flag;
    }

    /**
     * 下级数+1。
     * 板块->增加圈子时+1
     * 圈子->增加帖子时+1
     * 下级数-1。
     * 板块->删除圈子时-1
     * 圈子->删除帖子时-1
     *
     * @param comment_id 评论id
     * @param step       步进 +1 -1
     */
    public void childIncr(Long comment_id, int step) {

        Record record = Db.findFirst("select * from t_comment_stat where comment_id= ?", comment_id);
        if (record == null) {
            record = new Record();
            record.set("comment_id", comment_id);
            record.set("children_num", 1);
            Db.save("t_comment_stat", record);
        } else {
            record.set("children_num", record.getInt("children_num") + step);
            Db.update("t_comment_stat", record);
        }
        Comment.dao.clearCache();
    }

}
