package com.bingqiong.bq.controller.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.controller.admin.category.GroupController;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.model.Category;
import com.bingqiong.bq.model.UserFollow;
import com.bingqiong.bq.utils.EncodeUtils;
import com.bingqiong.bq.utils.RequestUtil;
import com.bingqiong.bq.vo.ResponseEmptyVo;
import com.bingqiong.bq.vo.ResponseMobileDataVo;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 圈子相关的服务
 * Created by hunsy on 2017/4/11.
 */
public class GroupApi extends GroupController {

    /**
     * 圈子列表
     */
    public void list() {
        String errMsg = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());

            int page = obj.getInteger("pageNo") == null ? 1 : obj.getIntValue("pageNo");
            int size = obj.getInteger("pageSize") == null ? 10 : obj.getIntValue("pageSize");
            String name = obj.getString("name");
            String user_id = obj.getString("user_id");
            if (name == null) {
                errMsg = "没有查询参数";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Page<Record> ls = Category.dao.getListByName(page,size,name,user_id);
            renderJson(ResponseMobileDataVo.success(ls.getList(), EncodeUtils.isEncode()));
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    @Override
    public void get() {
        String errMsg = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            Long id = obj.getLong("id");
            if (id == null) {
                errMsg = "获取圈子详情，缺少参数->id";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            Category record = Category.dao.getById(id);
            if (record == null || !record.getStr("type").equals("group")) {
                errMsg = "圈子不存在，id:" + id;
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            String user_id = obj.getString("user_id");
            boolean followed = false;
            if (StringUtils.isNotEmpty(user_id)) {
                Record r = Db.findFirst("select * from t_user_follow where user_id = ? and group_id = ?", user_id, id);
                if (r != null) {
                    followed = true;
                }
            }

            Record rt = new Record();
            rt.set("id", record.get("id"));
            rt.set("name", record.get("name"));
            rt.set("thumb_url", record.get("thumb_url"));
            Record stat = Category.dao.getStat(id);
            rt.set("post_num", stat.get("children_num"));
            rt.set("follows", stat.get("matter"));
            rt.set("followed", followed);
            if (getPara("user_id") != null) {
                Record follow = UserFollow.dao.findByUserAndGroup(id, getPara("user_id"));
                if (follow != null) {
                    rt.set("followed", 1);
                }
            }
            renderJson(ResponseMobileDataVo.success(JSONObject.parseObject(JsonKit.toJson(rt)), EncodeUtils.isEncode()));
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }


    /**
     * 取随机三个圈子
     */
    public void random() {
        String errMsg = "";
        try {

            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            String user_id = obj.getString("user_id");
            List<Record> records = Category.dao.randomGroup(3, user_id);
//            if (CollectionUtils.isEmpty(records)) {
//                errMsg = "没有圈子";
//                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
//            }
            renderJson(ResponseMobileDataVo.success(JSONArray.parseArray(JsonKit.toJson(records)), EncodeUtils.isEncode()));
        } catch (Exception e) {
            handleException(e, errMsg);
        }

    }

    /**
     * 关注圈子
     * -> param [group_id,user_id,user_name]
     */
    public void follow() {
        String errMsg = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            Long group_id = obj.getLong("group_id");
            String user_id = obj.getString("user_id");
            if (group_id == null || user_id == null) {
                errMsg = "缺少参数";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Category group = Category.dao.getById(group_id);

            if (group == null
                    || Long.parseLong(group.get("parent_id").toString()) == 0
                    || Integer.parseInt(group.get("status").toString()) == 0) {
                errMsg = "圈子不存在->id:" + getPara("group_id");
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Record record = new Record()
                    .set("user_id", user_id)
                    .set("group_id", group_id)
                    .set("user_name", obj.getString("user_name"));
            UserFollow.dao.saveFollow(record);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }


    /**
     * 取消圈子关注
     * -> param [group_id,user_id]
     */
    public void cancelfollow() {
        String errMsg = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            Long group_id = obj.getLong("group_id");
            String user_id = obj.getString("user_id");
            if (user_id == null || group_id == null) {
                errMsg = "缺少参数";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Record record = UserFollow.dao.findByUserAndGroup(group_id, user_id);
            if (record == null) {
                errMsg = "不存在关注记录";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            UserFollow.dao.deleteFollow(record);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 我的圈子
     * -> param [user_id]
     */
    public void mygroup() {
        String errMsg = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            String user_id = obj.getString("user_id");
            if (user_id == null) {
                errMsg = "缺少参数";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            List<Record> myGroups = Category.dao.getByUser(user_id);
//            if (CollectionUtils.isEmpty(myGroups)) {
//                errMsg = "没有圈子";
//                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
//            }
            renderJson(ResponseMobileDataVo.success(myGroups, EncodeUtils.isEncode()));
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 所有的圈子。
     */
    public void all() {
        String errMsg = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            String user_id = obj.getString("user_id");
            List<Record> plates = Category.dao.all(user_id);
//            if (CollectionUtils.isEmpty(plates)) {
//                errMsg = "没有圈子";
//                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
//            }
            renderJson(ResponseMobileDataVo.success(plates, EncodeUtils.isEncode()));
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }
}
