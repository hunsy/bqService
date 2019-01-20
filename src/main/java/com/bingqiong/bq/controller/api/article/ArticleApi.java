package com.bingqiong.bq.controller.api.article;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.model.Article;
import com.bingqiong.bq.model.ArticleType;
import com.bingqiong.bq.utils.EncodeUtils;
import com.bingqiong.bq.utils.RequestUtil;
import com.bingqiong.bq.vo.ResponseDataVo;
import com.bingqiong.bq.vo.ResponseMobileDataVo;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.qiniu.util.Json;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by hunsy on 2017/4/11.
 */

public class ArticleApi extends BaseArticleApi {


    /**
     * 获取文章类型
     */
    public void types() throws Exception {

        List<String> ls = new ArrayList<>();
        ls.add("热点");
        List<String> lz = ArticleType.dao.listNames();
        ls.addAll(lz);
        renderJson(ResponseMobileDataVo.success(ls, EncodeUtils.isEncode()));
    }

    public void get() {
        get(TYPE_ARTICLE);
    }

    public void page() {
        try {

            JSONObject object = RequestUtil.getDecodeParams(getRequest());
            logger.info("xxxx,{}", JsonKit.toJson(object));
            int page = object.getIntValue("pageNo") == 0 ? 1 : object.getIntValue("pageNo");
            int size = object.getIntValue("pageSize") == 0 ? 10 : object.getIntValue("pageSize");
            Map<String, String> params = new HashMap<>();
            if (object.containsKey("is_hot") && object.getIntValue("is_hot") == 1) {
                params.put("is_hot", "1");
            }
            if (StringUtils.isNotEmpty(object.getString("article_type"))) {
                params.put("article_type", object.getString("article_type"));
            }
            if (StringUtils.isNotEmpty(object.getString("title"))) {
                params.put("title", object.getString("title"));
            }
            String user_id = object.getString("user_id");
            params.put("status", "1");
            logger.info("params:{}", JsonKit.toJson(params));
            String ps = Article.dao.articlePage(false, page, size, params, user_id);
            renderJson(ResponseMobileDataVo.success(JSONObject.parseObject(ps), EncodeUtils.isEncode()));
        } catch (Exception e) {
            handleException(e, "");
        }
    }

}
