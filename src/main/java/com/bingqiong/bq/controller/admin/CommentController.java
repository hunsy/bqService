package com.bingqiong.bq.controller.admin;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.interceptor.PageInterceptor;
import com.bingqiong.bq.model.Comment;
import com.bingqiong.bq.utils.RequestUtil;
import com.bingqiong.bq.vo.PageRequest;
import com.bingqiong.bq.vo.ResponseDataVo;
import com.bingqiong.bq.vo.ResponseEmptyVo;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;

import java.util.Map;

/**
 * 评论，回复
 * <p>
 * Created by hunsy on 2017/4/10.
 */
public class CommentController extends BaseController {

    /**
     * 分页
     * ->param [pageNo,pageSize,param_title,param_start_time,param_end_time]
     */
    @Before(PageInterceptor.class)
    public void page() {
        try {
//            int page = getParaToInt("pageNo", 1);
//            int size = getParaToInt("pageSize", 10);
            PageRequest pageRequest = getAttr("pageRequest");
            logger.info("pageRequest:{}", JsonKit.toJson(pageRequest));
            Map<String, String> params = RequestUtil.getParams(getRequest());
            String ps = Comment.dao.findPage(pageRequest.getPageNo(), pageRequest.getPageSize(), params);
            renderJson(ResponseDataVo.success(JSONObject.parseObject(ps)));
        } catch (Exception e) {
            handleException(e, "");
        }
    }

    /**
     * 删除评论
     * ->param [id]
     */
    public void delete() {
        String errMsg = "";
        try {
            Long id = getParaToLong("id");
            if (id == null) {
                errMsg = "缺少参数id";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            Comment record = Comment.dao.getById(id);
            if (record == null) {
                errMsg = "评论不存在->id:" + id;
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Comment.dao.deleteComment(record);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 删除评论
     * ->param [ids]
     */
    public void batchdelete() {
        String errMsg = "";
        try {
            String[] ids = getParaValues("ids");
            if (ids[0].indexOf(",") > 0) {
                ids = ids[0].split(",");
            }
            if (ids == null || ids.length == 0) {
                errMsg = "没有参数";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            for (String id : ids) {
                Comment record = Comment.dao.getById(Long.parseLong(id));
                if (record == null) {
                    errMsg = "评论不存在->id:" + id;
                    throw new BizException(BqErrorCode.CODE_FAILED.getCode());
                }
                Comment.dao.deleteComment(record);
            }
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }


}
