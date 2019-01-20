package com.bingqiong.bq.model.comm;

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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 轮播图
 * <p>
 * Created by hunsy on 2017/6/26.
 */
public class Banner extends BaseModel<Banner> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final Banner dao = new Banner();
    public static final String TABLE_BANNER = "t_banner";
    private static String[] bannerTypes = new String[]{"帖子", "圈子", "外链"};

    /**
     * 新增banner
     *
     * @param banner banner记录
     * @return 返回保存banner的结果
     */
    @Before(Tx.class)
    public boolean saveBanner(Banner banner) throws BizException {

        if (StringUtils.isEmpty(banner.getStr("thumb_url")) || StringUtils.isEmpty(banner.getStr("remark"))) {

            logger.error("缺少参数thumb_url|remark");
            throw new BizException(ErrorCode.MISSING_PARM);
        }

        if (!ArrayUtils.contains(bannerTypes, banner.getStr("banner_type"))) {
            logger.error("Banner类型不存在");
            throw new BizException(ErrorCode.BANNER_TYPE_NOT_EXSIT);
        }

        Date date = MDateKit.getNow();
        banner.set("created_at", date);
        banner.set("updated_at", date);
        boolean flag = banner.save();
        if (flag) {
            removeListCache();
        }
        return flag;
    }

    /**
     * 更新Banner
     *
     * @param banner 更新Banner
     * @return 返回更新结果
     */
    @Before(Tx.class)
    public boolean updateBanner(Banner banner) throws BizException {

        Banner dbBanner = findById(banner.getLong("id"));
        if (dbBanner == null) {
            logger.error("banner不存在");
            throw new BizException(ErrorCode.BANNER_NOT_EXIST);
        }

        if (StringUtils.isEmpty(banner.getStr("thumb_url")) || StringUtils.isEmpty("remark")) {

            logger.error("缺少参数thumb_url|remark");
            throw new BizException(ErrorCode.MISSING_PARM);
        }

        if (!banner.getStr("banner_type").equals(dbBanner.getStr("banner_type"))) {
            if (!ArrayUtils.contains(bannerTypes, banner.getStr("banner_type"))) {
                logger.error("Banner类型不存在");
                throw new BizException(ErrorCode.BANNER_TYPE_NOT_EXSIT);
            }
        }

        if (StringUtils.isEmpty(banner.getStr("remark"))) {

            throw new BizException(ErrorCode.BANNER_REMARK_NOT_NULL);
        }

        Date date = MDateKit.getNow();
        banner.set("updated_at", date);
        boolean flag = banner.update();
        if (flag) {
            removeListCache();
        }
        return flag;
    }

    /**
     * 删除Banner
     *
     * @param id banner的ID
     * @return 返回删除结果
     */
    public boolean deleteBanner(Long id) throws BizException {

        if (id == null) {
            logger.error("Banner不存在");
            throw new BizException(ErrorCode.BANNER_NOT_EXIST);
        }

        Banner dbBanner = findById(id);
        return deleteBanner(dbBanner);
    }

    /**
     * 删除Banner
     *
     * @param banner banner记录
     * @return 返回删除结果
     * @throws BizException
     */
    @Before(Tx.class)
    private boolean deleteBanner(Banner banner) throws BizException {

        if (banner == null) {
            logger.error("Banner不存在");
            throw new BizException(ErrorCode.BANNER_NOT_EXIST);
        }

        banner.set("valid", 0);
        banner.set("updated_at", MDateKit.getNow());
        boolean flag = banner.update();
        if (flag) {
            removeListCache();
        }
        return flag;
    }

    @Override
    public Banner findById(Object idValue) {

        return findFirst("select * from t_banner where valid = 1 and id = ?", idValue);
    }

    /**
     * 分页查询
     * <p>
     * <p>
     * 1.7-26新增android_show & ios_show字段。用于识别Banner在那个平台展示。
     * </p>
     *
     * @param pageRequest 请求参数
     * @return 返回获取banner分页数据
     */
    public Page<Record> findPage(PageRequest pageRequest) {

        String sql = "select id,name,banner_type,remark,thumb_url,created_at,updated_at,status," +
                "android_show,ios_show ";
        String sql_ex = "from t_banner " +
                "where valid = 1 ";

//        if (StringUtils.isNotEmpty(pageRequest.getSimple())) {
//            sql_ex += pageRequest.getSimple();
//        }
        Map<String, String> params = pageRequest.getParams();
        List<String> lp = new ArrayList<>();
        if (StringUtils.isNotEmpty(params.get("name"))) {
            sql_ex += " and name like ? ";
            lp.add("%" + params.get("name") + "%");
        }

        if (StringUtils.isNotEmpty(params.get("id"))) {
            sql_ex += " and id = ? ";
            lp.add(params.get("id"));
        }

        if (StringUtils.isNotEmpty(params.get("status"))) {
            sql_ex += " and status = ? ";
            lp.add(params.get("status"));
        }

        if (StringUtils.isNotEmpty(params.get("ios_show"))) {
            sql_ex += " and ios_show = ? ";
            lp.add(params.get("ios_show"));
        }

        if (StringUtils.isNotEmpty(params.get("android_show"))) {
            sql_ex += " and android_show = ? ";
            lp.add(params.get("android_show"));
        }

        sql_ex += " order by idx desc,created_at desc";
        return Db.paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, lp.toArray());
    }


    /**
     * 获取所有上架的banner
     * <p>
     * <p>
     * 1.7-26,新增查询参数showType。参数值为ios|android,是从请求header中的platform获取值
     * </p>
     *
     * @param showType android ios
     * @return 返回ios|Android上架Banner列表
     */
    public List<Record> findList(String showType) {

        List<Record> records = Redis.use().get(Constants.REDIS_BANNER_LIST_KEY + showType);
        if (records == null) {
            String sql = "select id,name,banner_type,remark,thumb_url from t_banner " +
                    "where valid = 1 and status = 1 ";

            //--------------------------
            sql = andPlatform(sql, showType);

            sql += " order by idx desc,created_at desc";

            records = Db.find(sql);
            if (records != null) {
                Redis.use().set(Constants.REDIS_BANNER_LIST_KEY + showType, records);
            }
        }
//        logger.info("查询banner列表:{}", JsonKit.toJson(records));
        return records;
    }

    /**
     * 清除列表缓存
     */
    private void removeListCache() {
        Redis.use().del(Constants.REDIS_BANNER_LIST_KEY);
        Redis.use().del(Constants.REDIS_BANNER_LIST_KEY + "ios");
        Redis.use().del(Constants.REDIS_BANNER_LIST_KEY + "android");
    }

}
