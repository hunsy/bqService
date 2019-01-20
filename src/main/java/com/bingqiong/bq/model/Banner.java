package com.bingqiong.bq.model;

import com.bingqiong.bq.constant.BqConstants;
import com.bingqiong.bq.model.base.BaseBanner;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by hunsy on 2017/4/11.
 */
public class Banner extends BaseBanner {

    /**
     *
     */
    private static final long serialVersionUID = -6449513293173292883L;
    public static Banner dao = new Banner();
    final String REDIS_BANNER = BqConstants.BQ_APPLICATION + "banner:";
    final String REDIS_BANNER_LIST = BqConstants.BQ_APPLICATION + "banner:list";

    /**
     * @param id
     * @return
     */
    public Banner getById(Long id) throws InstantiationException, IllegalAccessException {

        return getByIdByCache(REDIS_BANNER, id, Banner.class);
    }


    /**
     * 新增
     *
     * @return
     */
    public boolean saveBanner(Banner record) {
        boolean flag = saveEntity(record, REDIS_BANNER);
        return flag;
    }

    /**
     * 更新
     *
     * @param record
     * @return
     */
    @Before(Tx.class)
    public boolean updateBanner(Banner record) {
        boolean flag = updateEntity(record, REDIS_BANNER);
        return flag;
    }

    /**
     * 删除Banner
     *
     * @param banner
     * @return
     * @throws Exception
     */
    public boolean deleteBanner(Banner banner) throws Exception {
        boolean flag = deleteEntity(banner, REDIS_BANNER);
        return flag;
    }

    /**
     * 分页查询
     *
     * @param page
     * @param size
     * @param params
     * @return
     */
    @Before(Tx.class)
    public String findPage(int page, int size, Map<String, String> params) {

        String sql = "select *  ";
        String sql_ex = "from t_banner where valid = 1 ";
        List<Object> lp = new ArrayList<>();
        StringBuilder cacheKey = new StringBuilder();
        cacheKey.append(page).append("_").append(size);
        if (!params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                cacheKey.append("_").append(key).append("_").append(value);
                if ("title".equals(key)) {
                    sql_ex += " and title like ? ";
                    lp.add("%" + value + "%");
                } else {
                    sql_ex += " and " + key + " = ? ";
                    lp.add(value);
                }
            }
        }
        sql_ex += " order by idx desc,created_at desc";
        String str = getPageByCache(REDIS_BANNER, cacheKey.toString(), lp, page, size, sql, sql_ex);
        return str;
    }

    /**
     * 获取Banner列表
     *
     * @return
     */
    public String list() {
        Cache cache = Redis.use();
        String str = cache.get(REDIS_BANNER_LIST);
        if (StringUtils.isEmpty(str)) {
            List<Record> ls = Db.find("select * " +
                    "from t_banner " +
                    "where valid = 1 and status = 1 " +
                    "order by idx desc, created_at desc");
            if (ls != null && ls.size() > 0) {
                str = JsonKit.toJson(ls);
                cache.set(REDIS_BANNER_LIST, str);
            }
        }
        if (StringUtils.isNotEmpty(str)) {
            cache.expire(REDIS_BANNER_LIST, 60 * 60);
        }
        return str;
    }

    @Override
    protected void clearCache() {
        Cache cache = Redis.use();
        cache.del(REDIS_BANNER + REDIS_PAGE);
        cache.del(REDIS_BANNER_LIST);
    }

    /**
     * 初始化Banner。
     */
    public void init() {

        List<Long> ll = Db.query("select id from t_banner where valid = 1");
        if (ll != null && ll.size() > 0) {
            Cache cache = Redis.use();
            for (Long id : ll) {
                logger.info("id:{}", id);
                String sid = id.toString();
                cache.sadd(REDIS_BANNER + REDIS_ID, sid);
            }
        }
    }
}
