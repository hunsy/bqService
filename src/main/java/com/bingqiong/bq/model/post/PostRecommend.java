package com.bingqiong.bq.model.post;

import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.utils.MDateKit;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.BaseModel;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 首页推荐的帖子
 * <p>
 * Created by hunsy on 2017/6/25.
 */
public class PostRecommend extends BaseModel<PostRecommend> {


    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final PostRecommend dao = new PostRecommend();
    public static final String TABLE_POST_RECOMMEND = "t_post_recommend";

    /**
     * 新增推荐
     *
     * @param post_id 推荐的帖子的id
     * @return 返回保存结果
     */
    @Before(Tx.class)
    public boolean saveRecommend(Long post_id) throws BizException {

        Post post = Post.dao.findById(post_id);
        if (post == null) {
            logger.error("帖子不存在->post_id:{}", post_id);
            throw new BizException(ErrorCode.POST_NOT_EXIST);
        }

        PostRecommend dbRecommend = findByPostId(post_id);
        if (dbRecommend != null) {
            throw new BizException(ErrorCode.POST_RECOMMEND_EXIST);
        }

        PostRecommend postRecommend = new PostRecommend();
        postRecommend.set("post_id", post_id);
        postRecommend.set("created_at", MDateKit.getNow());
        return postRecommend.save();
    }

    /**
     * 通过帖子id查询是否存在推荐记录
     *
     * @param post_id 帖子id
     * @return 返回查询记录
     */
    private PostRecommend findByPostId(Long post_id) {

        return findFirst("select * from t_post_recommend where post_id = ?", post_id);
    }

    /**
     * 更新（主要是跟新排序）
     *
     * @param recommend 推荐记录
     * @return 更新结果
     */
    @Before(Tx.class)
    public boolean updateRecommend(PostRecommend recommend) throws BizException {

        if (recommend == null) {
            logger.error("推荐帖子不存在");
            throw new BizException(ErrorCode.POST_RECOMMEND_NOT_EXIST);
        }
        return recommend.update();
    }

    /**
     * 删除推荐
     *
     * @param id 推荐id
     * @return 删除结果
     */
    @Before(Tx.class)
    public boolean deleteRecommend(Long id) throws BizException {

        PostRecommend recommend = findById(id);
        if (recommend == null) {
            logger.error("推荐贴不存在->id:{}", id);
            throw new BizException(ErrorCode.POST_RECOMMEND_NOT_EXIST);
        }
        return recommend.delete();
    }

    /**
     * 分页查询
     *
     * @param pageRequest 请求参数
     * @return 返回分页数据
     */
    public Page<Record> findPage(PageRequest pageRequest, String user_id) throws IOException {

        String sql = "select tp.id,tp.title,tp.intro,tp.thumb_url,tp.content," +
                "tp.status,tp.created_at,tp.likes,tp.comments," +
                "tp.user_id,tu.user_name,tu.avatar_url," +
                "tp.group_id,tg.name as group_name,tg.thumb_url as group_icon," +
                "tp.is_sys,tp.type_id,ty.name as type_name  ";
        String sql_ex = "from t_post_recommend tpr " +
                "left join t_post tp on tpr.post_id = tp.id " +
                "left join t_user tu on tp.user_id = tu.user_id " +
                "left join t_group tg on tp.group_id = tg.id " +
                "left join (select * FROM t_post_type WHERE valid = 1 and status = 1) as ty on tp.type_id = ty.id " +
                "where tp.valid = 1 and tg.valid = 1 and tp.status = 1 ";

        Map<String, String> params = pageRequest.getParams();
        List<Object> lp = new ArrayList<>();
        if (StringUtils.isNotEmpty(params.get("title"))) {
            sql_ex += " and tp.title like ? ";
            lp.add("%" + params.get("title") + "%");
        }

        if (StringUtils.isNotEmpty(params.get("ios_show"))) {
            sql_ex += " and tp.ios_show = ? ";
            lp.add(params.get("ios_show"));
        }

        if (StringUtils.isNotEmpty(params.get("android_show"))) {
            sql_ex += " and tp.android_show = ? ";
            lp.add(params.get("android_show"));
        }
        sql_ex += " order by tpr.idx desc,tpr.created_at desc";

        Page<Record> page = Db.paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, lp.toArray());
        Post.dao.parsePageList(page.getList(), user_id);
        return page;
    }

    /**
     * @param pageRequest 请求参数
     * @return 返回后端前台页面数据
     */
    public Page<Record> findPageAdmin(PageRequest pageRequest) {

        String sql = "select tpr.id,tp.id as post_id,tp.title,tp.intro,tp.thumb_url,tp.content," +
                "tp.status,tp.created_at,tp.likes,tp.comments," +
                "tp.user_id,tu.user_name,tu.avatar_url," +
                "tp.group_id,tg.name as group_name,tg.thumb_url as group_icon," +
                "ty.name as type_name,tp.is_sys,tp.ios_show,tp.android_show  ";
        String sql_ex = "from t_post_recommend tpr " +
                "left join t_post tp on tpr.post_id = tp.id " +
                "left join t_user tu on tp.user_id = tu.user_id " +
                "left join t_group tg on tp.group_id = tg.id " +
                "left join t_post_type ty on tp.type_id = ty.id " +
                "where tp.valid = 1  "
                /*"and tg.valid = 1"*/;

        Map<String, String> params = pageRequest.getParams();
        List<Object> lp = new ArrayList<>();
        if (StringUtils.isNotEmpty(params.get("title"))) {
            sql_ex += " and tp.title like ? ";
            lp.add("%" + params.get("title") + "%");
        }
        if (StringUtils.isNotEmpty(params.get("ios_show"))) {
            sql_ex += " and tp.ios_show = ? ";
            lp.add(params.get("ios_show"));
        }

        if (StringUtils.isNotEmpty(params.get("android_show"))) {
            sql_ex += " and tp.android_show = ? ";
            lp.add(params.get("android_show"));
        }
        sql_ex += " order by tpr.idx desc,tpr.created_at desc";

        Page<Record> page = Db.paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, lp.toArray());
        Post.dao.parseAdminPage(page);
        return page;
    }
}
