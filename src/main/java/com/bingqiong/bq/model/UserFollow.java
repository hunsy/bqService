package com.bingqiong.bq.model;

import java.util.Date;

import com.bingqiong.bq.constant.BqConstants;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Redis;

/**
 * Created by hunsy on 2017/4/11.
 */
public class UserFollow extends Model<UserFollow> {

    /**
     *
     */
    private static final long serialVersionUID = -1578696759351220921L;
    public static UserFollow dao = new UserFollow();

    /**
     * 添加关注
     *
     * @param record
     * @return
     */
    public boolean saveFollow(Record record) {

        record.set("created_at", new Date());
        boolean flag = Db.save("t_user_follow", record);
        //增加圈子的关注数
        if (flag) {
            Record stat = Category.dao.getStat(Long.parseLong(record.get("group_id").toString()));
            stat.set("matter", stat.getInt("matter") + 1);
            Db.update("t_category_stat", stat);
            Redis.use().sadd(BqConstants.REDIS_USER_FOLLOW_PRIFEX + record.get("user_id"), record.get("group_id"));
        }
        return flag;
    }

    /**
     * 取消关注
     *
     * @param record
     * @return
     */
    public boolean deleteFollow(Record record) {

        boolean flag = Db.delete("t_user_follow", record);
        //增加圈子的关注数
        if (flag) {
            Record stat = Category.dao.getStat(Long.parseLong(record.get("group_id").toString()));
            stat.set("matter", stat.getInt("matter") - 1);
            Db.update("t_category_stat", stat);
            Redis.use().srem(BqConstants.REDIS_USER_FOLLOW_PRIFEX + record.get("user_id"), record.get("group_id"));
        }
        return flag;
    }

    /**
     * @param group_id
     * @param user_id
     * @return
     */
    public Record findByUserAndGroup(Long group_id, String user_id) {
        return Db.findFirst("select * from t_user_follow where group_id = ? and user_id = ?", group_id, user_id);
    }
}
