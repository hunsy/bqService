package com.bingqiong.bq.api.controller.comm;


import com.bingqiong.bq.api.interceptor.ApiAuthInterceptor;
import com.bingqiong.bq.api.interceptor.ApiParamInterceptor;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.controller.IndexController;
import com.bingqiong.bq.comm.interceptor.GlobalInterceptor;
import com.bingqiong.bq.comm.utils.DESUtil;
import com.bingqiong.bq.comm.utils.QiNiuUtil;
import com.bingqiong.bq.conf.BqCmsConf;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.kit.HttpKit;
import com.jfinal.upload.UploadFile;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by hunsy on 2017/7/7.
 */
public class IndexApi extends IBaseController {


    private final String[] contentTypes = new String[]{"image/jpeg", "image/png", "image/gif", "image/bmp"};
    private Logger logger = LoggerFactory.getLogger(IndexController.class);

    /**
     * 文件上传
     */
    @Clear
    @Before(GlobalInterceptor.class)
    public void upload() {
        try {
            UploadFile file = getFile("filedata");
            String fileSubfix = file.getOriginalFileName().substring(file.getOriginalFileName().lastIndexOf(".") + 1);
            String tempFileName = UUID.randomUUID().toString() + "." + fileSubfix;
            File of = new File(file.getSaveDirectory() + tempFileName);
            Thumbnails.of(FileUtils.openInputStream(file.getFile()))
                    .scale(0.5f)
                    .toFile(of);
            String fileNamePrefix = DateTime.now().toString("yyyyMMddHHmmsss") + UUID.randomUUID().toString().substring(0, 4);
            String url = QiNiuUtil.getInstance().upload(file.getFile(), fileNamePrefix + "." + fileSubfix);
            QiNiuUtil.getInstance().upload(of, "thumb_" + fileNamePrefix + "." + fileSubfix);
            Map<String, String> ret = new HashMap<String, String>();
            ret.put("imgUrl", url);
            //删缓存文件
            FileUtils.deleteQuietly(file.getFile());
            renderSuccess(ret, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    @Clear
    public void parse() {

        String password = getPara("pwd");
        renderJson(DESUtil.decrypt(password));
    }


}
