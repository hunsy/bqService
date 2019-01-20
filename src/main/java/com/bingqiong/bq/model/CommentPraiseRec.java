package com.bingqiong.bq.model;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * Created by hunsy on 2017/5/3.
 */
public class CommentPraiseRec extends Model<CommentPraiseRec> {

    /**
     *
     */
    private static final long serialVersionUID = -5897296817358579787L;
    public static CommentPraiseRec dao = new CommentPraiseRec();

    /**
     * 用户是否对评论点过赞
     *
     * @return
     */
    public boolean praiseed(Long comment_id, String user_id) {
        Record record = Db.findFirst("select * from t_comment_praise_rec where comment_id =? and user_id = ?", comment_id, user_id);
        if (record != null) {
            return true;
        }
        return false;
    }

    public void saveOne(String user_id, Long comment_id) {
        Db.update("insert into t_comment_praise_rec (comment_id,user_id)  values(?,?)", comment_id, user_id);
    }

    public void deleteOne(String user_id, Long comment_id) {
        Db.update("delete from t_comment_praise_rec where comment_id = ? and user_id = ?", comment_id, user_id);
    }
}
