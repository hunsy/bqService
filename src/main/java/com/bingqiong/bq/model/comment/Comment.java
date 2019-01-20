package com.bingqiong.bq.model.comment;

import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.utils.JpushUtil;
import com.bingqiong.bq.comm.utils.MDateKit;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.BaseModel;
import com.bingqiong.bq.model.comm.Sensitive;
import com.bingqiong.bq.model.msg.MsgReadAt;
import com.bingqiong.bq.model.post.Post;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 评论
 * Created by hunsy on 2017/6/25.
 */
public class Comment extends BaseModel<Comment> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static Comment dao = new Comment();
    public static String TABLE_COMMENT = "t_comment";

    private static final int READ_TYPE = 1;

    /**
     * 保存评论
     *
     * @param comment 评论记录
     * @return 返回评论保存结果
     */
    @Before(Tx.class)
    public boolean saveComment(Comment comment) throws BizException {

        if (comment.getLong("post_id") == null) {
            logger.error("缺少参数post_id");
            throw new BizException(ErrorCode.MISSING_PARM);
        }

        Post post = Post.dao.findById(comment.getLong("post_id"));
        if (post == null) {
            logger.error("评论的帖子不存在->id:{}", comment.getLong("post_id"));
            throw new BizException(ErrorCode.POST_NOT_EXIST);
        }

        if (comment.getLong("parent_id") != null) {
            Comment bereply = findById(comment.getLong("parent_id"));
            if (bereply == null) {
                logger.error("回复的评论不存在");
                throw new BizException(ErrorCode.COMMENT_NOT_EXIST);
            }
            Long bereply_pid = bereply.get("fparent_id");
            if (bereply_pid == null) {
                bereply_pid = bereply.getLong("id");
            }
            comment.set("fparent_id", bereply_pid);
        }

        if (StringUtils.isEmpty(comment.getStr("content"))) {
            logger.error("评论内容不能为空");
            throw new BizException(ErrorCode.COMMENT_CONTENT_NOT_NULL);
        }

        if (comment.getStr("content").length() > 200) {
            logger.error("评论内容太多");
            throw new BizException(ErrorCode.COMMENT_CONTENT_LENGTH_MORE);
        }

        if (comment.getStr("content").length() < 3) {
            throw new BizException(ErrorCode.COMMENT_CONTENT_LENGTH_LESS);
        }

        Date date = MDateKit.getNow();
        comment.set("created_at", date);
        comment.set("updated_at", date);
        boolean flag = comment.save();
        //更新帖子的评论数
        if (flag) {
            Post.dao.updateComments(comment.getLong("post_id"), 1);
            if (comment.getLong("fparent_id") != null) {
                updateReplies(comment.getLong("fparent_id"), 1);
            }

            if (comment.getLong("parent_id") != null) {
                Comment p = findById(comment.getLong("parent_id"));
                JpushUtil.getInstance().pushRep(comment.getLong("id").toString(), p.getStr("user_id"));
            }
        }
        return flag;
    }

    /**
     * 删除评论
     *
     * @param id
     * @return
     */
    public boolean deleteComment(Long id) throws BizException {
        if (id == null) {

            throw new BizException(ErrorCode.MISSING_PARM);
        }
        Comment comment = findById(id);

        boolean flag = deleteComment(comment);
        return flag;
    }

    /**
     * 删除评论
     *
     * @param comment
     * @return
     * @throws BizException
     */
    @Before(Tx.class)
    public boolean deleteComment(Comment comment) throws BizException {
        if (comment == null) {
            logger.error("评论不存在");
            throw new BizException(ErrorCode.COMMENT_NOT_EXIST);
        }
        comment.set("valid", 0);
        comment.set("updated_at", MDateKit.getNow());
        boolean flag = comment.update();
        //更新帖子的评论数
        if (flag) {
            Post post = Post.dao.findById(comment.getLong("post_id"));
            if (post != null) {
                Post.dao.updateComments(comment.getLong("post_id"), -1);
            }
            if (comment.getLong("fparent_id") != null) {
                updateReplies(comment.getLong("fparent_id"), -1);
            }
        }

        return flag;
    }


    @Override
    public Comment findById(Object idValue) {

        return findFirst("select * from t_comment where id = ? and valid = 1", idValue);
    }

    /**
     * 分页查询
     *
     * @param pageRequest
     * @return
     */
    public Page<Record> findAdminPage(PageRequest pageRequest) throws BizException {

        String sql = "select tc.id,tp.title,tc.content,tc.replies,tc.likes,tc.factory_name,tc.device_model," +
                "tu.user_id,tu.mobile,tu.user_name,tu.avatar_url,tc.created_at  ";

        String sql_ex = "FROM t_comment tc " +
                "LEFT JOIN t_user tu on tc.user_id = tu.user_id " +
                "left join t_post tp on tc.post_id = tp.id  " +
                "WHERE " +
                "tc.valid = 1 ";
        Map<String, String> params = pageRequest.getParams();
        List<String> ls = new ArrayList<>();
        if (StringUtils.isNotEmpty(params.get("start_time"))) {
            sql_ex += " and tc.created_at >= ? ";
            ls.add(params.get("start_time") + " 00:00:00");
        }

        if (StringUtils.isNotEmpty(params.get("end_time"))) {
            sql_ex += " and tc.created_at <= ? ";
            ls.add(params.get("end_time") + " 23:59:59");
        }

        if (StringUtils.isNotEmpty(params.get("title"))) {
            sql_ex += " and tp.title like ? ";
            ls.add("%" + params.get("title") + "%");
        }
        if (StringUtils.isNotEmpty(params.get("content"))) {
            sql_ex += " and tc.content like ? ";
            ls.add("%" + params.get("content") + "%");
        }

        sql_ex += " order by tc.created_at desc ";
        Page<Record> page = Db.paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, ls.toArray());
        return page;
    }

    /**
     * 分页查询
     *
     * @param pageRequest
     * @return
     */
    public Page<Record> findPage(PageRequest pageRequest, String user_id) throws BizException, IOException {

        String sql = "select tc.id,tc.content,tc.replies,tc.likes," +
                "tu.user_id,tu.user_name,tu.avatar_url,tc.created_at  ";

        String sql_ex = "FROM t_comment tc " +
                "LEFT JOIN t_user tu on tc.user_id = tu.user_id " +
                "WHERE " +
                "tc.valid = 1 and tc.parent_id is null and post_id = ? " +
                "order by tc.created_at desc ";

        if (pageRequest.getParams().get("post_id") == null) {

            throw new BizException(ErrorCode.MISSING_PARM);
        }

        Page<Record> page = Db.paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, pageRequest.getSimpleValues());
        //有内容
        if (CollectionUtils.isNotEmpty(page.getList())) {
            for (Record record : page.getList()) {
                //查询，是否赞过
                if (StringUtils.isNotEmpty(user_id)) {
                    CommentLike like = CommentLike.dao.findByUserIdAndComment(user_id, record.getLong("id"));
                    record.set("praised", like != null);
                }
                //查看下面两个数据
                List<Record> ls = findList(record.getLong("id"), true);
                if (CollectionUtils.isNotEmpty(ls)) {
                    record.set("children", ls);
                }
                record.set("content", Sensitive.dao.filterSensitive(record.getStr("content")));
                record.set("user_name", Sensitive.dao.filterSensitive(record.getStr("user_name")));
            }
        }
        return page;
    }


    /**
     * @param limit
     * @return
     */
    public List<Record> findList(Long pid, boolean limit) throws IOException {
        String sql = "SELECT tc.id,tc.content,tc.replies,tc.likes,tc.user_id,tu.user_name,tu.avatar_url," +
                "tuu.user_id as reply_id,tuu.user_name as reply_name,tuu.avatar_url as reply_avatar_url " +
                "FROM t_comment tc " +
                "LEFT JOIN t_comment tr on tc.parent_id = tr.id " +
                "LEFT JOIN t_user tu on tc.user_id = tu.user_id " +
                "LEFT JOIN t_user tuu on tr.user_id = tuu.user_id " +
                "WHERE tc.fparent_id = ? and tc.valid = 1 order by tc.created_at desc";
        List<Record> records;
        if (limit) {
            records = Db.find(sql + " limit 0,2", pid);
        } else {
            records = Db.find(sql, pid);
        }

        for (Record record : records) {
            record.set("content", Sensitive.dao.filterSensitive(record.getStr("content")));
            record.set("user_name", Sensitive.dao.filterSensitive(record.getStr("user_name")));
            record.set("reply_name", Sensitive.dao.filterSensitive(record.getStr("reply_name")));
        }
        return records;
    }

    /**
     * 获取我的回复列表
     *
     * @param pageRequest
     * @return
     */
    public Page<Record> findMyReplies(PageRequest pageRequest) throws IOException {

        String sql = "select tc.id,tc.post_id,tc.content as comment_content," +
                "tp.content as post_content,tc.created_at,tc.user_id,tu.user_name,tu.avatar_url ";
        String sql_ex = "from t_comment tc " +
                "left join t_post tp on tc.post_id = tp.id " +
                "left join t_user tu on tu.user_id = tc.user_id " +
                "where tc.valid = 1 and tc.user_id = ? order by tc.created_at desc";

        Page<Record> page = Db.paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, pageRequest.getSimpleValues());
        if (page.getList() != null) {
            List<Record> records = page.getList();
            for (Record record : records) {
                record.set("comment_content", Sensitive.dao.filterSensitive(record.getStr("comment_content")));
                if (record.getStr("post_content") != null){
                    String pc = Jsoup.parse(record.getStr("post_content")).text();
                    record.set("post_content", Sensitive.dao.filterSensitive(pc));
                }
                record.set("user_name", Sensitive.dao.filterSensitive(record.getStr("user_name")));
            }
        }
        return page;
    }


    /**
     * 获取当前用户的回复数
     *
     * @param user_id
     * @return
     */
    public Long findReplyCount(String user_id) {

        MsgReadAt readAt = MsgReadAt.dao.get(user_id, READ_TYPE);

        String sql = "select count(rep.id) from t_comment rep " +
                "left join t_comment tm on rep.parent_id = tm.id " +
                "where tm.user_id = ? and rep.valid = 1 and tm.valid = 1 ";
        if (readAt == null) {

            sql += " order by rep.created_at desc";
            return Db.queryLong(sql, user_id);
        } else {
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(readAt.getDate("read_at"));
            sql += " and  rep.created_at > ? order by  rep.created_at desc";
            return Db.queryLong(sql, user_id, date);
        }
    }

    /**
     * 回复我的
     *
     * @return
     */
    public Page<Record> replyMes(PageRequest pageRequest) throws IOException {

        String sql = "SELECT c.id,c.content," +
                "rep.id as reply_id,rep.content as reply_content,rep.created_at as reply_at," +
                "tu.user_id,tu.user_name,tu.avatar_url," +
                "rep.post_id,tp.group_id,tg.`name` as group_name ";

        String sql_ex = "FROM t_comment rep " +
                "LEFT JOIN t_comment c ON rep.parent_id = c.id " +
                "LEFT JOIN t_user tu ON tu.user_id = rep.user_id " +
                "LEFT JOIN t_post tp ON tp.id = rep.post_id " +
                "LEFT JOIN t_group tg ON tp.group_id = tg.id " +
                "WHERE c.valid = 1 AND rep.valid = 1 AND c.user_id = ? ORDER BY rep.created_at DESC ";
        //新增请求时间标记
        MsgReadAt.dao.createdReadAt(pageRequest.getParams().get("user_id").toString(), 1);
        Page<Record> page = Db.paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, pageRequest.getSimpleValues());

        if (page.getList() != null) {
            List<Record> records = page.getList();
            for (Record record : records) {
                record.set("content", Sensitive.dao.filterSensitive(record.getStr("content")));
                record.set("reply_content", Sensitive.dao.filterSensitive(record.getStr("reply_content")));
                record.set("user_name", Sensitive.dao.filterSensitive(record.getStr("user_name")));
            }
        }

        return page;
    }


    /**
     * 更新评论的点赞数
     *
     * @param comment_id 评论id
     * @param step       步长
     */
    public void updateLikes(Long comment_id, int step) throws BizException {

        Comment comment = findById(comment_id);
        if (comment == null) {
            throw new BizException(ErrorCode.COMMENT_NOT_EXIST);
        }
//        int num;
//        if (step < 0 && comment.getInt("likes") <= 0) {
//            num = 0;
//        } else {
//            int tmp = comment.getInt("likes") <= 0 ? 0 : comment.getInt("likes");
//            num = tmp + step;
//        }
        Long count = Db.queryLong("select count(id) from t_comment_likes where comment_id = ?", comment_id);
        comment.set("likes", count);
        comment.update();
    }

    /**
     * 更新回复数
     *
     * @return
     */
    public boolean updateReplies(Long pid, int step) throws BizException {
        Comment comment = findById(pid);
        if (comment != null) {
            Long count = Db.queryLong("select count(id) from t_comment where fparent_id = ? ", pid);
            comment.set("replies", count);
            return comment.update();
        }
//        int num;
//        if (step < 0 && comment.getInt("replies") <= 0) {
//            num = 0;
//        } else {
//            int tmp = comment.getInt("replies") <= 0 ? 0 : comment.getInt("replies");
//            num = tmp + step;
//        }

        return false;
    }
}
