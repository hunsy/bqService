package com.bingqiong.bq.model.comm;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by hunsy on 2017/6/30.
 */
public class AppVersion extends BaseModel<AppVersion> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final AppVersion dao = new AppVersion();
    public static final String TABLE_APP_VERSION = "t_app_version";


    @Override
    public AppVersion findById(Object idValue) {

        return findFirst("select * from t_app_version where valid = 1 and id = ?", idValue);
    }

    /**
     * 根据包名和渠道，查询
     *
     * @param aPackage
     * @param channelCode
     * @return
     */
    public Record getByNameAndChannel(String aPackage, String channelCode) {

        Record ap = Db.findFirst("select * from t_app_version" +
                " where mpackage = ? and channel_code = ? and status = 1 " +
                " order by created_at desc ", aPackage, channelCode);
        return ap;
    }

    /**
     * 获取分页
     *
     * @param pageRequest
     */
    public Page<Record> findPage(PageRequest pageRequest) {

        String sql = "select * ";
        String sql_ex = " from t_app_version " +
                "where valid = 1 ";
        sql_ex += pageRequest.getSimple();
        sql_ex += " order by updated_at desc  ";
        return Db.paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, pageRequest.getSimpleValues());
    }

    /**
     * @param version
     */
    @Before(Tx.class)
    public boolean saveVersion(AppVersion version) {
        Date date = MDateKit.getNow();
        version.set("created_at", date);
        version.set("updated_at", date);
        boolean flag = version.save();
        return flag;
    }

    /**
     * 更新
     *
     * @param version
     */
    @Before(Tx.class)
    public boolean updateVersion(AppVersion version) {
        version.set("updated_at", MDateKit.getNow());
        boolean flag = version.update();
        return flag;
    }

    @Before(Tx.class)
    public boolean deleteVersion(Long id) throws BizException {
        if (id == null) {
            throw new BizException(ErrorCode.MISSING_PARM);
        }
        AppVersion version = findFirst("select * from t_app_version where valid = 1 and id = ?", id);
        if (version == null) {
            throw new BizException(ErrorCode.APP_VERSION_NOT_EXIST);
        }
        version.set("valid", 0);
        boolean flag = version.update();
        return flag;
    }

}
