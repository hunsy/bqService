package com.bingqiong.bq.model.base;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

/**
 * Created by hunsy on 2017/4/26.
 */
@SuppressWarnings({"serial"})
public abstract class BaseModel<M extends Model<M>> extends Model<M> {

    protected Logger logger = LoggerFactory.getLogger(BaseModel.class);

    protected final String REDIS_ID = "id";
    protected final String REDIS_DETAIL = "detail";
    protected final String REDIS_PAGE = "page";

    /**
     * @param prefix 缓存KEY的前缀
     * @param id
     * @return
     */
    protected M getByIdByCache(String prefix, Long id, Class<M> clazz)
            throws IllegalAccessException, InstantiationException {
        logger.info("\n");
        String cacheKey = prefix + REDIS_DETAIL;
        String setKey = prefix + REDIS_ID;
        Cache redisCache = Redis.use();
        if (id == null) {
            logger.info("通过id获取对象,id不能为null,obj:{}", clazz.getName());
            return null;
        }
        String cache = redisCache.hget(cacheKey, id);
        logger.info("获取缓存->key:{}", cacheKey);
        logger.info("filed:{},value:{}", id, cache);
        M m = null;
        // 存在缓存，则组装为Model
        if (StringUtils.isNotEmpty(cache)) {
            HashMap<String, Object> map = JSON.parseObject(cache,
                    HashMap.class, Feature.AllowArbitraryCommas);
            m = (M) clazz.newInstance().setAttrs(map);
        }
        // 不存在缓存，则查询Model并缓存
        if (m == null) {
            // 在对象id的set中存在，才进行sql查询。
            // 这个需要在保存对象成功后缓存id。
            // if (redisCache.sismember(setKey, id.toString())) {
            M fm = findById(id);
            if (fm != null && fm.getInt("valid") == 1) {
                m = fm;
                redisCache.hset(cacheKey, id, JsonKit.toJson(m));
                logger.info("保存缓存->key:{}", cacheKey);
                logger.info("filed:{},value:{}", id, cache);
            }
            // }
        }
        // 如果Model存在，则设置缓存期限。最多2天
        if (m != null) {
            int expire = getExpire(60 * 60 * 24);
            logger.info("设置expire->expire:{}", expire);
            redisCache.expire(cacheKey, expire);
        }
        return m;
    }

    /**
     * 分页请求，缓存1~2分钟。不延期，到期释放。以便更新数据
     *
     * @param prefix
     * @param field
     * @param lp
     * @param page
     * @param size
     * @param sql
     * @param sql_ex
     * @return
     */
    protected String getPageByCache(String prefix, String field,
                                    List<Object> lp, int page, int size, String sql, String sql_ex) {
        logger.info("\n");
        String cacheKey = prefix + REDIS_PAGE;
        logger.info("cache key:{},field:{}", cacheKey, field);
        Cache cache = Redis.use();
        String str = cache.hget(cacheKey, field);
        if (StringUtils.isEmpty(str)) {
            Page<Record> ps;
            if (lp.isEmpty()) {
                ps = Db.paginate(page, size, sql, sql_ex);
            } else {
                ps = Db.paginate(page, size, sql, sql_ex, lp.toArray());
            }
            str = JsonKit.toJson(ps);
            if (StringUtils.isNotEmpty(str)) {
                cache.hset(cacheKey, field, str);
                // 缓存1~2分钟
                cache.expire(cacheKey, getExpire(60));
            }
        }
        return str;
    }

    /**
     * 保存对象。
     *
     * @param m      待保存的对象
     * @param prefix 存放id的set的key
     * @return
     */
    @Before(Tx.class)
    public boolean saveEntity(M m, String prefix) {
        logger.info("\n");
        Date date = new Date();
        m.set("created_at", date);
        m.set("updated_at", date);
        boolean flag = m.save();
        if (flag) {
            Cache cache = Redis.use();
            cache.sadd(prefix + REDIS_ID, m.get("id").toString());
            logger.info("增加set key:{}下的值id,id:{}", prefix + REDIS_ID,
                    m.getLong("id") + "");
            // 清除缓存
            // page缓存，list缓存
            clearCache();
        }
        return flag;
    }

    /**
     * 更新
     *
     * @param m      待更新的对象
     * @param prefix 缓存id对应具体内容的hash的key
     */
    @Before(Tx.class)
    public boolean updateEntity(M m, String prefix) {
        logger.info("\n");
        Date d = new Date();
        m.set("updated_at", d);
        boolean flag = m.update();
        logger.info("更新对象->result:{},id:{}", flag, m.get("id"));
        // 清除缓存
        if (flag) {
            Redis.use().hdel(prefix + REDIS_DETAIL, m.get("id"));
            logger.info("清除缓存->id:{}", m.get("id"));
            clearCache();
        }
        return flag;
    }

    /**
     * @param m      待更新的对象
     * @param prefix 缓存具体内容hash的key
     */
    public boolean deleteEntity(M m, String prefix) {
        logger.info("\n");
        Date d = new Date();
        m.set("updated_at", d);
        m.set("valid", 0);
        boolean flag = m.update();
        // 清缓存
        if (flag) {
            Cache cache = Redis.use();
            // 删除Set中的id。
            cache.srem(prefix + REDIS_ID, m.get("id"));
            logger.info("删除set key:{}下的值id,id:{}", prefix + REDIS_ID,
                    m.get("id"));
            // 删除Hash中的具体内容
            cache.hdel(prefix + REDIS_DETAIL, m.get("id"));
            logger.info("删除hash key:{}下的具体内容，id:{}", prefix + REDIS_DETAIL,
                    m.get("id"));
            clearCache();
        }
        return flag;
    }

    /**
     * 清除缓存，需要继承对象重写这个方法，实现清除缓存。
     */
    protected void clearCache() {
    }

    /**
     * 获取数据有效期. 为防止缓存一下到期，使用不同时长的缓存有效期。
     *
     * @return
     */
    protected int getExpire(int base) {
        base = base + new Random().nextInt(base);
        return base;
    }

    public static void main(String[] args) {
        // System.out.print(getExpire());
    }

}
