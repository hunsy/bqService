package com.bingqiong.bq.controller.api.article;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.model.Article;
import com.bingqiong.bq.model.Category;
import com.bingqiong.bq.utils.EncodeUtils;
import com.bingqiong.bq.utils.RequestUtil;
import com.bingqiong.bq.vo.ResponseMobileDataVo;

import java.util.Map;

/**
 * Created by hunsy on 2017/4/11.
 */
public class PostApi extends BaseArticleApi {


    /**
     * 获取详情
     */
    public void get() {
        get(TYPE_POST);
    }

    /**
     * 获取置顶帖子。
     */
    public void tops() {
        try {

            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            Long group_id = obj.getLong("group_id");
            if (group_id == null) {
                logger.error("缺少group_id");
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            String rs = Article.dao.getTops(group_id);
            renderJson(ResponseMobileDataVo.success(JSONArray.parseArray(rs), EncodeUtils.isEncode()));
        } catch (Exception e) {
            handleException(e, "");
        }
    }

    /**
     * 获取分页
     * ->param [pageNo,pageSize]
     */
    public void page() {
        String errMsg = "";
        try {

            Long group_id = getParaToLong(-1);
            if (group_id == null) {
                errMsg = "缺少参数,/{group_id}";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            Category group = Category.dao.getById(group_id);
            if (group == null || Long.parseLong(group.get("parent_id").toString()) == 0) {
                errMsg = "圈子不存在->group_id:" + group_id;
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            JSONObject object = RequestUtil.getDecodeParams(getRequest());
            int page = object.getIntValue("pageNo") == 0 ? 1 : object.getIntValue("pageNo");
            int size = object.getIntValue("pageSize") == 0 ? 10 : object.getIntValue("pageSize");
            Map<String, String> params = RequestUtil.getParams(getRequest());
            params.put("status", "1");
            params.put("is_top", "0");
            String user_id = object.getString("user_id");
            String ps = Article.dao.postPage(false, group_id, page, size, params, user_id);
            renderJson(ResponseMobileDataVo.success(JSONObject.parseObject(ps), EncodeUtils.isEncode()));
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }


}
