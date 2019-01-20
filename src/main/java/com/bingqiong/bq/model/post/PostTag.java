package com.bingqiong.bq.model.post;

import com.bingqiong.bq.comm.constants.Constants;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.utils.MDateKit;
import com.bingqiong.bq.comm.utils.ValidateUtils;
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
import java.util.List;
import java.util.Map;

/**
 * 帖子标签定义
 * <p>
 * Created by hunsy on 2017/6/23.
 */
public class PostTag extends BaseModel<PostTag> {


    public static final PostTag dao = new PostTag();
    public static final String TABLE_POST_TAG = "t_post_tag";
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 新增标签
     *
     * @param tag 标签记录
     * @return 返回保存结果
     */
    @Before(Tx.class)
    public boolean saveTag(PostTag tag) throws BizException {

        String name = tag.getStr("name");
        validareTagName(name);
        boolean flag = false;
        if (!findByName(name)) {
            tag.set("created_at", MDateKit.getNow());
            flag = tag.save();
            if (flag) {
                //缓存
                findById(tag.getLong("id"));
                //缓存tag名称
                Redis.use().sadd(Constants.REDIS_POST_TAG_NAME_KEY, name);
            }
        }

        return flag;
    }


    /**
     * 更新标签
     *
     * @param tag 标签记录
     * @return 返回编辑结果
     * @throws BizException
     */
    @Before(Tx.class)
    public boolean updateTag(PostTag tag) throws BizException {

        Long id = tag.getLong("id");
        PostTag dbTag = findById(id);
        if (dbTag == null) {
            logger.error("标签不存在");
            throw new BizException(ErrorCode.POST_TAG_NOT_EXIST);
        }
        boolean change = false;
        //存在名称，并且有变更，则重新验证名称
        if (StringUtils.isNotEmpty(tag.getStr("name")) && !tag.getStr("name").equals(dbTag.getStr("name"))) {
            validareTagName(tag.getStr("name"));
            change = true;
        }
        boolean flag = false;
        if (change) {
            if (!findByName(tag.getStr("name"))) {
                flag = tag.update();
                if (flag) {
                    //更新缓存
                    tag = findFirst("select * from t_post_tag where id = ? and valid = 1", id);
                    Redis.use().hset(Constants.REDIS_POST_TAG_KEY, tag.getLong("id"), tag);
                    if (change) {
                        //缓存tag名称
                        Redis.use().srem(Constants.REDIS_POST_TAG_NAME_KEY, dbTag.getStr("name"));
                        Redis.use().sadd(Constants.REDIS_POST_TAG_NAME_KEY, tag.getStr("name"));
                    }
                }
            }
        }
        return flag;
    }


    /**
     * 删除标签
     *
     * @param id 待删除贴在的id
     * @return 返回删除结果
     */
    public boolean deleteTag(Long id) throws BizException {

        PostTag dbTag = findById(id);
        return deleteTag(dbTag);
    }

    /**
     * 删除标签
     *
     * @param name 待删除标签名称
     * @return 返回删除结果
     */
    public boolean deleteTag(String name) throws BizException {

        PostTag dbTag = findFirst("select * from t_post_tag where valid = 1 and name = ?", name);
        return deleteTag(dbTag);
    }

    /**
     * 删除标签
     *
     * @param tag 帖子标签
     * @return 返回删除帖子标签的结果
     * @throws BizException
     */
    @Before(Tx.class)
    public boolean deleteTag(PostTag tag) throws BizException {
        if (tag == null) {
            logger.error("标签不存在");
            throw new BizException(ErrorCode.POST_TAG_NOT_EXIST);
        }
        if (tag.getInt("sys") == 1) {
            logger.error("系统标签");
            throw new BizException(ErrorCode.POST_TAG_SYS);
        }
        tag.set("valid", 0);
        boolean flag = tag.update();
        if (flag) {
            Redis.use().hdel(Constants.REDIS_POST_TAG_KEY, tag.getLong("id"));
            Redis.use().srem(Constants.REDIS_POST_TAG_NAME_KEY, tag.getStr("name"));
        }
        return flag;
    }


    @Override
    public PostTag findById(Object idValue) {

        if (idValue == null) {
            return null;
        }

        PostTag tag = Redis.use().hget(Constants.REDIS_POST_TAG_KEY, idValue);
        if (tag == null) {
            tag = findFirst("select * from t_post_tag where id = ? and valid = 1", idValue);
            if (tag != null) {
                Redis.use().hset(Constants.REDIS_POST_TAG_KEY, tag.getLong("id"), tag);
            }
        }
        return tag;
    }

    /**
     * 通过名称查询标签
     *
     * @param name 帖子标签名称
     * @return 返回查询结果
     */
    public Boolean findByName(String name) {

        boolean flag = Redis.use().sismember(Constants.REDIS_POST_TAG_NAME_KEY, name);
        if (!flag) {
            PostTag tag = findFirst("select * from t_post_tag where name = ? and valid = 1 ", name);
            if (tag != null) {
                Redis.use().sadd(Constants.REDIS_POST_TAG_NAME_KEY, name);
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 通过名称查询标签
     *
     * @param name 帖子标签名称
     * @return 返回查询结果
     */
    public PostTag findTagByName(String name) {

        PostTag tag = findFirst("select * from t_post_tag where name = ? and valid = 1 ", name);
        return tag;
    }

    /**
     * @return 返回所有的非系统标签
     */
    public List<Record> findList() {

        return Db.find("select id,name from t_post_tag where valid = 1 and sys = 0");
    }

    /**
     * 分页查询
     *
     * @param request 查询参数
     * @return 返回标签分页记录
     */
    public Page<PostTag> findPage(PageRequest request) {

        String sql = "select id,name,created_at ";
        String sql_ex = "from t_post_tag where valid = 1 and sys = 0 ";
        Map<String, String> params = request.getParams();
        List<String> lp = new ArrayList<>();
        if (StringUtils.isNotEmpty(params.get("name"))) {
            sql_ex += " and name like ? ";
            lp.add("%" + params.get("name") + "%");
        }
        sql_ex += " order by idx desc,created_at desc";
        return paginate(request.getPageNo(), request.getPageSize(), sql, sql_ex, lp.toArray());
    }

    /**
     * 验证标签名称
     *
     * @param name 标签名称
     */
    private void validareTagName(String name) throws BizException {

        if (StringUtils.isEmpty(name)) {
            logger.error("标签名称不能为空");
            throw new BizException(ErrorCode.POST_TAG_NAME_NULL);
        }

        if (!ValidateUtils.validateStrLen(name, Constants.POST_TAG_NAME_LEN_MIN, Constants.POST_TAG_NAME_LEN_MAX)) {
            logger.error("标签名称应该在2-6位字符");
            throw new BizException(ErrorCode.POST_TAG_NAME_ILLEGLE);
        }

    }


}
