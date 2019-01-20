package com.bingqiong.bq.comm.http;

import com.alibaba.fastjson.JSONObject;
import com.squareup.okhttp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 进行get、post请求
 * <p>
 * Created by hunsy on 2017/5/25.
 */
public class DoRequest {

    private static Logger logger = LoggerFactory.getLogger(DoRequest.class);

    /**
     * post请求
     *
     * @param obj
     */
    public static JSONObject doPost(JSONObject obj, String url) throws IOException {
        logger.info("\n");
        logger.info("开始请求:{}>>>>", url);
        logger.info("请求参数:{}", obj.toJSONString());
        Request req = new Request.Builder()
                .url(url)
                .post(getFormBody(obj))
                .build();

        Response resp = MyHttpClient.exec(req);
        String str = resp.body().string();
        logger.error("返回数据：{}", str);
        if (resp.isSuccessful()) {
            JSONObject json = JSONObject.parseObject(str);
            return json;
        }
        return null;
    }

    /**
     * 请求
     *
     * @param obj
     * @param url
     * @throws IOException
     */
    public static void putJsonPost(String obj, String url) throws IOException {

        RequestBody requestBody = RequestBody.create(MediaType.parse("JSON"), obj);

        Request req = new Request.Builder()
                .url(url)
                .put(requestBody)
                .build();

        Response resp = MyHttpClient.exec(req);
        String str = resp.body().string();
        logger.error("返回数据：{}", str);
        if (resp.isSuccessful()) {
            JSONObject json = JSONObject.parseObject(str);
            logger.info("响应结果{}", json.toJSONString());
        }
    }

    /**
     * 请求
     *
     * @param url
     * @throws IOException
     */
    public static void delPost(String url) throws IOException {


        Request req = new Request.Builder()
                .url(url)
                .delete()
                .build();

        Response resp = MyHttpClient.exec(req);
        String str = resp.body().string();
        logger.error("返回数据：{}", str);
        if (resp.isSuccessful()) {
            JSONObject json = JSONObject.parseObject(str);
            logger.info("响应结果{}", json.toJSONString());
        }
    }

    /**
     * Get请求
     *
     * @param url
     * @return
     */
    public static JSONObject doGet(String url) throws IOException {
        logger.info("\n");
        logger.info("开始请求:{}>>>>", url);
        Request req = new Request.Builder()
                .url(url)
                .get()
                .build();

        Response resp = MyHttpClient.exec(req);
        String str = resp.body().string();
        logger.error("返回数据：{}", str);
        if (resp.isSuccessful()) {
            JSONObject json = JSONObject.parseObject(str);
            return json;
        }
        return null;
    }

    /**
     * put请求
     *
     * @param url
     * @param object
     * @return
     * @throws IOException
     */
    public static JSONObject doPut(String url, JSONObject object) throws IOException {

        RequestBody body = RequestBody.create(MediaType.parse("JSON"), object.toJSONString());
        Request req = new Request.Builder()
                .url(url)
                .put(body)
                .build();
        Response resp = MyHttpClient.exec(req);
        String str = resp.body().string();
        logger.error("返回数据：{}", str);
        if (resp.isSuccessful()) {
            JSONObject json = JSONObject.parseObject(str);
            return json;
        }
        return null;
    }


    /**
     * 组装请求form表单
     *
     * @param obj
     * @return
     */
    private static RequestBody getFormBody(JSONObject obj) {
        FormEncodingBuilder fb = new FormEncodingBuilder();
        for (String str : obj.keySet()) {
            if (obj.get(str) != null) {
                fb.add(str, obj.get(str).toString());
            }
        }
        return fb.build();
    }


}
