package com.bingqiong.bq.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bingqiong.bq.constant.BqConstants;
import com.bingqiong.bq.model.base.BaseAppVersion;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Redis;

/**
 * app版本管理
 * Created by hunsy on 2017/5/12.
 */
public class AppVersion extends BaseAppVersion {

    /**
     *
     */
    private static final long serialVersionUID = -7943034122376921674L;

    public static AppVersion dao = new AppVersion();

    private static final String REDIS_VERSION = BqConstants.BQ_APPLICATION + "version";
    private static final String REDIS_VERSION_NAME = REDIS_VERSION + ":name";

    /**
     * 获取id
     *
     * @param id
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public AppVersion getById(Long id) throws InstantiationException, IllegalAccessException {
        return getByIdByCache(REDIS_VERSION, id, AppVersion.class);
    }

    /**
     * @param version 版本号
     */
    public void saveApp(AppVersion version) {

        boolean flag = saveEntity(version, REDIS_VERSION);
        if (flag) {
            cacheName(version);
        }
    }

    /**
     * 更新
     *
     * @param version
     */
    public void updateVersion(AppVersion version) {

        boolean flag = updateEntity(version, REDIS_VERSION);
        if (flag) {
            cacheName(version);
        }
    }

    private void cacheName(AppVersion version) {
        String name = version.getStr("name");
        String channel_code = version.getStr("channel_code");
        Redis.use().hset(REDIS_VERSION_NAME, name + "_" + channel_code, version.getStr("version"));
    }

    public void deleteVersion(AppVersion version) {

        boolean flag = deleteEntity(version, REDIS_VERSION);
        if (flag) {
            Redis.use().hdel(REDIS_VERSION_NAME, version.getStr("name") + "_" + version.getStr("channel_code"));
        }
    }


    /**
     * 获取分页
     *
     * @param page
     * @param size
     * @param params
     */
    public String page(int page, int size, Map<String, String> params) {

        String sql = "select * ";
        String sql_ex = " from t_app_version " +
                "where valid = 1 ";
        StringBuilder sb = new StringBuilder();
        sb.append(page).append("_").append(size);
        List<Object> lp = new ArrayList<>();
        if (!params.entrySet().isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue();
                sql_ex += " and " + key + " = ? ";
                lp.add(val);
                sb.append("_").append(key).append("_").append(val);
            }
        }
        String str = getPageByCache(REDIS_VERSION, sb.toString(), lp, page, size, sql, sql_ex);
        return str;
    }


    @Override
    protected void clearCache() {
        Redis.use().del(REDIS_VERSION + REDIS_PAGE);
    }

    /**
     * 通过版本号获取。
     *
     * @param mpackage
     * @param channel_code
     * @return
     */
    public Record getByNameAndChannel(String mpackage, String channel_code) {
        Record ap = Db.findFirst("select * from t_app_version where mpackage = ? and channel_code = ?", mpackage, channel_code);
        return ap;
    }
}
