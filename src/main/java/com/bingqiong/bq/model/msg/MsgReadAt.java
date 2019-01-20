package com.bingqiong.bq.model.msg;

import com.bingqiong.bq.comm.utils.MDateKit;
import com.bingqiong.bq.model.BaseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息读取时间
 * Created by hunsy on 2017/7/7.
 */
public class MsgReadAt extends BaseModel<MsgReadAt> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static MsgReadAt dao = new MsgReadAt();
    public static String TABLE_READ_AT = "t_msg_read_at";

    /**
     * @param user_id 用户id（设备号）
     * @param type    类型  1 reply 2 pm 3 sm
     * @return 返回查询结果
     */
    public MsgReadAt get(String user_id, int type) {
        logger.info("user_id:{}", user_id);
        return findFirst("select * from t_msg_read_at where user_id= ? and type = ?", user_id, type);
    }

    /**
     * 新增
     *
     * @param user_id 用户id（设备号）
     * @param type    类型
     * @return 返回创建结果
     */
    public boolean createdReadAt(String user_id, int type) {
        boolean flag = false;
        MsgReadAt dbReadAt = get(user_id, type);
        if (dbReadAt == null) {
            dbReadAt = new MsgReadAt();
            dbReadAt.set("user_id", user_id);
            dbReadAt.set("type", type);
            dbReadAt.set("read_at", MDateKit.getNow());
            flag = dbReadAt.save();
        } else {
            dbReadAt.set("read_at", MDateKit.getNow());
            dbReadAt.update();
        }
        return flag;
    }

}
