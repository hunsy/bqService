package com.bingqiong.bq.api.controller.comm;

import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.conf.BqCmsConf;
import com.bingqiong.bq.model.comm.SysMsg;
import com.bingqiong.bq.model.user.User;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;

/**
 * Created by hunsy on 2017/7/8.
 */
public class SysMsgApi extends IBaseController {


    @Before(PageInterceptor.class)
    public void page() {

        try {

            PageRequest pageRequest = getAttr("pageRequest");
            User user = getAttr("bq_user");
            if (user != null) {
                pageRequest.getParams().put("user_id", user.getStr("user_id"));
            }
            pageRequest.setPageSize(100000000);

            String deviceId = getRequest().getHeader("deviceId");
            pageRequest.getParams().put("device_id", deviceId);
            Page<SysMsg> msg = SysMsg.dao.findPage(pageRequest);
            renderSuccess(msg, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

}
