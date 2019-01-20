package com.bingqiong.bq.model;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 圈子|板块的统计信息。
 * <p>
 * Created by hunsy on 2017/4/25.
 */
public class CategoryStat extends Model<CategoryStat> {

    /**
     *
     */
    private static final long serialVersionUID = 628208726748211240L;
    private Logger logger = LoggerFactory.getLogger(CategoryStat.class);
    public static CategoryStat dao = new CategoryStat();

    /**
     * 保存分类对应的统计信息。
     *
     * @param category_id
     */
    public void saveStat(Long category_id) {
        boolean flag = new CategoryStat().set("category_id", category_id).save();
        if (flag) {
            logger.info("保存category_stat成功，category_id:{}", category_id);
        }
    }

    /**
     * 下级数+1。
     * 板块->增加圈子时+1
     * 圈子->增加帖子时+1
     * 下级数-1。
     * 板块->删除圈子时-1
     * 圈子->删除帖子时-1
     *
     * @param category_id 分类id
     * @param step        步进 +1 -1
     */
    public void childIncr(Long category_id, int step) {
        Db.update("update t_category_stat " +
                        "set children_num = (children_num + ?) " +
                        "where category_id = ?",
                step, category_id);
    }

}
