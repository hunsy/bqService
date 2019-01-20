package com.bingqiong.bq.model;

import com.bingqiong.bq.constant.BqConstants;
import com.bingqiong.bq.model.base.BaseComment;
import com.bingqiong.bq.utils.IKAnalyzerUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Redis;
import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 评论，回复。
 * <p>
 * Created by hunsy on 2017/4/10.
 */
public class Comment extends BaseComment {

    /**
     *
     */
    private static final long serialVersionUID = 1135602343470203655L;
    private Logger logger = LoggerFactory.getLogger(Comment.class);
    public static Comment dao = new Comment();
    private static final String REDIS_COMMENT = BqConstants.BQ_APPLICATION + "comment:";

    /**
     * @param id
     * @return
     */
    public Comment getById(Long id) throws InstantiationException, IllegalAccessException {
        Comment record = getByIdByCache(REDIS_COMMENT, id, Comment.class);
//        Record record = Db.findFirst("select * from t_comment where id = ? and valid = 1", id);
        return record;
    }

    /**
     * 获取评论分页
     *
     * @param page
     * @param size
     * @param params
     * @return
     */
    public String findPage(int page, int size, Map<String, String> params) {
        String sql = "select tm.id,ta.title,tm.content,tm.created_at,tm.user_id,tm.user_name,tm.user_avatar,tm.factory_name,tm.device_model ";
        String sql_ex = " from t_comment tm  " +
                "left join t_article ta on tm.article_id = ta.id " +
                "where tm.valid = 1 ";
        List<Object> lp = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        field.append(page).append("_").append(size);
        if (params != null && !params.isEmpty()) {

            if (params.get("article_id") != null) {
                sql_ex += " and tm.article_id = ? ";
                lp.add(params.get("article_id"));
                field.append("article_id").append("_").append(params.get("article_id"));
            }

            if (params.get("title") != null) {
                sql_ex += " and ta.title like ? ";
                lp.add("%" + params.get("title") + "%");
                field.append("title").append("_").append(params.get("title"));
            }

            if (params.get("content") != null) {
                sql_ex += " and tm.content like ? ";
                lp.add("%" + params.get("content") + "%");
                field.append("content").append("_").append(params.get("content"));
            }
            /**
             * 开始，结束时间
             */
            if (params.get("start_time") != null && params.get("end_time") != null) {
                sql_ex += " and tm.created_at between ? and ? ";
                lp.add(params.get("start_time"));
                lp.add(params.get("end_time"));
                field.append("start_time").append("_").append(params.get("start_time"));
                field.append("end_time").append("_").append(params.get("end_time"));
            } else if (params.get("start_time") != null && params.get("end_time") == null) {
                sql_ex += " and tm.created_at > ? ";
                lp.add(params.get("start_time"));
                field.append("start_time").append("_").append(params.get("start_time"));
            } else if (params.get("start_time") == null && params.get("end_time") != null) {
                sql_ex += " and tm.created_at < ? ";
                lp.add(params.get("end_time"));
                field.append("end_time").append("_").append(params.get("end_time"));
            }
        }
        sql_ex += " order by tm.created_at desc ";
        return getPageByCache(REDIS_COMMENT, field.toString(), lp, page, size, sql, sql_ex);
    }

    /**
     * 获取前端的分页
     *
     * @param page
     * @param size
     * @return
     */
    public Page<Record> findApiPage(int page, int size, String article_id, String user_id) throws IOException {
        String sql = "select tm.id,tm.content,tm.created_at,tm.user_id,tm.user_name,tm.user_avatar," +
                "tm.reply_user_id,tm.reply_user_name,tm.reply_user_avatar, " +
                "tcs.children_num,tcs.praise_num,tm.user_avatar ";
        //第一级评论
        String sql_ex = " from t_comment tm  " +
                "left join t_comment_stat tcs on tcs.comment_id = tm.id " +
                "where tm.valid = 1 and tm.article_id = ? and tm.parent_id is null order by tm.created_at desc";

        Page<Record> rs = Db.paginate(page, size, sql, sql_ex, article_id);
        //下级回复，选取2个
        if (rs.getList() != null && rs.getList().size() > 0) {
            for (Record re : rs.getList()) {
                if (StringUtils.isNotEmpty(user_id)) {
                    //是否点赞
                    re.set("praised", CommentPraiseRec.dao.praiseed(re.getLong("id"), user_id));
                } else {
                    //是否点赞
                    re.set("praised", false);
                }

                //敏感过滤
                re.set("content", hasSensitive(re.getStr("content")));
//                Record child = new Record();
//                Long count = Db.queryLong("select count(id) from t_comment where valid = 1 and parent_id = ?", re.getLong("id"));
//                child.set("count", count);
//                if (count > 0) {
                List<Record> children = Db.find("select id,content,user_id,user_name,user_avatar,reply_user_id,reply_user_name,reply_user_avatar " +
                        "from t_comment " +
                        "where valid = 1 and parent_id = ? order by created_at desc limit 0,2 ", re.getLong("id"));
                if (children != null && children.size() > 0) {
                    for (Record r : children) {
                        r.set("content", hasSensitive(r.getStr("content")));
                    }
                }
//                child.set("list", children);
//                }
                re.set("children", children);
            }
        }
        return rs;
    }


