package com.bingqiong.bq.cms.controller.user;

import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.user.UserCard;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * Created by hunsy on 2017/7/10.
 */
public class CardController extends IBaseController {

    /**
     * 分页
     */
    @Before(PageInterceptor.class)
    public void page() {

        try {
            PageRequest pageRequest = getAttr("pageRequest");
            Page<Record> page = UserCard.dao.findPage(pageRequest);
            renderSuccess(page);
        } catch (Exception e) {
            renderFailure(e);
        }
    }


}
