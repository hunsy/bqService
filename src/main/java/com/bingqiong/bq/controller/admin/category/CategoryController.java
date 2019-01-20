package com.bingqiong.bq.controller.admin.category;

import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.controller.admin.BaseController;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.model.Category;
import com.bingqiong.bq.vo.ResponseDataVo;
import com.bingqiong.bq.vo.ResponseEmptyVo;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * 分类相关基类
 * Created by hunsy on 2017/4/25.
 */
public abstract class CategoryController extends BaseController {

    /**
     * 按主键获取详情
     */
    public void get(String type) {
        String errMsg = "";
        try {
            Category record = Category.dao.getById(getParaToLong("id"));
            Category.dao.checkCategory(record, type);
            renderJson(ResponseDataVo.success(record));
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 删除
     */
    public void delete(String type) {
        String errMsg = "";
        try {
            Category record = Category.dao.getById(getParaToLong("id"));
            //检查板块是否存在
            Category.dao.checkCategory(record, type);
            Category.dao.deleteCategory(record);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 批量删除
     * -> param [ids]
     */
    @Before(Tx.class)
    public void batchdelete(String type) {
        String errMsg = "";
        try {
            String[] ids = getParaValues("ids");
            if (ids[0].indexOf(",") > 0) {
                ids = ids[0].split(",");
            }
            if (ids == null || ids.length == 0) {
                errMsg = "没有参数ids";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            for (String id : ids) {
                Category record = Category.dao.getById(Long.parseLong(id));
                //检查圈子是否存在
                Category.dao.checkCategory(record, type);
                Category.dao.deleteCategory(record);
            }
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 排序
     * 排序
     * -> param [ids]
     */
//    @Before(Tx.class)
    public void sort(String type) {
        String errMsg = "";
        try {

            String[] ids = getParaValues("ids");
            if (ids[0].indexOf(",") > 0) {
                ids = ids[0].split(",");
            }
            if (ids == null || ids.length == 0) {
                errMsg = "没有排序内容";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            //更新idx字段
            for (int i = 0; i < ids.length; i++) {
                Category record = Category.dao.getById(Long.parseLong(ids[i]));
                //检查圈子是否存在
                Category.dao.checkCategory(record, type);
                record.set("idx", ids.length - i);
                Category.dao.updateCategory(record);
            }
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 下架/上架
     * <p>
     * ->param [id,status]
     */
    public void sold(String type) {
        String errMsg = "";
        try {
            Long id = getParaToLong("id");
            if (id == null) {
                errMsg = "缺少参数->id";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Integer status = getParaToInt("status");
            if (status == null) {
                errMsg = "缺少参数->status";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Category record = Category.dao.getById(id);
            //检查圈子是否存在
            Category.dao.checkCategory(record, type);
            record.set("status", status);
            Category.dao.updateCategory(record);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

}
