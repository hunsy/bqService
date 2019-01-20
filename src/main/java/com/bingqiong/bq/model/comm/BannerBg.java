package com.bingqiong.bq.model.comm;

import com.bingqiong.bq.comm.constants.Constants;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.exception.BizException;
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
 * banner的背景图
 * <p>
 * Created by hunsy on 2017/7/26.
 */
public class BannerBg extends BaseModel<BannerBg> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final BannerBg dao = new BannerBg();
    public static final String TABLE_BANNER_BG = "t_banner_bg";


    /**
     * 每个平台，只能保存一个背景图
     *
     * @param bg
     */
    @Before(Tx.class)
    public boolean saveBg(BannerBg bg) throws BizException {

        Integer ios_show = bg.getInt("ios_show");
        Integer android_show = bg.getInt("android_show");
        if (ios_show == null || android_show == null || (ios_show == 0 && android_show == 0)) {
            throw new BizException(ErrorCode.BANNER_BG_PLATFORM_SEL);
        }

        BannerBg dbBg;
        if (bg.getInt("ios_show") == null || bg.getInt("ios_show") == 1) {

            dbBg = getByShowType("ios");
            if (dbBg != null) {
                logger.error("ios平台背景已存在");
                throw new BizException(ErrorCode.BANNER_BG_IOS_EXIST);
            }
        }

        if (bg.getInt("android_show") == null || bg.getInt("android_show") == 1) {

            dbBg = getByShowType("android");
            if (dbBg != null) {
                logger.error("android平台背景已存在");
                throw new BizException(ErrorCode.BANNER_BG_ANDROID_EXIST);
            }
        }
        Date date = new Date();
        bg.set("created_at", date);
        bg.set("updated_at", date);
        boolean flag = bg.save();
        if (flag) {
            clearCache();
        }
        return flag;
    }


    public BannerBg getByShowType(String showType) {
        logger.info("查询->showType:{}", showType);
        BannerBg bg = Redis.use().get(Constants.REDIS_BANNER_BG_KEY + showType);
        if (bg == null) {
            String sql = "select * from t_banner_bg where valid = 1 ";
            if (StringUtils.isNotEmpty(showType)) {
                sql = andPlatform(sql, showType);
            }
            logger.info("sql->sql:{}", sql);

            bg = findFirst(sql);
            if (bg != null) {
                Redis.use().set(Constants.REDIS_BANNER_BG_KEY + showType, bg);
            }
        }
        return bg;
    }

    /**
     * 更新
     *
     * @param bg
     */
    @Before(Tx.class)
    public void updateBg(BannerBg bg) throws BizException {
        Integer ios_show = bg.getInt("ios_show");
        Integer android_show = bg.getInt("android_show");
        if (ios_show == null || android_show == null || (ios_show == 0 && android_show == 0)) {
            throw new BizException(ErrorCode.BANNER_BG_PLATFORM_SEL);
        }

        BannerBg dbBg = findById(bg.getLong("id"));
        if (dbBg == null) {
            throw new BizException(ErrorCode.BANNER_BG_NOT_EXIST);
        }

        if (dbBg.getInt("ios_show") == 0 && (bg.getInt("ios_show") == null || bg.getInt("ios_show") == 1)) {
            BannerBg iosDbBg = getByShowType("ios");
            if (iosDbBg != null) {
                logger.error("ios平台背景已存在");
                throw new BizException(ErrorCode.BANNER_BG_IOS_EXIST);
            }
        }

        if (dbBg.getInt("android_show") == 0 && (bg.getInt("android_show") == null || bg.getInt("android_show") == 1)) {
            BannerBg androidDbBg = getByShowType("android");
            if (androidDbBg != null) {
                logger.error("android平台背景已存在");
                throw new BizException(ErrorCode.BANNER_BG_ANDROID_EXIST);
            }
        }

        bg.set("updated_at", new Date());
        boolean flag = bg.update();
        if (flag) {
            clearCache();
        }
    }

    /**
     * @param id
     */
    @Before(Tx.class)
    public void deleteBg(Long id) throws BizException {

        BannerBg dbBg = findById(id);
        if (dbBg == null) {
            throw new BizException(ErrorCode.BANNER_BG_NOT_EXIST);
        }
        dbBg.set("valid", 0);
        boolean flag = dbBg.update();
        if (flag) {
            clearCache();
        }

    }

    @Override
    public BannerBg findById(Object idValue) {

        return findFirst("select * from t_banner_bg where valid = 1 and id = ?", idValue);
    }

    private void clearCache() {

        Redis.use().del(Constants.REDIS_BANNER_BG_KEY + "ios");
        Redis.use().del(Constants.REDIS_BANNER_BG_KEY + "android");
    }

    public Page<Record> findPage(PageRequest pageRequest) {

        String sql = "select * ";
        String sql_ex = " from t_banner_bg where valid = 1 ";

        Map<String, String> params = pageRequest.getParams();
        List<Object> lp = new ArrayList<>();

        if (StringUtils.isNotEmpty(params.get("name"))) {
            sql_ex += " and name like ? ";
            lp.add("%" + params.get("name") + "%");
        }

        if (StringUtils.isNotEmpty(params.get("ios_show"))) {
            sql_ex += " and ios_show = ? ";
            lp.add(params.get("ios_show"));
        }

        if (StringUtils.isNotEmpty(params.get("android_show"))) {
            sql_ex += " and android_show = ? ";
            lp.add(params.get("android_show"));
        }

        sql_ex += " order by created_at desc";
        return Db.paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, lp.toArray());
    }
}
