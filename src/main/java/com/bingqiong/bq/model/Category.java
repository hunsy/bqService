package com.bingqiong.bq.model;

import com.bingqiong.bq.constant.BqConstants;
import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.model.base.BaseCategory;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 分类-> 板块/圈子
 * <p>
 * Created by hunsy on 2017/4/7.
 */
public class Category extends BaseCategory {

    /**
     *
     */
    private static final long serialVersionUID = 6715776095660088621L;
    public static Category dao = new Category();
    /**
     * 保存id的set的key
     */
    private final String REDIS_CATEGORY = BqConstants.BQ_APPLICATION + "category:";

    /**
     * 检查是否是group|plate
     *
     * @param record
     * @throws BizException
     */
    public void checkCategory(Category record, String type) throws BizException {
        logger.info("---{}", JsonKit.toJson(record));
        if (record == null || !record.getStr("type").equals(type)) {
            logger.error("对象不存在|对象类型不正确");
            throw new BizException(BqErrorCode.CODE_FAILED.getCode());
        }
    }


    /**
     * @param id
     * @return
     */
    public Category getById(Long id) throws InstantiationException, IllegalAccessException {
        Category record = getByIdByCache(REDIS_CATEGORY, id, Category.class);
        return record;
    }

    /**
     * 获取板块分页。
     *
     * @param page
     * @param size
     * @param params
     * @return
     */
    public String platePage(int page, int size, Map<String, String> params) throws Exception {
        String sql = "select tc.id,tc.name,tc.status,tc.created_at,tcs.children_num ";
        String sql_ex = "from t_category tc " +
                "left join t_category_stat tcs on tc.id = tcs.category_id " +
                "where tc.valid = 1 and tc.parent_id = ? and type = ? ";
        List<Object> lp = new ArrayList<>();
        lp.add(0);
        lp.add("plate");
        return commonPage(0L, sql, sql_ex, page, size, lp, params);
    }

    /**
     * 获取板块下的圈子分页。
     *
     * @param plate_id
     * @param page
     * @param size
     * @param params
     * @return
     */
    public String groupPage(Long plate_id, int page, int size, Map<String, String> params) throws Exception {
        String sql = "select tc.id,tc.name,tc.thumb_url,tc.status,tc.created_at ";
        String sql_ex = "from t_category tc " +
                "where tc.valid = 1 and tc.parent_id = ? and type = ? ";
        List<Object> lp = new ArrayList<>();
        lp.add(plate_id);
        lp.add("group");
        return commonPage(plate_id, sql, sql_ex, page, size, lp, params);
    }


