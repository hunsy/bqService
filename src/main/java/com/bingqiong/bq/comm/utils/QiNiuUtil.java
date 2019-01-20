package com.bingqiong.bq.comm.utils;

import com.jfinal.upload.UploadFile;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.io.IoApi;
import com.qiniu.api.io.PutExtra;
import com.qiniu.api.io.PutRet;
import com.qiniu.api.rs.PutPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by hunsy on 2017/4/11.
 */
public class QiNiuUtil {

    private Logger logger = LoggerFactory.getLogger(QiNiuUtil.class);
    private static QiNiuUtil qiNiuUtil = null;
    private Properties properties;
    private String BUCKET_NAME;
    private String QINIU_URL;
    private Mac mac;

    private QiNiuUtil() {

    }

    public static QiNiuUtil getInstance() {
        if (qiNiuUtil == null) {
            qiNiuUtil = new QiNiuUtil();
        }
        return qiNiuUtil;
    }

    /**
     * 初始化七牛上传工具。
     *
     * @param properties
     */
    public void init(Properties properties) {
        this.properties = properties;
        Config.ACCESS_KEY = properties.getProperty("ACCESS_KEY");
        Config.SECRET_KEY = properties.getProperty("SECRET_KEY");
        BUCKET_NAME = properties.getProperty("BUCKET_NAME");
        QINIU_URL = properties.getProperty("QINIU_URL");
        mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
    }


    public String upload(UploadFile file, String fileName) {

        try {

            PutPolicy putPolicy = new PutPolicy(BUCKET_NAME);
            String token = putPolicy.token(mac);
            PutExtra extra = new PutExtra();
            PutRet put = IoApi.Put(token, fileName, new FileInputStream(file.getFile()), extra);
            if (put != null)
                logger.info("put.tostring========" + put.toString());
            logger.info("put.getResponse========" + put.getResponse());
            if (!(put != null) && put.toString().startsWith("{\"hash\":\"")) {
                throw new RuntimeException();
            }
            return QINIU_URL + "/" + fileName;
        } catch (Exception e) {
            logger.error("QnUtils.upload exception {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public String upload(File file, String name) {

        try {

            PutPolicy putPolicy = new PutPolicy(BUCKET_NAME);
            String token = putPolicy.token(mac);
            PutExtra extra = new PutExtra();
            PutRet put = IoApi.Put(token, name, new FileInputStream(file), extra);
            if (put != null)
                logger.info("put.tostring========" + put.toString());
            logger.info("put.getResponse========" + put.getResponse());
            if (!(put != null) && put.toString().startsWith("{\"hash\":\"")) {
                throw new RuntimeException();
            }
            return QINIU_URL + "/" + name;
        } catch (Exception e) {
            logger.error("QnUtils.upload exception {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }


}
