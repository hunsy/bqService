package com.bingqiong.bq.controller.admin.article;

import com.alibaba.fastjson.JSONArray;
import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.controller.admin.BaseController;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.interceptor.PageInterceptor;
import com.bingqiong.bq.model.Article;
import com.bingqiong.bq.model.ArticleType;
import com.bingqiong.bq.utils.RequestUtil;
import com.bingqiong.bq.vo.PageRequest;
import com.bingqiong.bq.vo.ResponseDataVo;
import com.bingqiong.bq.vo.ResponseEmptyVo;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import java.util.*;

/**
 * Created by hunsy on 2017/4/12.
 */
public class ArticleTypeController extends BaseController {

    /**
     * 获取文章类型列表
     */
    public void list() {
        try {
            List<String> records = ArticleType.dao.listNames();
            List<Map<String, String>> ls = new ArrayList();
            if (records != null) {
                for (String str : records) {
                    Map<String, String> m = new HashMap<>();
                    m.put("name", str);
                    m.put("value", str);
                    ls.add(m);
                }
            }
            renderJson(ResponseDataVo.success(ls));
        } catch (Exception e) {
            handleException(e, "");
        }
    }

    @Before(PageInterceptor.class)
    public void page() {
        try {
//            int page = getParaToInt("pageNo", 1);
//            int size = getParaToInt("pageSize", 10);
            PageRequest pageRequest = getAttr("pageRequest");
            logger.info("pageRequest:{}", JsonKit.toJson(pageRequest));
            String name = getPara("param_name");
            Page<Record> ps = ArticleType.dao.page(pageRequest.getPageNo(), pageRequest.getPageSize(), name);
            renderJson(ResponseDataVo.success(ps));
        } catch (Exception e) {
            handleException(e, "");
        }
    }

    /**
     * 获取文章类型详情
     * <p>
     * -> param [id]
     */
    public void get() {
        String errMsg = "";
        try {
            Long id = getParaToLong("id");
            if (id == null) {
                errMsg = "缺少参数->id";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Record record = ArticleType.dao.getById(id);
            if (record == null) {
                errMsg = "文章类型不存在!id:" + id;
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            renderJson(ResponseDataVo.success(record));
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 保存文章类型
     * <p>
     * ->param [name]
     */
    public void save() {
        String errMsg = "";
        try {
            Record record = RequestUtil.parseRecord(null, getRequest());
            if (record.get("name") == null) {
                errMsg = "缺少参数->name";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Record nn = Db.findFirst("select * from t_article_type where name = ? and valid = 1", getPara("name"));
            if (nn != null) {
                errMsg = "文章类型已经存在";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            ArticleType.dao.saveType(record);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 更新
     */
    public void update() {

        String errMsg = "";
        try {
            Long id = getParaToLong("id");
            if (id == null) {
                errMsg = "缺少参数->id";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Record zr = ArticleType.dao.getById(id);
            if (zr == null) {
                errMsg = "文章类型不存在->id:" + id;
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            Record record = RequestUtil.parseRecord(null, getRequest());
            if (record.get("name") == null) {
                errMsg = "缺少参数->name";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            if (!record.getStr("name").equals(zr.getStr("name"))) {
                Record nn = Db.findFirst("select * from t_article_type where name = ? and valid = 1", getPara("name"));
                if (nn != null) {
                    errMsg = "文章类型已经存在";
                    throw new BizException(BqErrorCode.CODE_FAILED.getCode());
                }
            }
            ArticleType.dao.updatetype(record);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 删除
     */
    public void delete() {

        String errMsg = "";
        try {
            Long id = getParaToLong("id");
            if (id == null) {
                errMsg = "缺少参数->id";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Record record = ArticleType.dao.getById(id);
            if (record == null) {
                errMsg = "文章类型不存在->id:" + id;
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            record.set("valid", 0);
            ArticleType.dao.updatetype(record);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 热点排序，资讯排序
     * 排序
     * ->param [ids]
     */
    public void sort() {
        String errMsg = "";
        try {
            String[] ids = getParaValues("ids");
            if (ids[0].indexOf(",") > 0) {
                ids = ids[0].split(",");
            }
            if (ids == null || ids.length == 0) {
                errMsg = "没有排序的文章类型";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            //更新idx字段
            for (int i = 0; i < ids.length; i++) {
                Record record = ArticleType.dao.getById(Long.parseLong(ids[i]));
                if (record == null) {
                    errMsg = "文章类型不存在->id=" + ids[i];
                    throw new BizException(BqErrorCode.CODE_FAILED.getCode());
                }
                record.set("idx", ids.length - i);
                ArticleType.dao.updatetype(record);
            }
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }


    /**
     * 上下架
     */
    public void sold() {
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

            Record type = ArticleType.dao.getById(id);
            if (type == null) {
                errMsg = "文章类型不存在";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            type.set("status", status);
            Db.update("t_article_type", type);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

}
