package com.bingqiong.bq.controller.api;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.coobird.thumbnailator.Thumbnails;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.controller.admin.BaseController;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.utils.EncodeUtils;
import com.bingqiong.bq.utils.QiNiuUtil;
import com.bingqiong.bq.vo.ResponseMobileDataVo;
import com.jfinal.upload.UploadFile;

/**
 * 公共接口类
 * <p>
 * Created by hunsy on 2017/4/7.
 */
public class IndexController extends BaseController {

    private final String[] contentTypes = new String[]{"image/jpeg", "image/png", "image/gif", "image/bmp"};
    private Logger logger = LoggerFactory.getLogger(IndexController.class);

    /**
     * 文件上传
     */
    public void upload() {
        String errMsg = "";
        try {
            UploadFile file = getFile("filedata");
            String contentType = file.getContentType();
            logger.info("文件ContentType:{}", contentType);
/*            if (!ArrayUtils.contains(contentTypes, contentType)) {
                errMsg = "上传图片格式不正确->contentType:" + contentType;
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }*/
            String fileSubfix = file.getOriginalFileName().substring(file.getOriginalFileName().lastIndexOf(".") + 1);
            String tempFileName = UUID.randomUUID().toString() + "." + fileSubfix;
            File of = new File(file.getSaveDirectory() + tempFileName);
            Thumbnails.of(FileUtils.openInputStream(file.getFile()))
                    .scale(0.5f)
                    .toFile(of);
            String fileNamePrefix = DateTime.now().toString("yyyyMMddHHmmsss")
                    + UUID.randomUUID().toString().substring(0, 4);
            String url = QiNiuUtil.getInstance().upload(file.getFile(), fileNamePrefix + "." + fileSubfix);
            QiNiuUtil.getInstance().upload(of, "thumb_" + fileNamePrefix + "." + fileSubfix);
            Map<String, String> ret = new HashMap<>();
            ret.put("imgUrl", url);
            //删缓存文件
            file.getFile().delete();
            renderJson(ResponseMobileDataVo.success(ret, EncodeUtils.isEncode()));
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

}
