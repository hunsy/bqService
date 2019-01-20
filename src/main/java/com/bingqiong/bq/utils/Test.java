package com.bingqiong.bq.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by hunsy on 2017/4/12.
 */
public class Test {

    public static void main(String[] args) throws IOException {
//        String text = "基于java语言开发的轻量级的中文分词工具包";
        Properties properties = new Properties();
        InputStream is = new FileInputStream(new File("C:\\Users\\hunsy\\Desktop\\qiniu.properties"));
        properties.load(is);
        QiNiuUtil qiNiuUtil = QiNiuUtil.getInstance();
        qiNiuUtil.init(properties);

        qiNiuUtil.upload(new File("C:\\Users\\hunsy\\Desktop\\QQ截图20170414091210.png"),"2017041116290061cf0.png");
    }
}
