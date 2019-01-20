package com.bingqiong.bq.model;

import com.bingqiong.bq.constant.BqConstants;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Redis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 用户收藏
 * Created by hunsy on 2017/4/11.
 */
public class UserCollection extends Model<UserCollection> {

    /**
     *
     */
    private static final long serialVersionUID = 2682700061926839926L;
    public static UserCollection dao = new UserCollection();
    private Logger logger = LoggerFactory.getLogger(UserCollection.class);

    /**
     * 新增收藏。
     *
     * @return
     */
    @Before(Tx.class)
    public boolean saveCollection(Record record) {

        record.set("created_at", new Date());
        boolean flag = Db.save("t_user_collection", record);
        //保存成功
        if (flag) {
            //将用户的收藏的article放到set集合中
            Redis.use().sadd(BqConstants.REDIS_USER_COLLECTION_PRIFEX + record.get("user_id"), record.get("article_id"));
        }
        return flag;
    }

    /**
     * 删除收藏。
     *
     * @param record
     * @return
     */
    @Before(Tx.class)
    public boolean deleteCollection(Record record) {
        record.set("valid", 0);
        boolean flag = Db.update("t_user_collection", record);
        //删除成功
        if (flag) {
            //将用户的收藏的article放到set集合中
            logger.info("key:{},field:{}", BqConstants.REDIS_USER_COLLECTION_PRIFEX + record.get("user_id"), record.get("article_id"));
            Redis.use().srem(BqConstants.REDIS_USER_COLLECTION_PRIFEX + record.get("user_id"), record.get("article_id"));
        }
        return flag;
    }

    /**
     * @param id
     * @return
     */
    public Record getById(Long id) {

        Record record = Db.findFirst("select * from t_user_collection where valid = 1 and id = ? ", id);
        return record;
    }


}
