package com.bingqiong.bq.api.controller.comm;

import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.conf.BqCmsConf;

/**
 * 检查服务器活动接口
 * Created by hunsy on 2017/5/4.
 */
public class DomestoreApi extends IBaseController {

    public void isUpgrade() throws Exception {

        renderSuccess(1, BqCmsConf.enc);
    }

}
