package com.bingqiong.bq.model.user;

import com.bingqiong.bq.comm.constants.Constants;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.utils.MDateKit;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.BaseModel;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Redis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户实名认证card
 * Created by hunsy on 2017/6/28.
 */
public class UserCard extends BaseModel<UserCard> {


    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final UserCard dao = new UserCard();
    public static final String TABLE_USER_CARD = "t_user_card";

    /**
     * 保存实名认证信息
     */
    public boolean saveCard(Record card) throws BizException {
        card.set("created_at", MDateKit.getNow());
        boolean flag = Db.save("t_user_card", card);
        if (flag) {
            logger.info("保存用户实名信息成功");
            //缓存
            Redis.use().hset(Constants.REDIS_USER_CARD_KEY, card.getStr("user_id"), card);

        }
        return flag;
    }

    /**
     * 已经实名认证了
     */
    public Record getCard(String user_id) {

        Record card = Redis.use().hget(Constants.REDIS_USER_CARD_KEY, user_id);
        if (card == null) {
            card = Db.findFirst("select * from t_user_card where user_id = ?", user_id);
            if (card != null) {
                Redis.use().hset(Constants.REDIS_USER_CARD_KEY, user_id, card);
            }
        }
        return card;
    }


    /**
     * 分页查询
     *
     * @param pageRequest 请求参数
     * @return 返回分页数据
     */
    public Page<Record> findPage(PageRequest pageRequest) {

        String sql = "select * ";
        String sql_ex = "from t_user_card " +
                "where 1 = 1 ";

        sql_ex += pageRequest.getSimple();
        sql_ex += " order by created_at desc";

        return Db.paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, pageRequest.getSimpleValues());
    }
}
