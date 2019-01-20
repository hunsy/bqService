package com.bingqiong.bq.cms.controller.comm;

import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.comm.FeedBack;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;

/**
 * 反馈
 * Created by hunsy on 2017/10/13.
 */
public class FeedbackController extends IBaseController {

    /**
     * 分页数据
     */
    @Before(PageInterceptor.class)
    public void page() {

        try {
            PageRequest pageRequest = getAttr("pageRequest");
            Page<FeedBack> feedBackPage = FeedBack.dao.findPage(pageRequest);
            renderSuccess(feedBackPage);
        } catch (Exception e) {
            renderFailure(e);
        }
    }


}
