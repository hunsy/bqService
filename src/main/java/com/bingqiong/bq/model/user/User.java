package com.bingqiong.bq.model.user;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.comm.constants.Constants;
import com.bingqiong.bq.comm.http.UcApi;
import com.bingqiong.bq.comm.utils.MDateKit;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.BaseModel;
import com.bingqiong.bq.model.comm.Sensitive;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Redis;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 冰趣用户
 * <p>
 * Created by hunsy on 2017/6/23.
 */
public class  User extends BaseModel<User> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final User dao = new User();
    public static final String TABLE_USER = "t_user";


    /**
     * 管理员用户，不向uc请求。
     * 仅仅保存在冰趣的用户系统中。
     * 与domestore的账号一一对应。
     *
     * @param user 用户记录（管理员）
     * @return 保存结果
     */
    @Before(Tx.class)
    public boolean saveAdmin(User user) {

        Date date = MDateKit.getNow();
        user.set("created_at", date);
        user.set("updated_at", date);
        boolean flag = user.save();
        logger.info("新增管理员成功");
        return flag;
    }

    /**
     * 保存冰趣用户
     *
     * @param user 普通用户
     * @return 返回保存结果
     */
    @Before(Tx.class)
    public boolean saveUser(User user) throws Exception {

        Date date = MDateKit.getNow();
        user.set("created_at", date);
        user.set("updated_at", date);
        boolean flag = user.save();
        if (flag) {
            user = findFirst("select * from t_user where user_id = ? ", user.getStr("user_id"));
            Redis.use().hset(Constants.REDIS_USER_KEY, user.getStr("user_id"), user);
        }
        return flag;
    }

    /**
     * 更新用户
     *
     * @param user 更新记录
     * @return 更新结果
     */
    @Before(Tx.class)
    public boolean updateUser(User user) throws Exception {

        user.set("updated_at", MDateKit.getNow());
        boolean flag = user.update();
        if (flag) {
            user = findFirst("select * from t_user where user_id = ? and valid = 1", user.getStr("user_id"));
            Redis.use().hset(Constants.REDIS_USER_KEY, user.getStr("user_id"), user);
        }
        return flag;
    }

    /**
     * 通过user_id查询。
     *
     * @param user_id 用户id
     * @return 返回用户记录
     */
    public User findByUserId(String user_id) throws Exception {

        User user = Redis.use().hget(Constants.REDIS_USER_KEY, user_id.toString());
        if (user == null) {
            user = findFirst("select * from t_user where user_id = ? and valid = 1", user_id);
            if (user != null) {
                Redis.use().hset(Constants.REDIS_USER_KEY, user.getStr("user_id"), user);
            }
        }
        if (user != null && !user.getStr("user_id").equals("sys_000001")){
            user.set("user_name", Sensitive.dao.filterSensitive(user.getStr("user_name")));
        }
        return user;
    }


    public void findByUc(String userId) throws Exception {
        //向uc确认是否存在用户
        JSONObject object = new JSONObject();
        object.put("userId", userId);
        JSONObject resp = UcApi.getInstance().doreq(object, "getUserById");
        if (resp.get("code").equals("0")) {
            JSONObject data = resp.getJSONObject("data");
            User user = findByUserId(userId);
            if (user == null) {
                //保存用户信息
                user = new User();
                user.set("user_name", data.getString("domeUserName"));
                user.set("user_id", userId);
                user.set("gender", data.getString("gender"));
                user.set("mobile", data.getString("mobile"));
                user.set("avatar_url", data.getString("avatar"));
                user.set("age", data.getIntValue("age"));
                User.dao.saveUser(user);
            } else {
                user.set("user_name", data.getString("domeUserName"));
                user.set("gender", data.getString("gender"));
                user.set("mobile", data.getString("mobile"));
                user.set("avatar_url", data.getString("avatar"));
                user.set("age", data.getIntValue("age"));
                updateUser(user);
            }
        }
    }

    /**
     * 更新用户圈子的关注数
     *
     * @param user_id 用户ID
     * @param i       更新步数 +1 -1
     * @return 返回更新结果
     */

    public boolean updateGroups(String user_id, int i) throws Exception {

        User user = findByUserId(user_id);
        user.set("groups", user.getInt("groups") + i);
        return updateUser(user);
    }

    /**
     * 查询当前用户的粉丝数（当前用户作为被关注人）
     *
     * @param pageRequest 请求参数
     * @return 返回分页数据
     */
    public Page<Record> findFans(PageRequest pageRequest) throws IOException {

        String sql = "select tu.user_id,tu.user_name,tu.avatar_url,tu.age,tu.gender ";
        String sql_ex = "from t_user tu " +
                "left join t_user_follows tuf on tu.user_id = tuf.user_id " +
                "where 1=1 ";

        Map<String, String> params = pageRequest.getParams();
        List<Object> lp = new ArrayList<>();
        if (!params.keySet().isEmpty()) {
            //当前用户
            String user_id = params.get("user_id");
            if (StringUtils.isNotEmpty(user_id)) {
                sql_ex += " and tuf.followed_id = ? ";
                lp.add(user_id);
            }
        }
        sql_ex += "order by tuf.created_at desc";
        Page<Record> page = Db.paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, lp.toArray());

        if (page.getList() != null) {

            for (Record record : page.getList()) {
                record.set("user_name", Sensitive.dao.filterSensitive(record.getStr("user_name")));
            }
        }

        return page;
    }


    /**
     * 查询当前用户的关注用户
     *
     * @param pageRequest 请求参数
     * @return 返回请求分页记录
     */
    public Page<Record> findFollows(PageRequest pageRequest) throws IOException {

        String sql = "select tu.user_id,tu.user_name,tu.avatar_url,tu.age,tu.gender ";
        String sql_ex = "from t_user tu " +
                "left join t_user_follows tuf on tu.user_id = tuf.followed_id " +
                "where 1 = 1 ";

        Map<String, String> params = pageRequest.getParams();
        List<Object> lp = new ArrayList<>();
        if (!params.keySet().isEmpty()) {
            //当前用户
            String user_id = params.get("user_id");
            if (StringUtils.isNotEmpty(user_id)) {
                sql_ex += " and tuf.user_id = ? ";
                lp.add(user_id);
            }
        }
        sql_ex += "order by tuf.created_at desc";
        Page<Record> page = Db.paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, lp.toArray());
        if (page.getList() != null) {

            for (Record record : page.getList()) {
                record.set("user_name", Sensitive.dao.filterSensitive(record.getStr("user_name")));
            }
        }
        return page;
    }


    /**
     * 更新用户的关注人数
     *
     * @param user_id 用户id
     * @param i       步数 +1 -1
     */
    void updateFollows(String user_id, int i) throws Exception {
        User user = findByUserId(user_id);
        Long count = Db.queryLong("select count(id) from t_user_follows where user_id = ?", user_id);
        user.set("follows", count);
        updateUser(user);
    }

    /**
     * 更新用户的被关注人数
     *
     * @param followed_id 被关注的用户id
     * @param i           步数
     */
    void updateFolloweds(String followed_id, int i) throws Exception {
        User user = findByUserId(followed_id);

        Long count = Db.queryLong("select count(id) from t_user_follows where user_id = ?", followed_id);
        user.set("followeds", count);
        updateUser(user);
    }
}
