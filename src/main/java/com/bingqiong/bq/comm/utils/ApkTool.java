//package com.bingqiong.bq.comm.utils;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import net.sf.json.xml.XMLSerializer;
//import org.apache.commons.io.IOUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipFile;
//
//public class ApkTool {
//
//    private static Logger log = LoggerFactory.getLogger(ApkTool.class);
//
//    /**
//     * 获取apk的名称
//     *
//     * @param file
//     * @return
//     */
//    public static Map<String, String> getApkInfo(File file) {
//        Map<String, String> info = new HashMap<>();
//        try {
//            byte[] bs = IOUtils.toByteArray(getXmlInputStream(file));
//            String str = new XMLSerializer().read(new String(bs, "UTF-8"))
//                    .toString();
//            JSONObject obj = JSON.parseObject(str);
//            info.put("versionCode", obj.getString("@android:versionCode"));
//            info.put("versionName", obj.getString("@android:versionName"));
//            info.put("package", obj.getString("@package"));
//            JSONObject application = obj.getJSONObject("application");
//            info.put("apkName", application.getString("@android:label"));
//            log.info("apkinfo:{}", obj.toJSONString());
//            log.info("apkName:{}", application.getString("@android:label"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return info;
//    }
//
//    private static InputStream getXmlInputStream(File file) {
//        InputStream inputStream = null;
//        InputStream xmlInputStream = null;
//        ZipFile zipFile = null;
//        try {
//            zipFile = new ZipFile(file);
//            ZipEntry zipEntry = new ZipEntry("AndroidManifest.xml");
//            inputStream = zipFile.getInputStream(zipEntry);
//
//            AXMLPrinter xmlPrinter = new AXMLPrinter();
//            xmlPrinter.startPrinf(inputStream);
//            xmlInputStream = new ByteArrayInputStream(xmlPrinter.getBuf()
//                    .toString().getBytes("UTF-8"));
//        } catch (IOException e) {
//            e.printStackTrace();
//            try {
//                inputStream.close();
//                zipFile.close();
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            }
//        }
//        return xmlInputStream;
//    }
//
//    public static void main(String[] args) {
//        getApkInfo(new File(
//                "C:\\Users\\hunsy\\Downloads\\com.qihoo.magic_2023.apk"));
//    }
//}