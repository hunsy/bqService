package com.bingqiong.bq.controller.api;

import com.bingqiong.bq.utils.EncodeUtils;
import com.bingqiong.bq.vo.ResponseMobileDataVo;
import com.jfinal.core.Controller;

/**
 * 检查服务器活动接口
 * Created by hunsy on 2017/5/4.
 */
public class DomestoreApi extends Controller {

    public void isUpgrade() throws Exception {

        renderJson(ResponseMobileDataVo.success(1, EncodeUtils.isEncode()));
    }

}
