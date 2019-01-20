package com.bingqiong.bq.controller.api;

import com.alibaba.fastjson.JSONArray;
import com.bingqiong.bq.controller.admin.BaseController;
import com.bingqiong.bq.model.Banner;
import com.bingqiong.bq.utils.EncodeUtils;
import com.bingqiong.bq.vo.ResponseMobileDataVo;

/**
 * Created by hunsy on 2017/4/11.
 */
public class BannerApi extends BaseController {

    /**
     * 获取Banner列表
     */
    public void list() {
        String errMsg = "";
        try {
            String ls = Banner.dao.list();
//            if (StringUtils.isEmpty(ls)) {
//                errMsg = "没有banner";
//                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
//            }
            renderJson(ResponseMobileDataVo.success(JSONArray.parseArray(ls), EncodeUtils.isEncode()));
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }


}
