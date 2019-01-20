package com.bingqiong.bq.model;

import com.bingqiong.bq.constant.BqConstants;
import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.model.base.BaseArticle;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 文章/帖子
 * <p>
 * Created by hunsy on 2017/4/7.
 */
public class Article extends BaseArticle {
    /**
     *
     */
    private static final long serialVersionUID = -2373861385950537401L;
    public static Article dao = new Article();
    /**
     * 保存id的set的key
     */
    public static final String REDIS_ARTICLE = BqConstants.BQ_APPLICATION + "article:";
    public static final String REDIS_COMMENT_NUM = REDIS_ARTICLE + "comment_num";
    public static final String REDIS_PRAISE_NUM = REDIS_ARTICLE + "praise_num";
    public static final String REDIS_POST_TOPS = REDIS_ARTICLE + "tops:";

    /**
     * 检查文章
     *
     * @param record
     * @param type
     * @throws BizException
     */
    public void checkArticle(Article record, String type) throws BizException {
        if (record == null || !record.getStr("type").equals(type)) {
            logger.error("对象不存在|对象类型不正确,obj:{},type:{}", getClass().getName(), type);
            throw new BizException(BqErrorCode.CODE_FAILED.getCode());
        }
    }


    /**
     * 获取文章/帖子详情
     *
     * @param id
     * @return
     */
    public Article getById(Long id) throws InstantiationException, IllegalAccessException {
        return getByIdByCache(REDIS_ARTICLE, id, Article.class);
    }

    /**
     * 获取文章分页。
     *
     * @return
     */
    public String articlePage(boolean isAdmin, int page, int size, Map<String, String> params, String user_id) throws Exception {
        String sql = "select ta.id,ta.title,ta.author_avatar,ta.intro,ta.article_type,ta.thumb_url,ta.author," +
                "tat.praise_num,tat.comment_num,ta.created_at,ta.status  ";
        if (isAdmin) {
            sql += ",ta.content ";
        }
        String sql_ex = "from t_article ta " +
                "left join t_article_stat tat on ta.id = tat.article_id " +
                "where ta.valid = 1 and ta.type = ? ";
        List<Object> lp = new ArrayList<>();
        lp.add("article");
        return commonPage(isAdmin, "article", sql, sql_ex, page, size, lp, params, user_id);
    }

    /**
     * 获取帖子分页。
     *
     * @return
     */
    public String postPage(boolean isAdmin, Long group_id, int page, int size, Map<String, String> params, String user_id) throws Exception {

        String sql = "select ta.id,ta.title,ta.author_avatar,ta.intro,ta.thumb_url,ta.author,ta.status,ta.created_at,tat.praise_num,tat.comment_num," +
                "ta.is_top ";
        if (isAdmin) {
            sql += ",ta.content ";
        }
        String sql_ex = "from t_article ta " +
                "left join t_article_stat tat on ta.id = tat.article_id " +
                "where ta.valid = 1 and ta.type = ? and ta.group_id = ? ";
        List<Object> lp = new ArrayList<>();
        lp.add("post");
        lp.add(group_id);
        return commonPage(isAdmin, "post" + group_id, sql, sql_ex, page, size, lp, params, user_id);
    }

