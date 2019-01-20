package com.bingqiong.bq.model.post;

import com.bingqiong.bq.comm.constants.Constants;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.constants.EsIndexType;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.utils.EsUtils;
import com.bingqiong.bq.comm.utils.MDateKit;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.BaseModel;
import com.bingqiong.bq.model.category.Group;
import com.bingqiong.bq.model.category.Plate;
import com.bingqiong.bq.model.comm.Sensitive;
import com.bingqiong.bq.model.msg.PrivateMsg;
import com.bingqiong.bq.model.user.User;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Redis;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * 帖子
 * Created by hunsy on 2017/6/22.
 */
public class Post extends BaseModel<Post> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static Post dao = new Post();

    public static final String TABLE_POST = "t_post";

    /**
     * 新增帖子
     *
     * @param post 待新增的帖子
     * @return 返回新增结果
     */
    @Before(Tx.class)
    public boolean savePost(Post post) throws BizException {

        //标题数据库128字符
        if (StringUtils.isNotEmpty(post.getStr("title")) && post.getStr("title").length() > 128) {
            logger.error("帖子标题过长->title:{}", post.getStr("title").length());
            throw new BizException(ErrorCode.POST_TITLE_TOO_LONG);
        }

        if (StringUtils.isEmpty(post.getStr("content")) && StringUtils.isEmpty(post.getStr("thumb_url"))) {
            logger.error("帖子内容不能为空或缩略图不能全为空");
            throw new BizException(ErrorCode.POST_CONTENT_NULL);
        }

        Long group_id = post.getLong("group_id");
        //查询圈子是否存在
        Group group = Group.dao.findById(group_id);
        if (group == null) {
            logger.error("圈子不存在->group_id:{}", group_id);
            throw new BizException(ErrorCode.GROUP_NOT_EXIST);
        }

        //存在帖子类型
        if (post.getLong("type_id") != null) {
            PostType type = PostType.dao.findById(post.getLong("type_id"));
            if (type == null) {
                logger.error("帖子类型不存在");
                throw new BizException(ErrorCode.POST_TYPE_NAME_NOT_EXIST);
            }
        }

        Date date = MDateKit.getNow();
        post.set("created_at", date);
        post.set("updated_at", date);

        boolean flag = post.save();
        //给圈子中的帖子数 +1
        if (flag) {
            //更新圈子的帖子数
            Group.dao.updatePosts(group_id, 1);
            //查询会缓存最新记录
            post = Post.dao.findById(post.getLong("id"));
            //上架，添加es索引
            if (post.getInt("status") == 1) {
                changeEsPost(post);
            }
        }
        return flag;
    }

    /**
     * 修改es内容
     *
     * @param post 待保存到es的帖子
     */
    private void changeEsPost(Post post) {
        post.remove("created_at", "updated_at");
        String str = JsonKit.toJson(post);
        logger.info("{}", str);
        EsUtils.getInstance()
                .createIndex(
                        post.getLong("id") + "",
                        EsIndexType.post.name(),
                        str);
    }

    /**
     * 编辑帖子
     *
     * @param post 待编辑的帖子
     * @return 返回编辑结果
     */
    @Before(Tx.class)
    public boolean updatePost(Post post) throws BizException {
        Long id = post.getLong("id");
        Post dbPost = findById(id);

        if (dbPost == null) {
            logger.error("帖子不存在了");
            throw new BizException(ErrorCode.POST_NOT_EXIST);
        }

        //标题数据库128字符
        if (StringUtils.isNotEmpty(post.getStr("title")) && post.getStr("title").length() > 128) {
            logger.error("帖子标题过长->title:{}", post.getStr("title"));
            throw new BizException(ErrorCode.POST_TITLE_TOO_LONG);
        }

        if (post.getLong("type_id") != null &&
                !Objects.equals(post.getLong("type_id"), dbPost.getLong("type_id"))) {
            PostType type = PostType.dao.findById(post.getLong("type_id"));
            if (type == null) {
                logger.error("帖子类型不存在");
                throw new BizException(ErrorCode.POST_TYPE_NAME_NOT_EXIST);
            }
        }

        post.set("updated_at", MDateKit.getNow());
        boolean flag = post.update();
        if (flag) {
            post = findFirst("select * from t_post where id = ? and valid = 1", id);
            //如果上架，更新es 索引。否则，删除es索引
            if (post.getInt("status") == 1) {
                changeEsPost(post);
            } else {
                EsUtils.getInstance().deleteIndex(post.getLong("id").toString(), EsIndexType.post.name());
            }
            //更新缓存
            Redis.use().hset(Constants.REDIS_POS_KEY, id, post);

            //更新圈子的帖子数 -1
            Group.dao.updatePosts(post.getLong("group_id"), -1);
        }
        return flag;
    }


    /**
     * 删除帖子
     *
     * @param id 需要删除帖子id
     * @return 返回操作结果
     */
    public boolean deletePost(Long id) throws BizException {

        Post post = findById(id);
        return deletePost(post);
    }

    /**
     * 删除帖子
     *
     * @param post 需要删除的帖子
     * @return 返回操作结果
     */
    @Before(Tx.class)
    public boolean deletePost(Post post) throws BizException {

        if (post == null) {
            logger.error("帖子不存在了");
            throw new BizException(ErrorCode.POST_NOT_EXIST);
        }
        post.set("valid", 0);
        post.set("updated_at", MDateKit.getNow());
        boolean flag = post.update();
        if (flag) {
            Group.dao.updatePosts(post.getLong("group_id"), -1);
            //删除es索引
            EsUtils.getInstance().deleteIndex(post.getLong("id").toString(), EsIndexType.post.name());
            Redis.use().hdel(Constants.REDIS_POS_KEY, post.getLong("id"));

            //删除帖子下所有的评论
            Db.update("update t_comment set valid = 0 where post_id = ? ", post.getLong("id"));

            //个人的帖子被删除，发一条私信
//            if (post.getInt("is_sys") == 0) {
//                String to = post.getStr("user_id");
//                PrivateMsg.dao.send("sys_000001", to, "您有一条帖子被删除了!");
//            }

        }
        return flag;
    }

    @Override
    public Post findById(Object idValue) {

        if (idValue == null) {
            return null;
        }

        Post post = Redis.use().hget(Constants.REDIS_POS_KEY, idValue);
        if (post == null) {
            post = findFirst("select * from t_post where valid = 1 and id = ?", idValue);
            if (post != null) {
                Redis.use().hset(Constants.REDIS_POS_KEY, idValue, post);
            }
        }
        return post;
    }

    /**
     * 获取帖子详情，包含作者信息
     *
     * @param id 获取帖子详情（App端需要）
     * @return 返回帖子详情
     */
    public Record findDetail(Long id) throws BizException, IOException {
        logger.info("id:{}", id);
        String sql = "select " +
                "tp.id,tp.title,tp.intro,tp.comments,tp.thumb_url,tp.likes,tp.created_at,tp.type_id,tp.content" +
                ",tp.user_id,tu.user_name,tu.avatar_url ," +
                "tpt.name as type_name,tp.group_id,tp.is_sys," +
                "tp.group_id,tg.name as group_name,tg.thumb_url as group_icon,tp.status " +
                "from t_post tp " +
                "left join t_user tu on tp.user_id = tu.user_id " +
                "left join (select id,name FROM t_post_type WHERE valid = 1 and status = 1) as tpt on tpt.id = tp.type_id " +
                "left join t_group tg on tg.id = tp.group_id  " +
                "where tp.id = ? and tp.valid = 1 ";
        Record record = Db.findFirst(sql, id);
        if (record == null) {
            throw new BizException(ErrorCode.POST_NOT_EXIST);
        }
        record.set("thumb_url", Post.dao.parseThumbUrl(record.getStr("thumb_url"), true));

        if (record.getInt("is_sys") == 0) {
            record.set("title", Sensitive.dao.filterSensitive(record.getStr("title")));
            record.set("content", Sensitive.dao.filterSensitive(record.getStr("content")));
            record.set("user_name", Sensitive.dao.filterSensitive(record.getStr("user_name")));
        }
        return record;
    }

    /**
     * 查询帖子下的置顶帖子
     *
     * @param group_id 帖子所属的圈子id
     * @param all      是否查询所有的。App端只显示上架（status=1）的，所有会显示不全。
     * @param showType 显示类型 ios | android
     * @return 返回置顶列表
     */
    public List<Record> findTops(Long group_id, boolean all, String showType) throws BizException, IOException {
        if (group_id == null) {

            throw new BizException(ErrorCode.MISSING_PARM);
        }

        Group group = Group.dao.findById(group_id);
        if (group == null) {
            throw new BizException(ErrorCode.GROUP_NOT_EXIST);
        }
        String sql = "select tp.id,tp.title,py.name as type_name,tp.is_sys " +
                "from t_post tp " +
                "left join (select id,name FROM t_post_type WHERE valid = 1 and status = 1) as py on tp.type_id = py.id " +
                "where tp.valid = 1 and tp.top = 1  and tp.group_id = ? ";
        if (all) {
            return Db.find(sql, group_id);
        } else {

            if (StringUtils.isNotEmpty(showType)) {

                if (StringUtils.equals(showType, "ios")) {
                    sql += " and tp.ios_show = 1 ";
                }

                if (StringUtils.equals(showType, "android")) {
                    sql += " and tp.android_show = 1 ";
                }
            }

        }

        List<Record> records = Db.find(sql + " and tp.status = 1 order by tp.idx desc,tp.created_at desc ", group_id);
//        for (Record record : records) {
//            record.set("title", Sensitive.dao.filterSensitive(record.getStr("title")));
//        }
        return records;
    }

