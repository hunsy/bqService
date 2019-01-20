package com.bingqiong.bq.controller.admin;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.conf.ErrorCode;
import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.interceptor.PageInterceptor;
import com.bingqiong.bq.model.Banner;
import com.bingqiong.bq.utils.RequestUtil;
import com.bingqiong.bq.vo.PageRequest;
import com.bingqiong.bq.vo.ResponseDataVo;
import com.bingqiong.bq.vo.ResponseEmptyVo;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by hunsy on 2017/4/11.
 */
public class BannerController extends BaseController {


    /**
     * 获取banner详情
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
            Banner banner = Banner.dao.getById(id);
            if (banner == null) {
                errMsg = "Banner不存在->id:" + id;
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            renderJson(ResponseDataVo.success(banner));
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }


    /**
     * 获取banner详情
     * -> param [pageNo,pageSize,param_title,param_status]
     */
    @Before(PageInterceptor.class)
    public void page() {
        try {
//            int page = getParaToInt("pageNo", 1);
//            int size = getParaToInt("pageSize", 10);
            PageRequest pageRequest = getAttr("pageRequest");
            logger.info("pageRequest:{}", JsonKit.toJson(pageRequest));
            Map<String, String> params = RequestUtil.getParams(getRequest());
            String ps = Banner.dao.findPage(pageRequest.getPageNo(), pageRequest.getPageSize(), params);
            renderJson(ResponseDataVo.success(JSONObject.parseObject(ps)));
        } catch (Exception e) {
            handleException(e, "");
        }
    }

    /**
     * 保存banner.
     */
    public void save() {
        String errMsg = "";
        try {
            Banner banner = getModel(Banner.class);
            if (banner.get("thumb_url") == null) {
                errMsg = "缺少参数->thumb_url";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            if (banner.getStr("bannerType").equals("文章")) {
                if (banner.getLong("article_id") == null) {
                    errMsg = "文章类型，缺少文章id";
                    throw new BizException(BqErrorCode.CODE_FAILED.getCode());
                }
            } else if (banner.getStr("bannerType").equals("外链")) {
                if (StringUtils.isEmpty(banner.getStr("redirect_url"))) {
                    errMsg = "外链类型类型，缺少外链链接";
                    throw new BizException(BqErrorCode.CODE_FAILED.getCode());
                }
            }

            Banner.dao.saveBanner(banner);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }


    /**
     * 保存banner.
     */
    public void update() {
        String errMsg = "";
        try {
            Banner banner = getModel(Banner.class);
            if (banner.get("id") == null) {
                errMsg = "缺少参数->id";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            if (banner.getStr("bannerType").equals("文章")) {
                if (banner.getLong("article_id") == null) {
                    errMsg = "文章类型，缺少文章id";
                    throw new BizException(BqErrorCode.CODE_FAILED.getCode());
                }
                banner.set("redirect_url", null);
            } else if (banner.getStr("bannerType").equals("外链")) {
                if (StringUtils.isEmpty(banner.getStr("redirect_url"))) {
                    errMsg = "外链类型类型，缺少外链链接";
                    throw new BizException(BqErrorCode.CODE_FAILED.getCode());
                }
                banner.set("article_id", null);
                banner.set("type", null);
            }
            Banner.dao.updateBanner(banner);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 删除banner.
     * ->param [id]
     */
    public void delete() {
        String errMsg = "";
        try {
            Long id = getParaToLong("id");
            if (id == null) {
                errMsg = "缺少参数->id";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Banner banner = Banner.dao.getById(id);
            if (banner == null) {
                logger.error("Banner不存在->id:{}", id);
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Banner.dao.deleteBanner(banner);
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

            Banner banner = Banner.dao.getById(getParaToLong("id"));
            if (banner == null) {
                errMsg = "Banner不存在->id:" + getPara("id");
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            Integer status = getParaToInt("status");
            if (status == null) {
                errMsg = "缺少参数->status";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            banner.set("status", status);
            Banner.dao.updateBanner(banner);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 排序
     */
    public void sort() {
        String errMsg = "";
        try {
            String[] ids = getParaValues("ids");
            if (ids[0].indexOf(",") > 0) {
                ids = ids[0].split(",");
            }
            if (ids == null || ids.length == 0) {
                errMsg = "没有排序的Banner";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            //更新idx字段
            for (int i = 0; i < ids.length; i++) {
                Banner r = Banner.dao.getById(Long.parseLong(ids[i]));
                if (r == null) {
                    errMsg = "Banner不存在";
                    throw new BizException(BqErrorCode.CODE_FAILED.getCode());
                }
                r.set("idx", ids.length - i);
                Banner.dao.updateBanner(r);
            }
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    public void init() {
        Banner.dao.init();
    }
}