    public String commonPage(boolean isAdmin, String type, String sql, String sql_ex, int page, int size, List<Object> lp, Map<String, String> params, String user_id) throws Exception {

        StringBuilder cacheKey = new StringBuilder();
        cacheKey.append(isAdmin).append("_").append(type).append("_").append(page).append("_").append(size);
        boolean ishot = false;
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                cacheKey.append("_").append(entry.getKey()).append("_").append(entry.getValue());
                String key = entry.getKey();
                String value = entry.getValue();
                logger.info("param->key:{},value:{}", key, value);
                if ("title".equals(key)) {
                    sql_ex += " and ta.title like ? ";
                    lp.add("%" + value + "%");
                } else {
                    sql_ex += " and ta." + key + " = ? ";
                    lp.add(value);
                }
                if (key.equals("is_hot") && Integer.parseInt(value) == 1) {
                    ishot = true;
                }
            }
        }
        if (ishot) {
            sql_ex += " order by ta.hot_idx desc,ta.created_at desc";
        } else if (type.startsWith("post")) {
            sql_ex += " order by ta.is_top desc,ta.idx desc,ta.created_at desc";
        } else {
            sql_ex += " order by ta.idx desc,ta.created_at desc";
        }
        return getPageByCache(isAdmin, REDIS_ARTICLE, cacheKey.toString(), lp, page, size, sql, sql_ex, user_id);
    }

    protected String getPageByCache(boolean isAdmin, String prefix, String field, List<Object> lp, int page, int size, String sql, String sql_ex, String user_id) {
        logger.info("\n");
        String cacheKey = prefix + REDIS_PAGE;
        logger.info("cache key:{},field:{}", cacheKey, field);
        Cache cache = Redis.use();
        String str = cache.hget(cacheKey, field);
        logger.info("cache:{}", str);
        if (StringUtils.isEmpty(str)) {
            Page<Record> ps;
            if (lp.isEmpty()) {
                ps = Db.paginate(page, size, sql, sql_ex);
            } else {
                ps = Db.paginate(page, size, sql, sql_ex, lp.toArray());
            }

            if (ps.getList() != null && ps.getList().size() > 0) {
                if (!isAdmin) {

                    for (Record r : ps.getList()) {

                        if (StringUtils.isNotEmpty(user_id)) {
                            Record pr = Db.findFirst("select * from t_article_praise_rec where article_id = ? and user_id = ? ", r.getLong("id"), user_id);
                            if (pr != null) {
                                r.set("praised", true);
                            }
                        }
                        String thumb_url = r.getStr("thumb_url");
                        r.set("thumb_url", parseThumbUrl(thumb_url));
                    }
                }

            }
            str = JsonKit.toJson(ps);
            if (StringUtils.isNotEmpty(str)) {
                cache.hset(cacheKey, field, str);
                //缓存1~2分钟
                cache.expire(cacheKey, getExpire(60));
            }
        }
        return str;
    }

    public String[] parseThumbUrl(String thumb_url) {

        if (StringUtils.isNotEmpty(thumb_url)) {
            String[] thumbs = thumb_url.split(";");
            logger.info("thumbs_size:{}", thumbs.length);
            if (thumbs.length > 3) {
                thumbs = (String[]) Arrays.asList(thumbs).subList(0, 3).toArray();
            }
            for (int j = 0; j < thumbs.length; j++) {
                String thumb = thumbs[j];
//                thumb = thumb.substring(0, thumb.lastIndexOf("/") + 1) + "thumb_" + thumb.substring(thumb.lastIndexOf("/") + 1);
                thumbs[j] = thumb;
            }
            return thumbs;
        } else {
            return new String[]{};
        }
    }

    /**
     * 增加减少圈子下的帖子数
     */
    private void incrGroupChild(Article record, int step) {
        //是帖子，则增加圈子下的帖子数 +1
        if ("post".equals(record.getStr("type"))) {
            CategoryStat.dao.childIncr(Long.parseLong(record.get("group_id").toString()), step);
            logger.info("圈子下帖子数->step:{},group_id:{}", step, record.get("group_id"));
        }
    }

    /**
     * 保存
     *
     * @param record
     */
    @Before(Tx.class)
    public void saveArticle(Article record) {
        boolean flag = saveEntity(record, REDIS_ARTICLE);
        //缓存
        if (flag) {
            //保存文章统计记录
            ArticleStat.dao.saveStat(record.getLong("id"));
            incrGroupChild(record, 1);
        }
    }

    /**
     * 更新
     *
     * @param record
     */
    @Before(Tx.class)
    public boolean updateArticle(Article record) {

        return updateEntity(record, REDIS_ARTICLE);
    }

    /**
     * 逻辑删除
     *
     * @param record
     */
    @Before(Tx.class)
    public void deleteArticle(Article record) {

        boolean flag = deleteEntity(record, REDIS_ARTICLE);
        //清缓存
        if (flag) {
            incrGroupChild(record, -1);
        }
    }

    /**
     * 获取置顶帖子
     *
     * @param group_id
     * @return
     */
    public String getTops(Long group_id) {

        Cache cache = Redis.use();
        String cacheStr = cache.hget(REDIS_POST_TOPS, group_id);
        logger.info("cache key:{},field:{},value:{}", REDIS_POST_TOPS, group_id, cacheStr);
        if (StringUtils.isEmpty(cacheStr)) {
            List<Record> tops = Db.find("select id,title from t_article " +
                    "where valid = 1 " +
                    "and type = 'post' " +
                    "and status = 1 " +
                    "and is_top = 1 " +
                    "and group_id= ? " +
                    "order  by idx desc,created_at desc", group_id);
            if (tops != null) {
                cacheStr = JsonKit.toJson(tops);
                cache.hset(REDIS_POST_TOPS, group_id, cacheStr);
            }
        }
        return cacheStr;
    }

    @Override
    protected void clearCache() {
        super.clearCache();
        //清除分页中的数据
        Cache cache = Redis.use();
        cache.del(REDIS_ARTICLE + REDIS_PAGE);
        cache.del(REDIS_POST_TOPS);
    }

    /**
     * 初始化文章。
     */
    public void init() {

        List<Long> ll = Db.query("select id from t_article where valid = 1");
        if (ll != null && ll.size() > 0) {
            Cache cache = Redis.use();
            for (Long id : ll) {
                logger.info("id:{}", id);
                String sid = id.toString();
                ArticleStat stat = ArticleStat.dao.findFirst("select * from t_article_stat where article_id = ? ", sid);
                if (stat != null) {
                    Redis.use().zadd(Article.REDIS_PRAISE_NUM, stat.getInt("praise_num"), sid);
                    Redis.use().zadd(Article.REDIS_COMMENT_NUM, stat.getInt("comment_num"), sid);
                } else {
                    Redis.use().zadd(Article.REDIS_PRAISE_NUM, 0, sid);
                    Redis.use().zadd(Article.REDIS_COMMENT_NUM, 0, sid);
                }
                cache.sadd(REDIS_ARTICLE + REDIS_ID, sid);
            }
        }
    }


}
