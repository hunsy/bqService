package com.bingqiong.bq.controller.admin;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.interceptor.PageInterceptor;
import com.bingqiong.bq.model.AppVersion;
import com.bingqiong.bq.utils.ApkTool;
import com.bingqiong.bq.utils.ChannelsUtils;
import com.bingqiong.bq.utils.QiNiuUtil;
import com.bingqiong.bq.utils.RequestUtil;
import com.bingqiong.bq.vo.PageRequest;
import com.bingqiong.bq.vo.ResponseDataVo;
import com.bingqiong.bq.vo.ResponseEmptyVo;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by hunsy on 2017/5/15.
 */
public class VersionController extends BaseController {


    /**
     * 文件上传
     */
    public void upload() {
        String errMsg = "";
        try {
            UploadFile file = getFile("filedata");
            String contentType = file.getContentType();
            logger.info("文件ContentType:{}", contentType);
//            if (!ArrayUtils.contains(contentTypes, contentType)) {
//                errMsg = "上传图片格式不正确->contentType:" + contentType;
//                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
//            }
            String fileSubfix = file.getOriginalFileName().substring(file.getOriginalFileName().lastIndexOf(".") + 1);
            if (!fileSubfix.equals("apk")) {
                errMsg = "文件格式错误。请传入APK文件";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            String fileNamePrefix = DateTime.now().toString("yyyyMMddHHmmsss")
                    + UUID.randomUUID().toString().substring(0, 4);


            String url = QiNiuUtil.getInstance().upload(file.getFile(), fileNamePrefix + "." + fileSubfix);

            Map<String, String> info = ApkTool.getApkInfo(file.getFile());
            Long size = file.getFile().length();

            Map<String, String> ret = new HashMap<>();
            ret.put("apkTmpUrl", url);
            ret.put("apkName", info.get("apkName"));
            ret.put("apkSize", size.toString());
            ret.put("clientName", info.get("apkName"));
            ret.put("mpackage", info.get("package"));
            ret.put("version", info.get("versionName"));
            ret.put("versionCode", info.get("versionCode"));
            //删缓存文件
            file.getFile().delete();
            renderJson(ResponseDataVo.success(ret));
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    public void get() {
        String errMsg = "";
        try {
            Long id = getParaToLong("id");
            if (id == null) {
                errMsg = "缺少参数ID";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            AppVersion version = AppVersion.dao.getById(id);
            if (version == null) {
                errMsg = "版本不存在";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            renderJson(ResponseDataVo.success(version));
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 获取channels
     */
    public void channels() {
        renderJson(ResponseDataVo.success(ChannelsUtils.getInstance().getChannels()));
    }

    /**
     * 分页
     */
    @Before(PageInterceptor.class)
    public void page() {

        try {
//            int page = getParaToInt("pageNo", 1);
//            int size = getParaToInt("pageSize", 10);
            PageRequest pageRequest = getAttr("pageRequest");
            logger.info("pageRequest:{}", JsonKit.toJson(pageRequest));
            Map<String, String> params = RequestUtil.getParams(getRequest());
            String ps = AppVersion.dao.page(pageRequest.getPageNo(), pageRequest.getPageSize(), params);
            renderJson(ResponseDataVo.success(JSONObject.parseObject(ps)));
        } catch (Exception e) {
            handleException(e, "");
        }
    }


    public void save() {
        String errMsg = "";
        try {
            AppVersion version = getModel(AppVersion.class);
            if (version.get("channel_code") == null
                    || version.getStr("name") == null
                    || version.getStr("version") == null
                    || version.get("channel_code") == null) {
                errMsg = "缺少参数->channel_code|name|version";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            Record cacheVersion = AppVersion.dao.getByNameAndChannel(
                    version.getStr("name"),
                    version.getStr("channel_code"));
            if (cacheVersion != null) {
                errMsg = "该渠道下已存在这个名称的应用";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            AppVersion.dao.saveApp(version);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    public void update() {
        String errMsg = "";
        try {
            AppVersion version = getModel(AppVersion.class);
            if (version.get("id") == null ||
                    version.get("channel_code") == null
                    || version.getStr("name") == null
                    || version.getStr("version") == null
                    || version.get("channel_code") == null) {
                errMsg = "缺少参数->id|channel_code|name|version";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            Record cacheVersion = AppVersion.dao.getByNameAndChannel(version.getStr("name"), version.getStr("channel_code"));
            if (cacheVersion != null && cacheVersion.getLong("id") != version.getLong("id")) {
                errMsg = "该渠道下已存在这个名称的应用";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            AppVersion.dao.updateVersion(version);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    public void delete() {
        String errMsg = "";
        try {
            Long id = getParaToLong("id");
            if (id == null) {
                errMsg = "缺少参数->id";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            AppVersion appVersion = AppVersion.dao.getById(id);
            if (appVersion == null) {
                errMsg = "版本不存在";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            AppVersion.dao.deleteVersion(appVersion);
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

            AppVersion version = AppVersion.dao.getById(getParaToLong("id"));
            if (version == null) {
                errMsg = "版本不存在->id:" + getPara("id");
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            Integer status = getParaToInt("status");
            if (status == null) {
                errMsg = "缺少参数->status";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            version.set("status", status);
            AppVersion.dao.updateVersion(version);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }
}
