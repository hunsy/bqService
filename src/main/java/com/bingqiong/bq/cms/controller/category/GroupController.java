package com.bingqiong.bq.cms.controller.category;

import com.alibaba.fastjson.JSON;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.category.Group;
import com.bingqiong.bq.model.category.Plate;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 圈子请求
 * Created by hunsy on 2017/6/21.
 */
public class GroupController extends IBaseController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void list() {

        try {

            Long plate_id = getParaToLong("plate_id");
            List<Record> records = Group.dao.findList(plate_id);
            renderSuccess(records);
        } catch (Exception e) {

            renderFailure(e);
        }
    }

    /**
     * 分页查询
     */
    @Before(PageInterceptor.class)
    public void page() {

        try {
            Long plate_id = getParaToLong(-1);
            PageRequest pageRequest = getAttr("pageRequest");
            if (plate_id != null)
                pageRequest.getParams().put("plate_id", plate_id.toString());
            logger.info(JsonKit.toJson(pageRequest));
            Page<Record> pd = Group.dao.findPage(pageRequest);
            renderSuccess(pd);
        } catch (Exception e) {
            renderJson(handleException(e));
        }
    }

    /**
     * 保存
     */
    public void save() {

        try {
            Group group = new Group();
            group.set("plate_id", getParaToLong("parent_id"));
            group.set("name", getPara("name"));
            group.set("status", getParaToInt("status"));
            group.set("thumb_url", getPara("thumb_url"));
            logger.info("group:{}", JSON.toJSONString(group));
            Group.dao.saveGroup(group);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 更新
     */
    public void update() {

        try {
            Group group = new Group();
            group.set("id", getParaToLong("id"));
            group.set("plate_id", getParaToLong("parent_id"));
            group.set("name", getPara("name"));
            group.set("status", getParaToInt("status"));
            group.set("thumb_url", getPara("thumb_url"));
            logger.info("group:{}", JSON.toJSONString(group));
            Group.dao.updateGroup(group);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 删除板块
     */
    public void delete() {

        try {
            Long id = getParaToLong(-1);
            Group.dao.deleteGroup(id);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 批量删除
     */
    public void batchdelete() {

        try {
            String[] ids = getParaValues("ids");
            List<Group> groups = findListByIds(ids);
            for (Group group : groups) {
                Group.dao.deleteGroup(group);
            }
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 排序
     */
    public void sort() {

        try {
            String[] ids = getParaValues("ids");
            List<Group> groups = findListByIds(ids);
            //倒序
            for (int i = 0; i < groups.size(); i++) {
                Group group = groups.get(i);
                group.set("idx", groups.size() - i);
                Group.dao.updateGroup(group);
            }
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 板块上下架
     */
    public void sold() {

        try {
            Long id = getParaToLong("id");
            Integer status = getParaToInt("status");
            if (id == null || status == null) {
                logger.error("缺少参数");
                throw new BizException(ErrorCode.MISSING_PARM);
            }

            Group group = Group.dao.findById(id);
            if (group == null) {
                logger.error("圈子不存在->id:{}", id);
                throw new BizException(ErrorCode.PLATE_NOT_EXIST);
            }

            group.set("status", status);
            Group.dao.updateGroup(group);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }

    }

    /**
     * 将圈子转移到其他版块下。
     */
    public void move() {

        try {

            Long id = getParaToLong("id");
            Long plate_id = getParaToLong("plate_id");
            if (id == null || plate_id == null) {
                logger.error("缺少参数id|plate_id");
                throw new BizException(ErrorCode.MISSING_PARM);
            }

            Group.dao.move(id,plate_id);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    /**
     * 根据id，获取板块列表
     *
     * @param ids
     * @return
     * @throws BizException
     */
    private List<Group> findListByIds(String[] ids) throws BizException {
        if (ids[0].indexOf(",") > 0) {
            ids = ids[0].split(",");
        }
        if (ids == null || ids.length == 0) {
            logger.error("缺少参数ids");
            throw new BizException(ErrorCode.MISSING_PARM);
        }
        //遍历查询
        //所有的板块都存在时，才进行遍历删除
        List<Group> groups = new ArrayList<Group>();
        for (String id : ids) {
            Group group = Group.dao.findById(Long.parseLong(id));
            if (group == null) {
                logger.error("圈子不存在->id:{}", id);
                throw new BizException(ErrorCode.GROUP_NOT_EXIST);
            }
            groups.add(group);
        }
        return groups;
    }

}
