package com.bingqiong.bq.model.user;

import com.bingqiong.bq.comm.constants.Constants;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.utils.MDateKit;
import com.bingqiong.bq.model.BaseModel;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Redis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 屏蔽用户
 * Created by hunsy on 2017/7/5.
 */
public class UserShields extends BaseModel<UserShields> {


    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final UserShields dao = new UserShields();
    public static final String TABLE_USER_SHIELDS = "t_user_shields";


    /**
     * 新增屏蔽
     *
     * @param shields 屏蔽记录
     * @return 返回保存屏蔽结果
     */
    @Before(Tx.class)
    public boolean saveShield(UserShields shields) throws Exception {

        String shield_id = shields.getStr("shield_id");
        User user = User.dao.findByUserId(shield_id);
        if (user == null) {
            logger.info("用户不存在");
            throw new BizException(ErrorCode.USER_NOT_EXIST);
        }

        boolean s = shiled(shields.getStr("user_id"), shield_id);
        if (s) {
            throw new BizException(ErrorCode.USER_SHIELD_EXIST);
        }

        shields.set("created_at", MDateKit.getNow());
        boolean flag = shields.save();
        if (flag) {
            Redis.use().sadd(Constants.REDIS_USER_SHIELD_KEY + shields.getStr("user_id"), shield_id);
        }
        return flag;
    }


    /**
     * 取消屏蔽
     *
     * @param user_id   用户id
     * @param shield_id 被屏蔽着id
     * @return 返回删除结果
     */
    @Before(Tx.class)
    public boolean deleteShield(String user_id, String shield_id) {

        boolean flag = Db.update("delete from t_user_shields where user_id = ? and shield_id = ?", user_id, shield_id) == 1;
        if (flag) {
            Redis.use().srem(Constants.REDIS_USER_SHIELD_KEY + user_id, shield_id);
        }
        return flag;
    }

    /**
     * 是否屏蔽该用户
     *
     * @param user_id   用户id
     * @param shield_id 被屏蔽者id
     * @return 返回查询结果
     */
    public boolean shiled(String user_id, String shield_id) {

        return Redis.use().sismember(Constants.REDIS_USER_SHIELD_KEY + user_id, shield_id);
    }


}
