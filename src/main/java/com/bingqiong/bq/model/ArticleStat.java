package com.bingqiong.bq.model;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文章/帖子的统计
 * Created by hunsy on 2017/4/24.
 */
@SuppressWarnings({"serial"})
public class ArticleStat extends Model<ArticleStat> {
    private Logger logger = LoggerFactory.getLogger(ArticleStat.class);
    public static ArticleStat dao = new ArticleStat();


    /**
     * @param article_id
     * @return
     */
    public Record getByArticle(Long article_id) {
        Record record = new Record();
        Cache cache = Redis.use();
        record.set("comment_num", cache.zscore(Article.REDIS_COMMENT_NUM, article_id.toString()));
        record.set("praise_num", cache.zscore(Article.REDIS_PRAISE_NUM, article_id.toString()));
        return record;
    }

    /**
     * 保存文章对应的统计表。
     *
     * @param article_id
     */
    public void saveStat(Long article_id) {
        boolean flag = new ArticleStat()
                .set("article_id", article_id)
                .save();
        if (flag) {
            logger.info("保存article_stat成功，article_id:{}", article_id);
            Redis.use().zadd(Article.REDIS_PRAISE_NUM, 0, article_id);
            Redis.use().zadd(Article.REDIS_COMMENT_NUM, 0, article_id);
        }
    }

    /**
     * 点赞+-1
     *
     * @param article_id
     * @param step
     */
    public boolean praiseIncr(Long article_id, String user_id, int step) {
        ArticleStat articleStat = ArticleStat.dao.findFirst("select * from t_article_stat where article_id = ?", article_id);
        boolean flag = false;
        if (articleStat != null) {
            logger.info("文章|帖子->{},点赞数{}", article_id, step);
            flag = articleStat.set("praise_num", articleStat.getInt("praise_num") + step).update();
            if (step > 0) {
                //增加用户点赞记录
                Record r = new Record()
                        .set("user_id", user_id)
                        .set("article_id", article_id);
                Db.save("t_article_praise_rec", r);
            } else {
                //删除点赞记录
                Db.update("delete from t_article_praise_rec where article_id = ? and user_id = ?", article_id, user_id);
            }
            if (flag) {
                Article.dao.clearCache();
                Redis.use().zincrby(Article.REDIS_PRAISE_NUM, step, article_id.toString());
            }
        }
        return flag;
    }

    /**
     * 评论+-1
     *
     * @param article_id
     * @param step
     */
    public boolean commentIncr(Long article_id, int step) {
        ArticleStat articleStat = ArticleStat.dao.findFirst("select * from t_article_stat where article_id = ?", article_id);
        boolean flag = false;
        if (articleStat != null) {
            logger.info("文章|帖子->{},评论数{}", article_id, step);
            flag = articleStat.set("comment_num", articleStat.getInt("comment_num") + step).update();
            if (flag) {
                Redis.use().zincrby(Article.REDIS_COMMENT_NUM, step, article_id.toString());
            }
        }
        if (flag){
            Article.dao.clearCache();
        }
        return flag;
    }

}