    public String commonPage(Long pid, String sql, String sql_ex, int page, int size, List<Object> lp, Map<String, String> params) throws Exception {
        StringBuilder cacheKey = new StringBuilder();
        cacheKey.append(pid).append(page).append("_").append(size);
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                cacheKey.append("_").append(entry.getKey()).append("_").append(entry.getValue());
                String key = entry.getKey();
                String value = entry.getValue();
                logger.info("param->key:{},value:{}", key, value);
                if ("name".equals(key)) {
                    sql_ex += " and tc.name like ? ";
                    lp.add("%" + value + "%");
                } else {
                    sql_ex += " and tc." + key + " = ? ";
                    lp.add(value);
                }
            }
        }
        sql_ex += " order by tc.idx desc";
        return getPageByCache(REDIS_CATEGORY, cacheKey.toString(), lp, page, size, sql, sql_ex);
    }

    /**
     * 新增圈子时，增加板块的下级数。
     *
     * @param category
     * @param step
     */
    protected void childIncr(Category category, int step) {
        //表明是在新增圈子，给板块增加一个圈子数
        if (category.get("type").equals("group")) {
            CategoryStat.dao.childIncr(category.getLong("parent_id"), step);
        }
    }

    /**
     * 保存板块/圈子
     *
     * @param record
     */
    @Before(Tx.class)
    public void saveCategory(Category record) {
        boolean flag = saveEntity(record, REDIS_CATEGORY);
        //缓存
        if (flag) {
            //保存对应的stat
            CategoryStat.dao.saveStat(record.getLong("id"));
            childIncr(record, 1);
        }
    }

    /**
     * 编辑板块/圈子
     *
     * @param record
     */
    @Before(Tx.class)
    public boolean updateCategory(Category record) {
        boolean flag = updateEntity(record, REDIS_CATEGORY);
        return flag;
    }


    /**
     * 删除板块/圈子
     *
     * @param record
     */
    @Before(Tx.class)
    public void deleteCategory(Category record) {
        boolean flag = deleteEntity(record, REDIS_CATEGORY);
        if (flag) {
            childIncr(record, -1);
        }
    }

    /**
     * @param num     取随机num个圈子
     * @param user_id
     */
    public List<Record> randomGroup(int num, String user_id) {
        //查询圈子

        List<Record> groups = Db.find("select id,name,thumb_url from t_category " +
                "where valid = 1 and type = 'group' and status = 1");
        if (StringUtils.isNotEmpty(user_id)) {
            groups = Db.find("select tc.id,tc.name,tc.thumb_url " +
                    "from  t_category tc " +
                    "where tc.valid = 1 and tc.type='group'  and tc.status = 1 " +
                    "and tc.id not in(select group_id from t_user_follow WHERE user_id = ?)", user_id);
        }

        if (CollectionUtils.isEmpty(groups)) {
            return null;
        }
        if (groups.size() <= num) {
            return groups;
        }
        logger.info("size:{}", groups.size());
        List<Record> rt = new ArrayList<>();
        Set<Integer> sets = new HashSet<>();
        while (sets.size() < num) {
            int i = new Random().nextInt(groups.size());
            sets.add(i);
        }
        for (int i : sets) {
            rt.add(groups.get(i));
        }
        return rt;
    }

    /**
     * 获取统计
     *
     * @param id
     */
    public Record getStat(long id) {

        Record stat = Db.findFirst("select * from t_category_stat where category_id = ? ", id);
        return stat;
    }

    /**
     * 查询用户下的圈子
     *
     * @param user_id
     */
    public List<Record> getByUser(String user_id) {

        String sql = "select tc.id,tc.name,tc.thumb_url from t_user_follow tf " +
                "left join t_category tc on tf.group_id = tc.id " +
                "where tc.parent_id > 0 and tc.valid = 1 and tc.status = 1 and tf.user_id = ? " +
                "order by tf.created_at desc ";
        return Db.find(sql, user_id);
    }

    /**
     * 获取所有的圈子。包含板块
     *
     * @param user_id
     */
    public List<Record> all(String user_id) {
        //板块
        List<Record> plates = Db.find("select id,name from t_category " +
                "where valid = 1 " +
                "and status = 1 " +
                "and type ='plate' " +
                "order by idx desc");
        String sql = "select id,name,thumb_url from t_category " +
                "where valid = 1 " +
                "and status = 1 " +
                "and parent_id = ? " +
                "order by idx desc";
        if (CollectionUtils.isNotEmpty(plates)) {
            for (Record r : plates) {
                List<Record> groups = Db.find(sql, r.get("id"));
                if (StringUtils.isNotEmpty(user_id)) {
                    if (CollectionUtils.isNotEmpty(groups)) {
                        for (Record g : groups) {
                            Record rk = Db.findFirst("select * from t_user_follow where user_id =? and group_id = ?", user_id, g.getLong("id"));
                            if (rk != null) {
                                g.set("followed", true);
                            }
                        }
                    }
                }
                r.set("groups", groups);
            }
        }
        return plates;
    }

    /**
     * 通过名称匹配圈子
     *
     * @param name
     * @return
     */
    public Page<Record> getListByName(int page, int size, String name, String user_id) {
        String sql = "select id,name,thumb_url ";
        String sql_ex = "from t_category " +
                "where valid = 1 and type ='group' and name like ? order by idx desc, name asc";
        Page<Record> pg = Db.paginate(page, size, sql, sql_ex, "%" + name + "%");
        if (CollectionUtils.isNotEmpty(pg.getList()) && StringUtils.isNotEmpty(user_id)) {
            for (Record r : pg.getList()) {
                Record rk = Db.findFirst("select * from t_user_follow where user_id =? and group_id = ?", user_id, r.getLong("id"));
                if (rk != null) {
                    r.set("followed", true);
                }
            }
        }
        return pg;
    }

    @Override
    protected void clearCache() {
        super.clearCache();
        Cache cache = Redis.use();
        cache.del(REDIS_CATEGORY + REDIS_PAGE);
    }
}
