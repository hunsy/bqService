package com.bingqiong.bq.api.controller.comm;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.conf.BqCmsConf;
import com.bingqiong.bq.model.comm.AppVersion;
import com.jfinal.plugin.activerecord.Record;

/**
 * 版本检查
 * Created by hunsy on 2017/6/30.
 */
public class AppVersionApi extends IBaseController {

    /**
     * 检查版本
     */
    public void check() {

        try {
            JSONObject obj = getAttr("params");

            if (obj.getString("package") == null
                    || obj.getString("channelCode") == null
                    || obj.getString("versionName") == null
                    || obj.getInteger("versionCode") == null) {
                throw new BizException(ErrorCode.MISSING_PARM);
            }

            Record cache = AppVersion.dao.getByNameAndChannel(obj.getString("package"), obj.getString("channelCode"));

            if (cache == null) {
                throw new BizException(ErrorCode.APP_VERSION_NOT_EXIST);
            }

            boolean hasUpdate = false;
            if (obj.getIntValue("versionCode") < Integer.valueOf(cache.getStr("version_code"))) {
                hasUpdate = true;
            } else {

                String versionName = cache.getStr("version_name");
                String vn = obj.getString("versionName");
                if (!versionName.equals(vn)) {
                    hasUpdate = true;
                }
            }
            cache.set("hasUpdate", hasUpdate);
            renderSuccess(cache, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }


}
