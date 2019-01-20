package com.bingqiong.bq.model.category;

import com.bingqiong.bq.comm.constants.Constants;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.constants.EsIndexType;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.utils.EsUtils;
import com.bingqiong.bq.comm.utils.MDateKit;
import com.bingqiong.bq.comm.utils.ValidateUtils;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.BaseModel;
import com.bingqiong.bq.model.post.PostTag;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Redis;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 圈子，板块的下级分类
 * <p>
 * Created by hunsy on 2017/6/22.
 */
public class Group extends BaseModel<Group> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final Group dao = new Group();
    public static final String TABLE_GROUP = "t_group";

    /**
     * 创建板块
     *
     * @param group 圈子记录
     * @return 返回保存结果
     */
    @Before(Tx.class)
    public boolean saveGroup(Group group) throws BizException {

        Long plate_id = group.getLong("plate_id");
        String name = group.getStr("name");
        //验证板块，圈子必须放在板块下面
        if (plate_id == null) {
            logger.error("缺少参数plate_id");
            throw new BizException(ErrorCode.MISSING_PARM);
        }
        //查询板块是否存在
        Plate dbPlate = Plate.dao.findById(plate_id);
        if (dbPlate == null) {
            logger.error("板块不存在->plate_id:{}", plate_id);
            throw new BizException(ErrorCode.PLATE_NOT_EXIST);
        }
        //验证名称
        validateGroupName(name);
        Date mdate = MDateKit.getNow();
        group.set("created_at", mdate);
        group.set("updated_at", mdate);

        //新增一条以圈子名称为名的帖子标签（PostTag）
        PostTag tag = new PostTag();
        tag.set("sys", 1);
        tag.set("name", name);
        boolean saveTag = PostTag.dao.saveTag(tag);
        if (!saveTag) {
            tag = PostTag.dao.findTagByName(name);
        }

        //设置圈子的标签id
        group.set("tag_id", tag.getLong("id"));
        boolean flag = group.save();
        if (flag) {
            //更新板块的圈子数 +1
            Plate.dao.updateGroups(plate_id, 1);
            //在查询圈子时，会缓存最新的记录
            group = findById(group.getLong("id"));
            //只有上架的圈子才能再elastic search中查询。所有只有状态是1时。才添加es索引
            if (group.getInt("status") == 1) {
                //添加es 搜索
                logger.info("开始添加es索引");
                changeEsGroup(group);
            }
            Redis.use().sadd(Constants.REDIS_GROUP_NAME_KEY, group.getStr("name"));
        }
        return flag;
    }

    /**
     * 更新板块
     *
     * @param group 圈子
     * @return 返回更新结果
     */
    @Before(Tx.class)
    public boolean updateGroup(Group group) throws BizException {

        Long group_id = group.getLong("id");
        Group dbGroup = findById(group_id);
        if (dbGroup == null) {
            logger.error("圈子不存在->group_id:{}", group_id);
            throw new BizException(ErrorCode.GROUP_NOT_EXIST);
        }
        //验证新的圈子名称
        boolean change = false;//圈子名称是否变更
        if (!StringUtils.equals(group.getStr("name"), dbGroup.getStr("name"))) {
            validateGroupName(group.getStr("name"));
            //更新标签
            Long tag_id = dbGroup.getLong("tag_id");
            PostTag tag = PostTag.dao.findById(tag_id);
            tag.set("name", group.getStr("name"));
            PostTag.dao.updateTag(tag);
            change = true;
        }
        //更新时间
        dbGroup.set("updated_at", MDateKit.getNow());
        boolean flag = group.update();
        if (flag) {
            //因为findById是读缓存，所以要重新查询一下数据库
            group = findFirst("select * from t_group where id = ? and valid = 1", group_id);
            Redis.use().hset(Constants.REDIS_GROUP_KEY, group.getLong("id"), group);
            if (group.getInt("status") == 1) {
                changeEsGroup(group);
                //下架删除索引
            } else {
                EsUtils.getInstance().deleteIndex(group.getLong("id").toString(), EsIndexType.group.name());
            }
            if (change) {
                Redis.use().srem(Constants.REDIS_GROUP_NAME_KEY, dbGroup.getStr("name"));
                Redis.use().sadd(Constants.REDIS_GROUP_NAME_KEY, group.getStr("name"));
            }
            //删除板块圈子列表的缓存
            Plate.dao.removePlateGroupCache();

            //更新板块的圈子数 -1
            Plate.dao.updateGroups(group.getLong("plate_id"), -1);
        }
        return flag;
    }

    /**
     * 删除
     *
     * @param id 圈子id
     * @return 返回删除结果
     */
    @Before(Tx.class)
    public boolean deleteGroup(Long id) throws BizException {
        if (id == null) {
            logger.error("id为空");
            throw new BizException(ErrorCode.MISSING_PARM);
        }
        Group dbGroup = findById(id);
        return deleteGroup(dbGroup);
    }

    /**
     * 删除
     *
     * @param group 圈子
     * @return 返回删除圈子结果
     */
    @Before(Tx.class)
    public boolean deleteGroup(Group group) throws BizException {

        if (group == null) {
            logger.error("圈子不存在");
            throw new BizException(ErrorCode.GROUP_NOT_EXIST);
        }
        //判断圈子下的是否存在帖子
        long i = Db.queryLong("select count(id) from t_post where group_id = ? and valid = 1 ", group.getLong("id"));
        if (i > 0) {
            throw new BizException(ErrorCode.POST_MORE_EXIST);
        }
        group.set("valid", 0);
        group.set("updated_at", MDateKit.getNow());
        boolean flag = group.update();
        //板块中的圈子数 -1
        if (flag) {
            //更新板块的圈子数 -1
            Plate.dao.updateGroups(group.getLong("plate_id"), -1);
            //删除es索引
            EsUtils.getInstance().deleteIndex(group.getLong("id").toString(), EsIndexType.group.name());
            //删除缓存
            Redis.use().hdel(Constants.REDIS_GROUP_KEY, group.getLong("id"));
            //删除缓存的名称
            Redis.use().srem(Constants.REDIS_GROUP_NAME_KEY, group.getStr("name"));
//            //删除圈子所对应的标签
//            PostTag.dao.deleteTag(group.getStr("name"));
        }
        return flag;
    }


    /**
     * @param idValue 圈子id
     * @return 返回查询的圈子
     */
    @Override
    public Group findById(Object idValue) {

        logger.info("查询圈子记录->id:{}", idValue);
        if (idValue == null) {
            return null;
        }
        Group group = Redis.use().hget(Constants.REDIS_GROUP_KEY, idValue);
        if (group == null) {
            group = findFirst("select * from t_group where valid = 1 and id = ?", idValue);
            if (group != null) {
                Redis.use().hset(Constants.REDIS_GROUP_KEY, idValue, group);
            }
        }
        logger.info("圈子记录->group:{}", JsonKit.toJson(group));
        return group;
    }

    /**
     * 通过名称查询
     *
     * @param name 圈子名称
     * @return 返回根据名称查到的圈子结果
     */
    private boolean findByName(String name) {

        boolean flag = Redis.use().sismember(Constants.REDIS_GROUP_NAME_KEY, name);
        if (!flag) {
            Group group = findFirst("select * from " + TABLE_GROUP + " where name = ? and valid = 1", name);
            if (group != null) {
                flag = true;
                Redis.use().sadd(Constants.REDIS_GROUP_NAME_KEY, name);
            }
        }
        return flag;
    }

    /**
     * 查询分页
     *
     * @param pageRequest 查询条件
     * @return 返回圈子的分页记录
     */
    public Page<Record> findPage(PageRequest pageRequest) {

        String sql = "select tg.id,tg.name,tg.thumb_url,tg.status," +
                "tg.follows,tg.posts,tg.created_at,tg.updated_at," +
                "tg.ios_show,tg.android_show  ";
        String sql_ex = "from t_group tg " +
//                "left join t_group_follows tgf on tg.id = tgf.group_id " +
                "where tg.valid = 1 ";

        Map<String, String> parmas = pageRequest.getParams();
        List<Object> lp = new ArrayList<>();
        if (StringUtils.isNotEmpty(parmas.get("plate_id"))) {
            sql_ex += " and tg.plate_id = ? ";
            lp.add(parmas.get("plate_id"));
        }

        if (StringUtils.isNotEmpty(parmas.get("name"))) {
            sql_ex += " and tg.name like ? ";
            lp.add("%" + parmas.get("name") + "%");
        }

        if (StringUtils.isNotEmpty(parmas.get("status"))) {
            sql_ex += " and tg.status = ? ";
            lp.add(parmas.get("status"));
        }

        if (StringUtils.isNotEmpty(parmas.get("id"))) {
            sql_ex += " and tg.id = ? ";
            lp.add(parmas.get("id"));
        }


        if (StringUtils.isNotEmpty(parmas.get("ios_show"))) {
            sql_ex += " and tg.ios_show = ? ";
            lp.add(parmas.get("ios_show"));
        }

        if (StringUtils.isNotEmpty(parmas.get("android_show"))) {
            sql_ex += " and tg.android_show = ? ";
            lp.add(parmas.get("android_show"));
        }
        sql_ex += " order by tg.idx desc,tg.created_at desc,tg.name asc";
        Page<Record> page = Db.paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, lp.toArray());
