package com.bingqiong.bq.comm.controller;

import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.interceptor.GlobalInterceptor;
import com.bingqiong.bq.comm.utils.QiNiuUtil;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.kit.PathKit;
import com.jfinal.upload.UploadFile;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 公用接口，图片上传
 * Created by hunsy on 2017/6/22.
 */
public class IndexController extends IBaseController {

    protected final String[] contentTypes = new String[]{"image/jpeg", "image/png", "image/gif", "image/bmp"};
    private Logger logger = LoggerFactory.getLogger(IndexController.class);

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
//            if (!ArrayUtils.contains(contentTypes, contentType)) {
//                logger.error("上传图片格式不正确->contentType:" + contentType);
//                throw new BizException(ErrorCode.FILE_TYPE_NOT_SUPPORT);
//            }
            String fileSubfix = file.getOriginalFileName().substring(file.getOriginalFileName().lastIndexOf(".") + 1);
            String tempFileName = UUID.randomUUID().toString() + "." + fileSubfix;
            File of = new File(file.getSaveDirectory() + tempFileName);
            Thumbnails.of(FileUtils.openInputStream(file.getFile()))
                    .scale(0.5f)
                    .toFile(of);
            String fileNamePrefix = UUID.randomUUID().toString();
            String url = QiNiuUtil.getInstance().upload(file.getFile(), fileNamePrefix + "." + fileSubfix);
            QiNiuUtil.getInstance().upload(of, "thumb_" + fileNamePrefix + "." + fileSubfix);
            Map<String, String> ret = new HashMap<String, String>();
            ret.put("imgUrl", url);
            //删缓存文件
            FileUtils.deleteQuietly(file.getFile());
            renderSuccess(ret);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 文件上传,xhediter富文本文件上传,必须返回{"err":"","msg":""}
     */
    public void richupload() {
        HttpServletRequest request = getRequest();
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
                Map<String, String> ret = new HashMap<String, String>();
                ret.put("err", err);
                ret.put("msg", url);
                //删除缓存文件
                f.delete();
                tmpfile.delete();
                renderJson(ret);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Map<String, String> ret = new HashMap<String, String>();
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

}
