package com.bingqiong.bq.controller.admin;

import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.utils.EncodeUtils;
import com.bingqiong.bq.utils.QiNiuUtil;
import com.bingqiong.bq.vo.ResponseDataVo;
import com.bingqiong.bq.vo.ResponseMobileDataVo;
import com.jfinal.kit.PathKit;
import com.jfinal.upload.UploadFile;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
            if (!ArrayUtils.contains(contentTypes, contentType)) {
                errMsg = "上传图片格式不正确->contentType:" + contentType;
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            String fileSubfix = file.getOriginalFileName().substring(file.getOriginalFileName().lastIndexOf(".") + 1);
            ;
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
            FileUtils.deleteQuietly(file.getFile());
            renderJson(ResponseDataVo.success(ret));
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 文件上传,xhediter富文本文件上传,必须返回{"err":"","msg":""}
     */
    public void richupload() {
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();
        String err = "";
        try {
            if ("application/octet-stream".equals(request.getContentType())) { //HTML 5 上传

                String dispoString = request.getHeader("Content-Disposition");
                int iFindStart = dispoString.indexOf("name=\"") + 6;
                int iFindEnd = dispoString.indexOf("\"", iFindStart);
                iFindStart = dispoString.indexOf("filename=\"") + 10;
                iFindEnd = dispoString.indexOf("\"", iFindStart);
                String sFileName = dispoString.substring(iFindStart, iFindEnd);
                int i = request.getContentLength();
                byte buffer[] = new byte[i];
                int j = 0;
                while (j < i) { //获取表单的上传文件
                    int k = request.getInputStream().read(buffer, j, i - j);
                    j += k;
                }
                if (buffer.length == 0) { //文件是否为空
                    err = "文件不能为空";
                    throw new BizException();
                }
                String fm = getSaveFilePath(sFileName);
                File f = new File(PathKit.getWebRootPath() + "/" + fm);
                FileUtils.writeByteArrayToFile(f, buffer);
                String url = QiNiuUtil.getInstance().upload(f, fm);
                File tmpfile = new File(PathKit.getWebRootPath() + "/temp_" + fm);
                Thumbnails.of(f)
                        .scale(0.5f)
                        .toFile(tmpfile);
                QiNiuUtil.getInstance().upload(tmpfile, "thumb_" + fm);
                Map<String, String> ret = new HashMap<>();
                ret.put("err", err);
                ret.put("msg", url);
                //删除缓存文件
                f.delete();
                tmpfile.delete();
                renderJson(ret);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Map<String, String> ret = new HashMap<>();
            ret.put("err", err);
            ret.put("msg", "");
            renderJson(ret);
        }
    }


    public String getSaveFilePath(String sFileName) throws IOException {
        String extensionName = sFileName.substring(sFileName.lastIndexOf(".") + 1); //获取文件扩展名
        String filename = UUID.randomUUID().toString(); //重命名文件
        String path = filename + "." + extensionName;
        logger.info(path);
        return path;
    }


    public void index() {

        renderJson("success", true);
    }

}
