package com.bingqiong.bq.cms.controller.comm;

import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.interceptor.GlobalInterceptor;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.utils.ChannelsUtils;
import com.bingqiong.bq.comm.utils.QiNiuUtil;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.comm.AppVersion;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import net.dongliu.apk.parser.ApkParsers;
import net.dongliu.apk.parser.bean.ApkMeta;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by hunsy on 2017/7/7.
 */
public class AppVersionController extends IBaseController {


    private Logger logger = LoggerFactory.getLogger(getClass());


    public void get() {

        try {
            Long id = getParaToLong("id");
            AppVersion version = AppVersion.dao.findById(id);
            renderSuccess(version);
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    /**
     * 获取分页数据
     */
    @Before(PageInterceptor.class)
    public void page() {

        try {

            PageRequest pageRequest = getAttr("pageRequest");
            Page<Record> page = AppVersion.dao.findPage(pageRequest);
            renderSuccess(page);
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    /**
     * 文件上传
     */
    @Clear
    @Before(GlobalInterceptor.class)
    public void upload() {
        try {
            UploadFile file = getFile("filedata");
            String contentType = file.getContentType();
            logger.info("文件ContentType:{}", contentType);
            String fileSubfix = file.getOriginalFileName().substring(file.getOriginalFileName().lastIndexOf(".") + 1);
            if (!fileSubfix.equalsIgnoreCase("apk")) {
                throw new BizException(ErrorCode.APK_FORMAT_SUPPORT);
            }
            String fileNamePrefix = DateTime.now().toString("yyyyMMddHHmmsss")
                    + UUID.randomUUID().toString().substring(0, 4);
            //上传七牛
            String url = QiNiuUtil.getInstance().upload(file.getFile(), fileNamePrefix + "." + fileSubfix);
            //解析版本号等信息
            ApkMeta metaInfo = ApkParsers.getMetaInfo(file.getFile());
            Long size = file.getFile().length();
            Map<String, String> ret = new HashMap<>();
            ret.put("apkTmpUrl", url);
            ret.put("apkName", metaInfo.getName());
            ret.put("apkSize", size.toString());
            ret.put("clientName", metaInfo.getName());
            ret.put("mpackage", metaInfo.getPackageName());
            ret.put("version", metaInfo.getVersionName());
            ret.put("versionCode", metaInfo.getVersionCode().toString());
            //删缓存文件
            file.getFile().delete();
            renderSuccess(ret);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 获取channels
     */
    public void channels() {
        renderSuccess(ChannelsUtils.getInstance().getChannels());
    }

    /**
     * 保存
     */
    public void save() {

        try {

            AppVersion version = getModel(AppVersion.class);
            if (version.get("channel_code") == null
                    || version.getStr("version") == null || version.get("channel_code") == null) {
                throw new BizException(ErrorCode.MISSING_PARM);
            }

            Record cacheVersion = AppVersion.dao.getByNameAndChannel(
                    version.getStr("name"),
                    version.getStr("channel_code"));
            if (cacheVersion != null) {
                throw new BizException(ErrorCode.APP_VERSIO_EXIST);
            }
            AppVersion.dao.saveVersion(version);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    /**
     * 保存
     */
    public void update() {

        try {
            AppVersion version = getModel(AppVersion.class);
            if (version.get("channel_code") == null || version.getStr("name") == null
                    || version.getStr("version") == null || version.get("channel_code") == null) {
                throw new BizException(ErrorCode.MISSING_PARM);
            }
            AppVersion.dao.updateVersion(version);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    public void delete() {

        try {
            Long id = getParaToLong("id");
            AppVersion.dao.deleteVersion(id);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    public void sold() {
        try {
            Long id = getParaToLong("id");
            Integer status = getParaToInt("status");
            if (id == null || status == null) {
                logger.error("缺少参数");
                throw new BizException(ErrorCode.MISSING_PARM);
            }
            AppVersion version = AppVersion.dao.findById(id);
            if (version == null) {
                logger.error("版本不存在");
                throw new BizException(ErrorCode.APP_VERSION_NOT_EXIST);
            }
            version.set("status", status);
            AppVersion.dao.updateVersion(version);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }


}
