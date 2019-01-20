package com.bingqiong.bq.model;

import com.bingqiong.bq.constant.BqConstants;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Redis;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by hunsy on 2017/4/12.
 */
public class ArticleType extends Model<ArticleType> {

    /**
     *
     */
    private static final long serialVersionUID = -7379966487772357518L;
    public static ArticleType dao = new ArticleType();

    /**
     * 保存文章类型
     *
     * @param record
     * @return
     */
    @Before(Tx.class)
    public boolean saveType(Record record) {
        Date date = new Date();
        record.set("updated_at", date);
        record.set("created_at", date);
        boolean flag = Db.save("t_article_type", record);
        if (flag) {
            Redis.use().del(BqConstants.REDIS_ARTICLE_TYPE_LIST);
        }
        return flag;
    }

    /**
     * 更新
     *
     * @param record
     * @return
     */
    @Before(Tx.class)
    public boolean updatetype(Record record) {
        record.set("updated_at", new Date());
        boolean b = Db.update("t_article_type", record);
        if (b) {
            Redis.use().del(BqConstants.REDIS_ARTICLE_TYPE_LIST);
        }
        return b;
    }

    /**
     * 获取列表
     *
     * @return
     */
    public Page<Record> page(int page, int size, String name) {

        String sql = "select * ";
        String sql_ex = " from  t_article_type where valid = 1 ";

        if (StringUtils.isNotEmpty(name)) {
            sql_ex += " and name like ? order by idx desc,created_at desc";
            return Db.paginate(page, size, sql, sql_ex, name);
        } else {
            sql_ex += " order by idx desc,created_at desc";
            return Db.paginate(page, size, sql, sql_ex);
        }
    }

    /**
     * 获取仅仅名字的列表
     *
     * @return
     */
    public List<String> listNames() {
        List<Record> records = Db.find("select name from t_article_type where valid = 1 order by idx desc");
        List<String> ls = new ArrayList<>();
        if (records != null){
            for (Record r:records) {
                ls.add(r.getStr("name"));
            }
        }
        return ls;
    }

    public Record getById(Long id) {
        return Db.findFirst("select * from t_article_type where valid = 1 and id = ? ", id);
    }
}
