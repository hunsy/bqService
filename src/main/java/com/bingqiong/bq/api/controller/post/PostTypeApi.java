package com.bingqiong.bq.api.controller.post;

import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.conf.BqCmsConf;
import com.bingqiong.bq.model.post.PostType;
import com.jfinal.plugin.activerecord.Record;

import java.util.List;

/**
 * Created by hunsy on 2017/6/30.
 */
public class PostTypeApi extends IBaseController {


    public void list() {

        try {

            List<Record> ls = PostType.dao.findList(1);
            renderSuccess(ls, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

}
