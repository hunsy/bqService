package com.bingqiong.bq.model.msg;

import com.bingqiong.bq.comm.utils.MDateKit;
import com.bingqiong.bq.model.BaseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 谁给当前用户发过消息
 * Created by hunsy on 2017/7/24.
 */
public class PmFrom extends BaseModel<PmFrom> {


    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final PmFrom dao = new PmFrom();
    public static final String TABLE_PM_FROM = "t_pm_from";

    /**
     * 给user_id增加一条消息来自谁
     *
     * @param user_id 用户id
     * @param from    消息来自用户
     * @param msg_id  最后一条消息的id
     */
    void saveFrom(String user_id, String from, Long msg_id) {

        PmFrom pmFrom = findPmFrom(user_id, from);
        Date date = MDateKit.getNow();
        //新增一条记录
        if (pmFrom == null) {
            logger.info("新增一条记录");
            pmFrom = new PmFrom();
            pmFrom.set("user_id", user_id);
            pmFrom.set("from_user", from);
            pmFrom.set("updated_at", date);
            pmFrom.set("last_msg", msg_id);
            pmFrom.save();
        } else {
            logger.info("更新消息数");
            //给from增加一条计数
            pmFrom.set("last_msg", msg_id);
            pmFrom.set("count", pmFrom.getInt("count") + 1);
            pmFrom.set("updated_at", date);
            pmFrom.update();
        }
    }

    /**
     * 获取是否已经存在来自from的记录
     *
     * @param user_id 用户id
     * @param from    来自用户id
     * @return 记录
     */
    private PmFrom findPmFrom(String user_id, String from) {

        return findFirst("select * from t_pm_from where user_id = ? and from_user = ?", user_id, from);
    }


    /**
     * 清空消息的未读数
     *
     * @param user_id 用户id
     * @param from    消息来自用户id
     */
    void clearCount(String user_id, String from) {

        PmFrom pm = findPmFrom(user_id, from);
        pm.set("count", 0);
        pm.update();
    }
}