//    /**
//     * 根据标签查询帖子
//     *
//     * @return
//     */
//    public List<Record> findTags(Long group_id, Long tag_id) {
//
//        List<Record> posts = Db.find("select tp.id,tp.title  from t_post tp " +
//                "left join t_post_tags tpts on tp.id = tpts.post_id " +
//                "left join t_post_tag tpt on tpts.tag_id = tpt.id " +
//                " where  and tp.group_id = ? and tpt.id = ? ", group_id, tag_id);
//        return posts;
//    }

    /**
     * 查询分页
     *
     * @param pageRequest 分页参数
     * @param user_id     用于判断分页中的记录是否被当前用户点赞
     * @return 返回APP端需要的分页数据
     */
    public Page<Record> findPage(PageRequest pageRequest, String user_id) throws IOException {

        String sql = "select tp.id,tp.title,tp.intro,tp.thumb_url,tp.content," +
                "tp.status,tp.created_at,tp.likes,tp.comments," +
                "tp.user_id,tu.user_name,tu.avatar_url," +
                "tp.group_id,tg.name as group_name,tg.thumb_url as group_icon," +
                "tp.is_sys,tp.type_id,ty.name as type_name  ";
        String sql_ex = "from t_post tp " +
                "left join t_user tu on tp.user_id = tu.user_id " +
                "left join t_group tg on tg.id = tp.group_id " +
                "left join (select id,name FROM t_post_type WHERE valid = 1 and status = 1) as ty on tp.type_id = ty.id " +
                "where tp.valid = 1 and tp.top = 0  and tp.status = 1 ";

        Map<String, String> params = pageRequest.getParams();
        List<Object> ls = new ArrayList<>();
        if (StringUtils.isNotEmpty(params.get("title"))) {
            sql_ex += " and tp.title like ?";
            ls.add(params.get("title"));
        }

//        if (StringUtils.isNotEmpty(params.get("status"))) {
//            sql_ex += " and tp.status = ? ";
//            ls.add(params.get("status"));
//        }

        if (StringUtils.isNotEmpty(params.get("group_id"))) {
            sql_ex += " and tp.group_id = ? ";
            ls.add(params.get("group_id"));
        }
//        if (params.get("tag_id") != null) {
//            sql_ex += " and tpt.id  = ? ";
//            ls.add(params.get("tag_id"));
//        }

        if (StringUtils.isNotEmpty(params.get("type_id"))) {
            sql_ex += " and tp.type_id = ? ";
            ls.add(params.get("type_id"));
        }


        if (StringUtils.isNotEmpty(params.get("ios_show"))) {
            sql_ex += " and tp.ios_show = ? ";
            ls.add(params.get("ios_show"));
        }

        if (StringUtils.isNotEmpty(params.get("android_show"))) {
            sql_ex += " and tp.android_show = ? ";
            ls.add(params.get("android_show"));
        }

//        if (params.get("user_id") != null && !params.get("user_id").toString().equals("")) {
//            sql_ex += " and tp.user_id = ? ";
//            ls.add(params.get("user_id"));
//        }

//        tp.idx desc,
        //和产品确认过，确认不排序
        sql_ex += " order by tp.created_at desc";
        Page<Record> page = Db.paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, ls.toArray());
        parsePageList(page.getList(), user_id);
        return page;
    }


    /**
     * 获取我|他的帖子。
     *
     * @param pageRequest 请求参数
     * @return 返回请求分页数据
     */
    public Page<Record> findMyPosts(PageRequest pageRequest) throws IOException {

        String sql = "select tp.id,tp.title,tp.intro,tp.thumb_url,tp.content," +
                "tp.status,tp.created_at,tp.likes,tp.comments," +
                "tp.user_id,tu.user_name,tu.avatar_url," +
                "tp.group_id,tg.name as group_name,tg.thumb_url as group_icon," +
                "ty.name as type_name,tp.is_sys,tp.type_id  ";

        String sql_ex = " from t_post tp " +
                " left join t_user tu on tp.user_id = tu.user_id " +
                " left join t_group tg on tg.id = tp.group_id " +
                " left join (select id,name FROM t_post_type WHERE valid = 1 and status = 1) as ty on tp.type_id = ty.id " +
                " where tp.valid = 1 and tp.user_id = ? ";

        Map<String, String> params = pageRequest.getParams();
        List<Object> ls = new ArrayList<>();
        ls.add(params.get("user_id"));
        if (StringUtils.isNotEmpty(params.get("ios_show"))) {
            sql_ex += " and tp.ios_show = ? ";
            ls.add(params.get("ios_show"));
        }

        if (StringUtils.isNotEmpty(params.get("android_show"))) {
            sql_ex += " and tp.android_show = ? ";
            ls.add(params.get("android_show"));
        }

        sql_ex += "order by tp.created_at desc";
        Page<Record> page = Db.paginate(
                pageRequest.getPageNo(),
                pageRequest.getPageSize(),
                sql,
                sql_ex,
                ls.toArray());
        parsePageList(page.getList(), null);
        return page;
    }


    /**
     * 查询分页
     *
     * @param pageRequest 请求参数
     * @return 返回后台前端所需要的分页数据
     */
    public Page<Record> findPageAdmin(PageRequest pageRequest) {

        String sql = "select tp.id,tp.title,tp.intro,tp.thumb_url,tp.content," +
                "tp.status,tp.created_at,tp.likes,tp.comments," +
                "tp.user_id,tu.user_name,tu.avatar_url,tp.is_sys,tp.top," +
                "tp.type_id,ty.name as type_name,tp.ios_show,tp.android_show  ";
        String sql_ex = "from t_post tp " +
                "left join t_user tu on tp.user_id = tu.user_id " +
                "left join t_post_type ty on tp.type_id = ty.id " +
                "left join t_group tg on tp.group_id = tg.id  " +
                "where tp.valid = 1 and tg.valid = 1 ";

        Map<String, String> params = pageRequest.getParams();
        List<Object> ls = new ArrayList<>();
        if (StringUtils.isNotEmpty(params.get("title"))) {
            sql_ex += " and tp.title like ?";
            ls.add("%" + params.get("title") + "%");
        }

        if (StringUtils.isNotEmpty(params.get("status"))) {
            sql_ex += " and tp.status = ? ";
            ls.add(params.get("status"));
        }

        if (StringUtils.isNotEmpty(params.get("type_id"))) {
            sql_ex += " and tp.type_id = ? ";
            ls.add(params.get("type_id"));
        }

        if (StringUtils.isNotEmpty(params.get("group_id"))) {
            sql_ex += " and tp.group_id = ? ";
            ls.add(params.get("group_id"));
        }


        if (StringUtils.isNotEmpty(params.get("ios_show"))) {
            sql_ex += " and tp.ios_show = ? ";
            ls.add(params.get("ios_show"));
        }

        if (StringUtils.isNotEmpty(params.get("android_show"))) {
            sql_ex += " and tp.android_show = ? ";
            ls.add(params.get("android_show"));
        }

        sql_ex += " order by tp.top desc,tp.idx desc,tp.created_at desc";
        Page<Record> page = Db.paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, ls.toArray());
        parseAdminPage(page);
        return page;
    }


    /**
     * 解析帖子分页数据。丰富分页数据
     *
     * @param records 记录列表
     * @param user_id 用于判断当前记录是否被该用户点过赞。
     */
    void parsePageList(List<Record> records, String user_id) throws IOException {

        if (CollectionUtils.isNotEmpty(records)) {
            for (Record record : records) {
//                //获取所有可用标签
//                //因为存在精华标签待审核，所有不能获取所有的标签
//                List<Record> tags = PostTags.dao.findTagsByPost(record.getLong("id"));
//                if (CollectionUtils.isNotEmpty(tags)) {
//                    record.set("tags", tags);
//                }
//                Long type_id = record.getLong("type_id");
//                if (type_id != null) {
//                    Record pt = Db.findFirst("select * from t_post_type where status = 1 and valid = 1 and id = ?", type_id);
//                    if (pt != null) {
//                        record.set("type_name", record.getStr("name"));
//                    }
//                }

                if (record.getInt("is_sys") == 0) {
                    record.set("title", Sensitive.dao.filterSensitive(record.getStr("title")));
                    String content = Sensitive.dao.filterSensitive(record.getStr("content"));
                    record.set("content", content);
                    record.set("intro", Sensitive.dao.filterSensitive(record.getStr("intro")));
                    record.set("user_name", Sensitive.dao.filterSensitive(record.getStr("user_name")));
                }

                //转换图片
                record.set("thumb_url", Post.dao.parseThumbUrl(record.getStr("thumb_url"), false));
                //是否点赞
                if (user_id != null) {
                    record.set("praised", PostLike.dao.liked(record.getLong("id"), user_id));
                } else {
                    record.set("praised", false);
                }
            }
        }
    }

    /**
     * 将url数据转为数组
     *
     * @param thumbUrl 缩略图。以;分割的数据。
     * @return 返回缩略图列表
     */
    public Object[] parseThumbUrl(String thumbUrl, boolean all) {

        if (StringUtils.isNotEmpty(thumbUrl)) {
            Object[] thumbs = thumbUrl.split(";");
            logger.info("thumbs_size:{}", thumbs.length);
            if (thumbs.length > 3 && !all) {
                thumbs = Arrays.asList(thumbs).subList(0, 3).toArray();
            }
            return thumbs;
        } else {
            return new String[]{};
        }
    }

    /**
     * @param page 分页数据。将分页数据中的thumb_url，转换未后端前台页面所需要的数据类型。
     */
    void parseAdminPage(Page<Record> page) {
        if (CollectionUtils.isNotEmpty(page.getList())) {
            List<Record> lrs = page.getList();
            for (Record record : lrs) {
                String thumb_url = record.getStr("thumb_url");
                if (StringUtils.isNotEmpty(thumb_url)) {
                    String[] strings = thumb_url.split(";");
                    List<Map<String, String>> ths = new ArrayList<>();
                    for (String tb : strings) {
                        Map<String, String> map = new HashMap<>();
                        map.put("picUrl", tb);
                        ths.add(map);
                    }
                    record.set("thumb_url", strings[0]);
                    record.set("thumb_urls", ths);
                } else {
                    Map<String, String> map = new HashMap<>();
                    map.put("picUrl", "");
                    record.set("thumb_urls", new Object[]{map});
                }
            }
        }
    }

    /**
     * 更新帖子的评论数
     *
     * @param post_id 需要更新评论数的帖子的id
     * @param step    新增数 1 增1，-1 减1
     * @return 返回操作结果
     */
    public boolean updateComments(Long post_id, int step) throws BizException {

        Post post = findById(post_id);
        if (post == null) {
            logger.error("帖子不存在了");
            throw new BizException(ErrorCode.POST_NOT_EXIST);
        }

//        int num;
//        if (step < 0 && post.getInt("comments") <= 0) {
//            num = 0;
//        } else {
//            int tmp = post.getInt("comments") <= 0 ? 0 : post.getInt("comments");
//            num = tmp + step;
//        }
        Long count = Db.queryLong("select count(id) from t_comment where post_id = ? and valid = 1", post_id);
        post.set("comments", count);
        return updatePost(post);
    }

    /**
     * 更新点赞数
     *
     * @param post_id 要点赞的帖子id
     * @param step    是点赞 1，还是取消点赞 -1
     * @return 返回操作结果
     */
    boolean updateLikes(Long post_id, int step) throws BizException {

        Post post = findById(post_id);
        if (post == null) {
            logger.error("帖子不存在了");
            throw new BizException(ErrorCode.POST_NOT_EXIST);
        }
//        int num;
//        if (step < 0 && post.getInt("likes") <= 0) {
//            num = 0;
//        } else {
//            int tmp = post.getInt("likes") <= 0 ? 0 : post.getInt("likes");
//            num = tmp + step;
//        }
        Long count = Db.queryLong("select count(id) from t_post_likes where post_id = ? ", post_id);
        post.set("likes", count);
        return updatePost(post);
    }
}
