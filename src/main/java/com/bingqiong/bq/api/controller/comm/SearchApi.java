package com.bingqiong.bq.api.controller.comm;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.api.interceptor.ApiAuthInterceptor;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.utils.EsUtils;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.conf.BqCmsConf;
import com.bingqiong.bq.model.comm.SearchHotWord;
import com.bingqiong.bq.model.user.User;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by hunsy on 2017/7/5.
 */
public class SearchApi extends IBaseController {


    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 获取热词
     */
    @Clear(ApiAuthInterceptor.class)
    public void hotwords() {

        try {
            //12个词
//            HashMap<String, Object> words = new HashMap<>();
            List<Record> hotWords = SearchHotWord.dao.findList();
            logger.info("{}", JsonKit.toJson(hotWords));
            Set<Record> words = new HashSet<>();
            if (hotWords != null && hotWords.size() < 12) {
                words.addAll(hotWords);
            }

            //遍历查询词，直到获取12个
            if (hotWords != null && hotWords.size() > 12) {
                while (words.size() < 12) {
                    int index = RandomUtils.nextInt(0, hotWords.size());
                    Record record = hotWords.get(index);
                    words.add(record);
                }
            }
            logger.info("---:{}", JsonKit.toJson(words));
            renderSuccess(words.toArray(), BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    /**
     * 搜索
     */
    @Before(PageInterceptor.class)
    public void group() {

        try {
            PageRequest pageRequest = getAttr("pageRequest");
            Map<String, String> params = pageRequest.getParams();
            if (params.get("text") == null) {
                throw new BizException(ErrorCode.MISSING_PARM);
            }

            int size = pageRequest.getPageSize();
            int pageNo = pageRequest.getPageNo();
            int from = (pageNo - 1) * size;
            String text = params.get("text");
            SearchHotWord.dao.saveOrUpdateWord(text.trim());
            JSONObject groups = EsUtils.getInstance().searchGroup(text, from, size);
            logger.info("返回圈子:{}",groups.toJSONString());
            renderSuccess(groups, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    @Before(PageInterceptor.class)
    public void post() {

        try {
            PageRequest pageRequest = getAttr("pageRequest");
            Map<String, String> params = pageRequest.getParams();
            if (params.get("text") == null) {
                throw new BizException(ErrorCode.MISSING_PARM);
            }
            String user_id = null;
            User user = getAttr("bq_user");
            if (user != null) {
                user_id = user.getStr("user_id");
            }
            int size = pageRequest.getPageSize();
            int pageNo = pageRequest.getPageNo();
            int from = (pageNo - 1) * size;
            String text = params.get("text").toString();
            Object group_id = params.get("group_id");
            //新增热词
            SearchHotWord.dao.saveOrUpdateWord(text.trim());
            JSONObject posts = EsUtils.getInstance().searchPost(text, group_id, from, size, user_id);
            logger.info("返回帖子:{}",posts.toJSONString());
            renderSuccess(posts, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }
//    public void index() {
//
//        try {
//            PageRequest pageRequest = getAttr("pageRequest");
//            Map<String, Object> params = pageRequest.getParams();
//            if (params.get("text") == null || params.get("type") == null) {
//                throw new BizException(ErrorCode.MISSING_PARM);
//            }
//            JSONObject groups = null;
//            JSONObject posts = null;
//            //type  all group post
//            String type = params.get("type").toString();
//            String text = params.get("text").toString();
//            int size = pageRequest.getPageSize();
//            int pageNo = pageRequest.getPageNo();
//            int from = (pageNo - 1) * size;
//
//            if ("all".equals(type) || "group".equals(type)) {
//                groups = EsUtils.getInstance().searchGroup(text, from, size);
//            }
//            if ("all".equals(type) || "post".equals(type)) {
//                posts = EsUtils.getInstance().searchPost(text, from, size);
//            }
//
//            Map<String, JSONObject> ret = new HashMap<>();
//            if (groups != null) {
//                ret.put("groups", groups);
//            }
//            if (posts != null) {
//                ret.put("posts", posts);
//            }
//
//            SearchHotWord.dao.saveOrUpdateWord(text);
//            renderSuccess(ret, BqCmsConf.enc);
//        } catch (Exception e) {
//            renderFailure(e);
//        }
//    }

}
