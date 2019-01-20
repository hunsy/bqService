package com.bingqiong.bq.controller.api;

import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.controller.admin.BaseController;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.model.Article;
import com.bingqiong.bq.model.UserCollection;
import com.bingqiong.bq.utils.RequestUtil;
import com.bingqiong.bq.vo.ResponseEmptyVo;
import com.jfinal.plugin.activerecord.Record;

/**
 * 用户收藏相关服务
 * <p>
 * Created by hunsy on 2017/4/11.
 */
public class CollectionApi extends BaseController {


    /**
     * 新增收藏
     * <p>
     * ->param [user_id,user_name,article_id]
     */
    public void add() {
        String errMsg = "";
        try {
            Record record = RequestUtil.parseRecord(null, getRequest());
            if (record.get("user_id") == null
                    || record.get("article_id") == null) {
                errMsg = "缺少参数";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Article article = Article.dao.getById(Long.parseLong(record.get("article_id").toString()));
            if (article == null) {
                errMsg = "文章不存在->id" + record.get("article_id");
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            UserCollection.dao.saveCollection(record);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 新增收藏
     * <p>
     * ->param [id]
     */
    public void remove() {
        String errMsg = "";
        try {
            Long id = getParaToLong("id");
            if (id == null) {
                errMsg = "缺少参数->id";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Record collection = UserCollection.dao.getById(id);
            if (collection == null) {
                errMsg = "收藏记录不存在->id" + id;
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            UserCollection.dao.deleteCollection(collection);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

}
