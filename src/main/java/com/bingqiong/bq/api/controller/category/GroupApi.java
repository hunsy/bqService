package com.bingqiong.bq.api.controller.category;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.api.interceptor.ApiAuthInterceptor;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.conf.BqCmsConf;
import com.bingqiong.bq.model.category.Group;
import com.bingqiong.bq.model.category.GroupFollows;
import com.bingqiong.bq.model.category.Plate;
import com.bingqiong.bq.model.user.User;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 圈子相关请求
 * Created by hunsy on 2017/6/25.
 */
public class GroupApi extends IBaseController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 获取板块下的圈子
     */
    @Before(PageInterceptor.class)
    public void list() {
        try {

            PageRequest pageRequest = getAttr("pageRequest");
            Map<String, String> params = pageRequest.getParams();
            params.put("status", "1");
            if (StringUtils.isEmpty(params.get("plate_id"))) {
                throw new BizException(ErrorCode.MISSING_PARM);
            }

//            User user = getAttr("bq_user");
//            if (user != null) {
//                params.put("user_id", user.getStr("user_id"));
//            }
            JSONObject object = getAttr("mobileInfo");
            //请求来自哪个平台
            String platform;
            if (object.containsKey("platform")) {
                platform = object.getString("platform");
                pageRequest.getParams().put(platform + "_show", "1");
            }

            Page<Record> page = Group.dao.findPage(pageRequest);
            renderSuccess(page, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 获取圈子详情
     */
    public void get() {

        try {

            JSONObject object = getAttr("params");
            Group group = Group.dao.findById(object.getLong("id"));
            if (group == null || group.getInt("status") == 0) {
                throw new BizException(ErrorCode.GROUP_NOT_EXIST);
            }

            User user = getAttr("bq_user");
            boolean followed = false;
            if (user != null) {
                GroupFollows gf = GroupFollows.dao.userFollowed(user.getStr("user_id"), group.getLong("id"));
                if (gf != null) {
                    followed = true;
                }
            }
            group.put("followed", followed);
            renderSuccess(group, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    /**
     * 关注
     */
    public void follow() {

        try {

            JSONObject object = getAttr("params");
            Long group_id = object.getLong("group_id");
            User user = getAttr("bq_user");
            GroupFollows groupFollows = new GroupFollows();
            groupFollows.set("group_id", group_id);
            groupFollows.set("user_id", user.getStr("user_id"));
            GroupFollows.dao.saveGroupFollows(groupFollows);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 取消关注
     */
    public void cancelfollow() {

        try {

            JSONObject object = getAttr("params");
            Long group_id = object.getLong("group_id");
            User user = getAttr("bq_user");
            GroupFollows.dao.deleteFollows(user.getStr("user_id"), group_id);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 获取所有的板块和圈子
     */
    public void all() {

        try {

            JSONObject object = getAttr("mobileInfo");
            //请求来自哪个平台
            String platform = "";
            if (object.containsKey("platform")) {
                platform = object.getString("platform");
            }
            List<Record> plates = Plate.dao.plateAndGroup(platform);
            //进行加密处理
            renderSuccess(plates, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

}
