package com.bingqiong.bq.controller.admin.article;

import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.controller.admin.BaseController;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.model.Article;
import com.bingqiong.bq.vo.ResponseDataVo;
import com.bingqiong.bq.vo.ResponseEmptyVo;

/**
 * Created by hunsy on 2017/4/25.
 */
public class BaseArticleController extends BaseController {

    public void get(String type) {
        try {
            Article record = Article.dao.getById(getParaToLong("id"));
            Article.dao.checkArticle(record, type);
            renderJson(ResponseDataVo.success(record));
        } catch (Exception e) {
            handleException(e, "");
        }
    }

    public void delete(String type) {
        String errMsg = "";
        try {
            Article record = Article.dao.getById(getParaToLong("id"));
            Article.dao.checkArticle(record, type);
            Article.dao.deleteArticle(record);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    public void batchdelete(String type) {
        String errMsg = "";
        try {
            String[] ids = getParaValues("ids");
            if (ids[0].indexOf(",") > 0) {
                ids = ids[0].split(",");
            }
            if (ids == null || ids.length == 0) {
                errMsg = "缺少参数->ids";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            for (String id : ids) {
                Article article = Article.dao.getById(Long.parseLong(id));
                Article.dao.checkArticle(article, type);
                Article.dao.deleteArticle(article);
            }
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    public void sort(String type) {
        String errMsg = "";
        try {
            String[] ids = getParaValues("ids");
            if (ids[0].indexOf(",") > 0) {
                ids = ids[0].split(",");
            }
            if (ids == null || ids.length == 0) {
                errMsg = "没有排序的文章";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            //更新idx字段
            for (int i = 0; i < ids.length; i++) {
                Article r = Article.dao.getById(Long.parseLong(ids[i]));
                Article.dao.checkArticle(r, type);
                r.set("idx", ids.length - i);
                Article.dao.updateArticle(r);
            }
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

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

            Article article = Article.dao.getById(id);
            Article.dao.checkArticle(article, type);

            article.set("status", status);
            Article.dao.updateArticle(article);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

}
