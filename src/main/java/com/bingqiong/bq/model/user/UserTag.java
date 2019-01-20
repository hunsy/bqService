//package com.bingqiong.bq.model.user;
//
//import com.bingqiong.bq.comm.constants.Constants;
//import com.bingqiong.bq.comm.constants.ErrorCode;
//import com.bingqiong.bq.comm.exception.BizException;
//import com.bingqiong.bq.comm.utils.MDateKit;
//import com.bingqiong.bq.comm.vo.PageRequest;
//import com.bingqiong.bq.model.BaseModel;
//import com.jfinal.aop.Before;
//import com.jfinal.plugin.activerecord.Page;
//import com.jfinal.plugin.activerecord.tx.Tx;
//import com.jfinal.plugin.redis.Redis;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * 用户标签
// * Created by hunsy on 2017/6/28.
// */
//public class UserTag extends BaseModel<UserTag> {
//
//    private Logger logger = LoggerFactory.getLogger(getClass());
//    public static final UserTag dao = new UserTag();
//    public static final String TABLE_USER_TAG = "t_user_tag";
//
//    /**
//     * 新增标签
//     */
//    @Before(Tx.class)
//    public boolean saveTag(UserTag tag) throws BizException {
//
//        if (StringUtils.isEmpty(tag.getStr("name"))) {
//            logger.error("用户标签名称为空");
//            throw new BizException(ErrorCode.USER_TAG_NAME_NULL);
//        }
//
//        UserTag dbTag = findByName(tag.getStr("name"));
//        if (dbTag != null) {
//            logger.error("用户标签已存在");
//            throw new BizException(ErrorCode.USER_TAG_EXIST);
//        }
//
//        tag.set("created_at", MDateKit.getNow());
//        boolean flag = tag.save();
//        if (flag) {
//            Redis.use().hset(Constants.REDIS_USER_TAG_KEY, tag.getStr("name"), tag);
//        }
//        return flag;
//    }
//
//    /**
//     * 删除用户标签
//     *
//     * @param id
//     */
//    @Before(Tx.class)
//    public void deleteTag(Long id) throws BizException {
//
//        UserTag dbTag = findById(id);
//        if (dbTag == null) {
//            logger.error("用户标签不存在");
//            throw new BizException(ErrorCode.USER_TAG_NOT_EXIST);
//        }
//
//        String name = dbTag.getStr("name");
//        boolean flag = dbTag.delete();
//        //刪除缓存
//        if (flag) {
//            Redis.use().hdel(Constants.REDIS_USER_TAG_KEY, name);
//        }
//    }
//
//
//    @Override
//    public UserTag findById(Object idValue) {
//        if (idValue == null) {
//            return null;
//        }
//        return findFirst("select * from t_user_tag where id = ? ", idValue);
//    }
//
//    /**
//     * 通过名称查询，没有则新增
//     *
//     * @param name
//     * @return
//     */
//    public UserTag findByName(String name) throws BizException {
//
//        if (StringUtils.isEmpty(name)) {
//            return null;
//        }
//
//        UserTag tag = Redis.use().hget(Constants.REDIS_USER_TAG_KEY, name);
//        if (tag == null) {
//            tag = findFirst("select * from t_user_tag where name = ? ", name);
//            if (tag != null) {
//                Redis.use().hset(Constants.REDIS_USER_TAG_KEY, name, tag);
//            } else {
//                //不存在，则新增
//                tag = new UserTag();
//                tag.set("name", name);
//                saveTag(tag);
//                tag = findById(tag.getLong("id"));
//            }
//        }
//        return tag;
//    }
//
//    /**
//     * 分页查询
//     *
//     * @param pageRequest
//     * @return
//     */
//    public Page<UserTag> findPage(PageRequest pageRequest) {
//
//        String sql = "select * ";
//        String sql_ex = "from t_user_tag ";
//        if (pageRequest.getParams().get("name") != null) {
//            sql_ex += " where name like ? order by created_at";
//            return paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, pageRequest.getSimpleValues());
//        }
//        return paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex + "order by name asc,created_at desc");
//    }
//
//}
