package com.bingqiong.bq.model.uc;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.lang3.StringUtils;

/**
 * 用户 Created by hunsy on 2017/5/10.
 */
public class User extends Model<User> {
    private static final long serialVersionUID = 4507059716352015091L;
    public static User dao = new User();

    /**
     * @param user_id
     * @return
     */
    public Record getByUid(String user_id) {

        return Db
                .findFirst("select * from uc_user where user_id = ? ", user_id);
    }

    /**
     * 编辑用户
     *
     * @param record
     */
    public void updateUser(Record record) {

        Db.update("uc_user", record);
        String user_id = record.getStr("user_id");
        // 头像
        String avatarUrl = record.getStr("avatar");
        // 更新已有评论中的用户和头像
        if (StringUtils.isNotEmpty(avatarUrl)) {
            Db.update(
                    "update t_comment set user_avatar = ? where user_id = ? ",
                    avatarUrl, user_id);
            Db.update(
                    "update t_comment set reply_user_avatar = ? where reply_user_id = ? ",
                    avatarUrl, user_id);
        }
        String user_name = record.getStr("user_name");
        if (StringUtils.isNotEmpty(user_name)) {
            Db.update("update t_comment set user_name = ? where user_id = ? ",
                    user_name, user_id);
            Db.update(
                    "update t_comment set reply_user_name = ? where reply_user_id = ? ",
                    user_name, user_id);
        }
    }

}