    /**
     * 保存
     *
     * @return
     */
    @Before(Tx.class)
    public boolean saveComment(Comment record) throws Exception {

        boolean flag = saveEntity(record, REDIS_COMMENT);
        if (flag) {
            ArticleStat.dao.commentIncr(Long.parseLong(record.get("article_id").toString()), 1);


            String parent_id = record.get("parent_id");
            if (parent_id == null) {
                CommentStat.dao.saveStat(record.getLong("id"));
            }

            if (parent_id != null) {
                Long pid = Long.parseLong(parent_id);
                CommentStat.dao.childIncr(pid, 1);
            }
        }
        return flag;
    }

    @Before(Tx.class)
    public void deleteComment(Comment record) throws Exception {

        boolean flag = deleteEntity(record, REDIS_COMMENT);
        if (flag) {
            ArticleStat.dao.commentIncr(Long.parseLong(record.get("article_id").toString()), -1);
            if (record.get("parent_id") != null) {
                CommentStat.dao.childIncr(record.getLong("id"), -1);
            }
        }
    }

    @Override
    protected void clearCache() {
        Redis.use().del(REDIS_COMMENT + REDIS_PAGE);
    }

    /**
     * 获取下级
     *
     * @param
     */
    public Page<Record> childPage(int page, int size, Long pid) throws IOException {
        String sql = "select id,article_id,content,user_id,user_name,user_avatar," +
                "reply_user_id,reply_user_name,reply_user_avatar,created_at ";
        String sql_ex = "from t_comment where valid = 1 and parent_id = ? order by created_at desc";

        Page<Record> rs = Db.paginate(page, size, sql, sql_ex, pid.toString());
        if (rs.getList() != null && rs.getList().size() > 0) {
            for (Record re : rs.getList()) {
                //敏感过滤
                re.set("content", hasSensitive(re.getStr("content")));
            }
        }
        return rs;
    }

    public List<Record> getChildren(Long pid) {

        String sql = "select tm.id," +
                "tm.content,tm.created_at,tm.user_id,tm.user_name,tm. " +
                " " +
                "from t_comment tm  " +
                "left join t_article ta on tm.article_id = ta.id " +
                "where tm.valid = 1 " +
                "and tm.parent_id = ? order by tm.created_at desc";
        return Db.find(sql, pid);
    }

    /**
     * 检验是否含有敏感词
     *
     * @param str
     * @return
     * @throws IOException
     */
    public String hasSensitive(String str) throws IOException {
        List<String> ls = IKAnalyzerUtil.getInstance().analyzer(str);
        StringBuilder sb = new StringBuilder();
        for (String l : ls) {
            logger.info("分词:->{}", l);
            if (Redis.use().sismember(BqConstants.REDIS_SENSITIVE_KEY, l)) {
//                StringUtils.replace(str, l, "***");
                sb.append("***");
                logger.info("存在敏感词:{}", l);
            } else {
                sb.append(l);
            }
        }
        logger.info("替换后的:{}", sb.toString());
        return EmojiParser.parseToUnicode(sb.toString());
    }

    /**
     * 用户下的回复
     *
     * @param page
     * @param size
     * @param user_id
     * @return
     */
    public Page<Record> myPage(int page, int size, String user_id) throws IOException {
        String sql = "select tmr.id,tmr.article_id,tmr.content as reply_content,tmr.user_id,tmr.user_name,tmr.user_avatar," +
                "tmr.reply_user_id,tmr.reply_user_name,tmr.reply_user_avatar,tm.content,tmr.created_at," +
                "tmr.group_id,tc.name as category_name ";

        String sql_ex = "from t_comment tmr " +
                "left join t_comment tm on tmr.fparent_id = tm.id " +
                "left join t_category tc on tc.id = tmr.group_id " +
                "where tm.valid = 1 and tmr.valid = 1 and tmr.reply_user_id = ? order by tmr.created_at desc";
        Page<Record> rs = Db.paginate(page, size, sql, sql_ex, user_id);
        if (rs.getList() != null && rs.getList().size() > 0) {
            for (Record re : rs.getList()) {
                //敏感过滤
                re.set("content", hasSensitive(re.getStr("content")));
            }
        }
        return rs;
    }
}
