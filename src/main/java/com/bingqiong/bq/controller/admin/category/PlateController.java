package com.bingqiong.bq.controller.admin.category;

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

import java.util.Map;

/**
 * 提供后台管理的plate相关服务。
 * <p>
 * Created by hunsy on 2017/4/7.
 */
public class PlateController extends CategoryController {

    private static final String TYPE_PLATE = "plate";

    /**
     * 按主键获取详情
     */
    public void get() {
        get(TYPE_PLATE);
    }

    /**
     * 获取分页
     */
    @Before(PageInterceptor.class)
    public void page() {
        try {
//            int page = getParaToInt("pageNo", 1);
//            int size = getParaToInt("pageSize", 10);
            PageRequest pageRequest = getAttr("pageRequest");
            logger.info("pageRequest:{}", JsonKit.toJson(pageRequest));
            Map<String, String> params = RequestUtil.getParams(getRequest());
            String ps = Category.dao.platePage(pageRequest.getPageNo(), pageRequest.getPageSize(), params);
            renderJson(ResponseDataVo.success(JSONObject.parseObject(ps)));
        } catch (Exception e) {
            handleException(e, "");
        }
    }

    /**
     * 新增板块
     */
    public void save() {
        String errMsg = "";
        try {
            Category record = getModel(Category.class);
            if (record.get("name") == null || record.getStr("name").length() > 6) {
                errMsg = "缺少参数->name|过长";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            record.set("type", "plate");
            Category.dao.saveCategory(record);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 更新板块
     */
    public void update() {
        String errMsg = "";
        try {
            Category record = getModel(Category.class);
            if (record.getLong("id") == null) {
                errMsg = "编辑板块不存在->id:" + getPara("id");
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            if (record.getStr("name") != null || record.getStr("name").length() > 6){
                errMsg = "缺少参数->name|过长";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Category r = Category.dao.getById(record.getLong("id"));
            Category.dao.checkCategory(r, TYPE_PLATE);
            Category.dao.updateCategory(record);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 删除
     */
    public void delete() {
        delete(TYPE_PLATE);
    }

    /**
     * 批量删除
     */
    public void batchdelete() {
        batchdelete(TYPE_PLATE);
    }

    /**
     * 板块排序
     * 排序
     */
    public void sort() {
        sort(TYPE_PLATE);
    }

    /**
     * 板块下架/上架
     * <p>
     * ->param [id,status]
     */
    public void sold() {
        sold(TYPE_PLATE);
    }

}
