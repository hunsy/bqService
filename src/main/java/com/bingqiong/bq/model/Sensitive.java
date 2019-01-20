package com.bingqiong.bq.model;

import com.bingqiong.bq.constant.BqConstants;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Created by hunsy on 2017/5/3.
 */
public class Sensitive extends Model<Sensitive> {


    /**
	 * 
	 */
	private static final long serialVersionUID = -2353175238803216686L;
	public static Sensitive dao = new Sensitive();

    /**
     * 更新，敏感词
     *
     * @param text
     */
    public void updateSensitive(String text) {
        Cache cache = Redis.use();
        Record r = Db.findFirst("select * from t_sensitive where text = ? ", text);
        if (r == null) {
            r = new Record().set("text", text).set("created_at",new Date());
            Db.save("t_sensitive", r);
            cache.sadd(BqConstants.REDIS_SENSITIVE_KEY, text);
        }
    }

    public Page<Record> recordPage(int page, int size, String text) {
        String sql = "select * ";
        String sql_ex = "from t_sensitive where 1 = 1  ";
        if (StringUtils.isNotEmpty(text)) {
            sql_ex += " and text like ? ";
            return Db.paginate(page, size, sql, sql_ex, "%" + text + "%");
        }
        return Db.paginate(page, size, sql, sql_ex);
    }


}
