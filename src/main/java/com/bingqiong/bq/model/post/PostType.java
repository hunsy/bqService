package com.bingqiong.bq.model.post;

import com.bingqiong.bq.comm.constants.Constants;
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
import com.jfinal.plugin.redis.Redis;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 帖子类型
 * Created by hunsy on 2017/6/30.
 */
public class PostType extends BaseModel<PostType> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static PostType dao = new PostType();
    public static final String TABLE_POST_TYPE = "t_post_type";

    /**
     * 添加类型
     *
     * @param type 贴心类型
     * @return 返回保存帖子类型的结果
     */
    @Before(Tx.class)
    public boolean saveType(PostType type) throws BizException {

        String name = type.getStr("name");
        validateName(name);
        Date date = MDateKit.getNow();
        type.set("created_at", date);
        type.set("updated_at", date);
        boolean flag = type.save();
        if (flag) {
            //查询最新的记录。会进行缓存
            findById(type.getLong("id"));
            //缓存名称
            Redis.use().sadd(Constants.REDIS_POST_TYPE_NAME_KEY, name);
            //清除列表缓存
            removeListCache();
        }
        return flag;
    }


    /**
     * 更新类型
     *
     * @param type 帖子类型
     * @return 返回编辑帖子类型的结果
     * @throws BizException
     */
    @Before(Tx.class)
    public boolean updateType(PostType type) throws BizException {
        Long id = type.getLong("id");
        PostType dbType = findById(id);
        if (dbType == null) {
            logger.error("帖子类型不存在");
            throw new BizException(ErrorCode.POST_TYPE_NAME_NOT_NULL);
        }
        boolean change = false;
        if (StringUtils.isNotEmpty(type.getStr("name")) && !StringUtils.equals(dbType.getStr("name"), type.getStr("name"))) {
            validateName(type.getStr("name"));
            change = true;
        }

        type.set("updated_at", MDateKit.getNow());
        boolean flag = type.update();
        if (flag) {
            type = findFirst("select * from t_post_type where id = ? and valid = 1", id);
            //缓存对象
            Redis.use().hset(Constants.REDIS_POST_TYPE_KEY, type.getLong("id"), type);
            //缓存新名称
            if (change) {
                Redis.use().srem(Constants.REDIS_POST_TYPE_NAME_KEY, dbType.getStr("name"));
                Redis.use().sadd(Constants.REDIS_POST_TYPE_NAME_KEY, type.getStr("name"));
            }
            removeListCache();
        }
        return flag;
    }


    /**
     * 删除类型
     *
     * @param id 帖子类型id
     * @return 返回删除帖子类型的结果
     * @throws BizException
     */
    public boolean deleteType(Long id) throws BizException {

        PostType type = findById(id);
        return deleteType(type);
    }

    /**
     * 删除类型
     *
     * @param type 帖子类型
     * @return 返回删除帖子类型的结果
     * @throws BizException
     */
    @Before(Tx.class)
    private boolean deleteType(PostType type) throws BizException {

        if (type == null) {
            logger.error("帖子类型不存在");
            throw new BizException(ErrorCode.POST_TYPE_NAME_NOT_NULL);
        }
        type.set("valid", 0);
        boolean flag = type.update();
        if (flag) {
            Redis.use().del(Constants.REDIS_POST_TYPE_KEY, type.getLong("id"));
            Redis.use().srem(Constants.REDIS_POST_TYPE_NAME_KEY, type.getStr("name"));
            removeListCache();
        }
        return flag;
    }


    /**
     * 通过名称查询帖子类型
     *
     * @param name 类型名称
     * @return 返回查询结果
     */
    private Boolean findByName(String name) {

        boolean flag = Redis.use().sismember(Constants.REDIS_POST_TYPE_NAME_KEY, name);
        if (!flag) {
            PostType type = findFirst("select * from t_post_type where name = ? and valid = 1 ", name);
            if (type != null) {
                flag = true;
                Redis.use().sadd(Constants.REDIS_POST_TYPE_NAME_KEY, type.getStr("name"));
            }
        }
        return flag;
    }


    @Override
    public PostType findById(Object idValue) {
        if (idValue == null) {
            return null;
        }
        PostType type = Redis.use().hget(Constants.REDIS_POST_TYPE_KEY, idValue);
        if (type == null) {
            type = findFirst("select * from t_post_type where valid = 1 and id = ?", idValue);
            if (type != null) {
                Redis.use().hset(Constants.REDIS_POST_TYPE_KEY, idValue, type);
            }
        }
        return type;
    }

    /**
     * @param name 类型名称
     * @throws BizException
     */
    private void validateName(String name) throws BizException {
        if (StringUtils.isEmpty(name)) {

            throw new BizException(ErrorCode.POST_TYPE_NAME_NOT_NULL);
        }

        if (name.length() < 2 || name.length() > 6) {

            throw new BizException(ErrorCode.POST_TYPE_NAME_ILLEGLE);
        }

        if (findByName(name)) {
            logger.error("分类名称已存在");
            throw new BizException(ErrorCode.POST_TYPE_NAME_EXIST);
        }
    }

    /**
     * 列表
     *
     * @return 返回列表
     */
    public List<Record> findList(int status) {
        List<Record> ls;
        if (status == 1) {
            ls = Redis.use().get(Constants.REDIS_POST_TYPE_LIST_KEY);
            if (ls == null) {
                ls = Db.find("select id,name,status,created_at,updated_at " +
                        "from t_post_type " +
                        "where valid = 1 and status = 1 order by idx desc");
                if (ls != null) {
                    Redis.use().set(Constants.REDIS_POST_TYPE_LIST_KEY, ls);
                }
            }
        } else {
            ls = Db.find("select id as value,name,status,created_at,updated_at " +
                    "from t_post_type " +
                    "where valid = 1 order by idx desc");
        }
        return ls;
    }

    /**
     * 分页数据
     *
     * @param request 请求参数
     * @return 返回查询分页数据
     */
    public Page<Record> findPage(PageRequest request) {

        String sql = "select id,name,status,created_at,updated_at ";
        String sql_ex = "from t_post_type " +
                "where valid = 1 ";

        Map<String, String> params = request.getParams();
        List<String> lp = new ArrayList<>();
        if (StringUtils.isNotEmpty(params.get("name"))) {
            sql_ex += " and name like ? ";
            lp.add("%" + params.get("name") + "%");
        }

        if (StringUtils.isNotEmpty(params.get("status"))) {
            sql_ex += " and status = ? ";
            lp.add(params.get("status"));
        }

        sql_ex += " order by idx desc, created_at desc";
        return Db.paginate(request.getPageNo(), request.getPageSize(), sql, sql_ex, lp.toArray());
    }

    /**
     * 清除列表缓存
     */
    private void removeListCache() {
        Redis.use().del(Constants.REDIS_POST_TYPE_LIST_KEY);
    }
}