//        //是否关注
//        if (StringUtils.isNotEmpty(parmas.get("user_id"))) {
//            String user_id = parmas.get("user_id");
//            if (page.getList() != null) {
//                for (Record record : page.getList()) {
//                    GroupFollows follows = GroupFollows.dao.userFollowed(user_id, record.getLong("id"));
//                    if (follows != null) {
//                        record.set("followed", true);
//                    } else {
//                        record.set("followed", false);
//                    }
//                }
//            }
//        }
        return page;
    }


    /**
     * 查询分页
     *
     * @param pageRequest 查询条件
     * @return 返回圈子的分页记录
     */
    public Page<Record> findFollowsPage(PageRequest pageRequest) {

        String sql = "select tg.id,tg.name,tg.thumb_url,tg.status," +
                "tg.follows,tg.posts,tg.created_at,tg.updated_at  ";
        String sql_ex = "from t_group tg " +
                "left join t_group_follows tgf on tg.id = tgf.group_id " +
                "where tg.valid = 1 and tgf.user_id = ? ";

        Map<String, String> parmas = pageRequest.getParams();
        List<Object> lp = new ArrayList<>();
        lp.add(parmas.get("user_id"));

        if (StringUtils.isNotEmpty(parmas.get("ios_show"))) {
            sql_ex += " and tg.ios_show = ? ";
            lp.add(parmas.get("ios_show"));
        }

        if (StringUtils.isNotEmpty(parmas.get("android_show"))) {
            sql_ex += " and tg.android_show = ? ";
            lp.add(parmas.get("android_show"));
        }

        sql_ex += " order by tg.idx desc,tg.created_at desc";
        Page<Record> page = Db.paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, lp.toArray());
        return page;
    }


    /**
     * 验证圈子名称
     *
     * @param name 圈子名称
     */
    private void validateGroupName(String name) throws BizException {
        //名称不能为空
        if (StringUtils.isEmpty(name)) {
            logger.error("圈子名称不能为空");
            throw new BizException(ErrorCode.GROUP_NAME_NULL);
        }
        //查询名称是否存在
        if (findByName(name)) {
            logger.error("圈子名称已经存在->name:{}", name);
            throw new BizException(ErrorCode.GROUP_NAME_EXIST);
        }

        if (!ValidateUtils.validateStrLen(name, Constants.GROUP_NAME_LEN_MIN, Constants.GROUP_NAME_LEN_MAX)) {
            logger.error("圈子名称格式错误");
            throw new BizException(ErrorCode.GROUP_NAME_ILLEGLE);
        }
    }

    /**
     * 修改es内容
     *
     * @param group 更新圈子的es数据
     */
    private void changeEsGroup(Group group) {

        EsUtils.getInstance()
                .createIndex(
                        group.getLong("id") + "",
                        EsIndexType.group.name(),
                        JsonKit.toJson(group));
    }

    /**
     * 获取圈子列表
     *
     * @param plate_id 板块id
     * @return 返回（板块id下的)圈子列表
     */
    public List<Record> findList(Long plate_id) {
        String sql = "select id as value,name " +
                "from t_group " +
                "where valid = 1 ";
        if (plate_id != null) {
            sql += " and plate_id = ? order by idx desc,created_at desc";
            return Db.find(sql, plate_id);
        }
        sql += "order by idx desc,created_at desc";
        return Db.find(sql);
    }

    /**
     * 更新关注数
     *
     * @param group_id 圈子id
     * @param step     步长
     * @return 返回结果
     */
    boolean updateFollows(Long group_id, int step) throws BizException {
        logger.info("更新帖子的关注记录");
        Group group = findById(group_id);
        if (group == null) {
            throw new BizException(ErrorCode.GROUP_NOT_EXIST);
        }

//        int num;
//        if (step < 0 && group.getInt("follows") == 0) {
//            num = 0;
//        } else {
//            num = group.getInt("follows") + step;
//        }
        Long count = Db.queryLong("select count(id) from t_group_follows where group_id = ? ", group_id);
        group.set("follows", count);
        return updateGroup(group);
    }

    /**
     * 变更圈子中的帖子数
     *
     * @param group_id 圈子id
     * @param step     步长
     * @return 返回更新帖子数的结果
     */
    @Before(Tx.class)
    public boolean updatePosts(Long group_id, int step) throws BizException {
        logger.info("更新圈子的帖子数");
        Group group = findById(group_id);
        if (group == null) {
            logger.error("圈子不存在->id:{}", group_id);
            throw new BizException(ErrorCode.GROUP_NOT_EXIST);
        }
//        int num;
//        if (step < 0 && group.getInt("posts") == 0) {
//            num = 0;
//        } else {
//            num = group.getInt("posts") + step;
//        }
        //// TODO: 2017/8/11 是否是上架的数量
        Long count = Db.queryLong("select count(id) from t_post where group_id = ? and valid = 1 and status = 1", group_id);
        group.set("posts", count);
        return updateGroup(group);
    }

    /**
     * 转移圈子到其他的版块下
     *
     * @param id
     * @param plate_id
     */
    public void move(Long id, Long plate_id) throws BizException {

        Group group = findById(id);
        //原来的版块id
        Long orignal_plate_id = group.getLong("plate_id");
        Plate plate = Plate.dao.findById(plate_id);
        if (plate == null) {
            logger.error("版块不存在->{}", plate_id);
            throw new BizException(ErrorCode.PLATE_NOT_EXIST);
        }
        group.set("plate_id", plate_id);
        updateGroup(group);
        //原版块-1
        Plate.dao.updateGroups(orignal_plate_id, -1);
    }
}
