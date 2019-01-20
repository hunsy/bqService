package com.bingqiong.bq.controller.admin.category;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.interceptor.PageInterceptor;
import com.bingqiong.bq.model.Category;
import com.bingqiong.bq.utils.RequestUtil;
import com.bingqiong.bq.vo.PageRequest;
import com.bingqiong.bq.vo.ResponseDataVo;
import com.bingqiong.bq.vo.ResponseEmptyVo;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * 提供后台管理的group相关服务。
 * <p>
 * Created by hunsy on 2017/4/7.
 */
public class GroupController extends CategoryController {

    private static final String TYPE_GROUP = "group";

    /**
     * 按主键获取详情
     */
    public void get() {
        get(TYPE_GROUP);
    }

    /**
     * 获取分页
     */
    @Before(PageInterceptor.class)
    public void page() {
        String errMsg = "";
        try {
            Long plate_id = getParaToLong(-1);
            if (plate_id == null) {
                errMsg = "缺少路径参数->plate_id";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            // 检查板块是否存在
            Category plate = Category.dao.getById(plate_id);
            Category.dao.checkCategory(plate, "plate");

//            int page = getParaToInt("pageNo", 1);
//            int size = getParaToInt("pageSize", 10);
            PageRequest pageRequest = getAttr("pageRequest");
            logger.info("pageRequest:{}", JsonKit.toJson(pageRequest));
            Map<String, String> params = RequestUtil.getParams(getRequest());
            String ps = Category.dao.groupPage(plate_id, pageRequest.getPageNo(), pageRequest.getPageSize(), params);
            renderJson(ResponseDataVo.success(JSONObject.parseObject(ps)));
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 新增圈子 -> param [parent_id,name,status:1,thumb_url]
     */
    public void save() {
        String errMsg = "";
        try {
            Category category = getModel(Category.class);

            if (category.getStr("name") == null) {
                errMsg = "缺少参数->name";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            if (category.getStr("name").length() > 6){
                errMsg = "圈子名称(name)最多6个字符";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Long plate_id = category.getLong("parent_id");
            // 检查板块是否存在
            Category plate = Category.dao.getById(plate_id);
            Category.dao.checkCategory(plate, "plate");

            category.set("type", TYPE_GROUP);
            Category.dao.saveCategory(category);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 更新圈子 -> param [id,parent_id,name,status:1,thumb_url]
     */
    public void update() {
        String errMsg = "";
        try {
            Category record = getModel(Category.class);
            if (record.getLong("id") == null) {
                errMsg = "缺少参数->id";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            if (record.getStr("name") == null) {
                errMsg = "缺少参数->name";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            if (record.getStr("name").length() > 6){
                errMsg = "圈子名称(name)最多6个字符";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            if (record.get("parent_id") != null) {
                Long plate_id = record.getLong("parent_id");
                // 检查板块是否存在
                Category plate = Category.dao.getById(plate_id);
                Category.dao.checkCategory(plate, "plate");
            }

            Category r = Category.dao.getById(record.getLong("id"));
            Category.dao.checkCategory(r, TYPE_GROUP);
            Category.dao.updateCategory(record);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 删除 -> param [id]
     */
    public void delete() {
        delete(TYPE_GROUP);
    }

    /**
     * 批量删除 -> param [ids]
     */
    @Before(Tx.class)
    public void batchdelete() {
        batchdelete(TYPE_GROUP);
    }

    /**
     * 圈子排序 排序 -> param [ids]
     */
    @Before(Tx.class)
    public void sort() {
        sort(TYPE_GROUP);
    }

    /**
     * 圈子下架/上架
     * <p>
     * ->param [id,status]
     */
    public void sold() {
        sold(TYPE_GROUP);
    }

}
