package com.bingqiong.bq.comm.http;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.PropKit;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by hunsy on 2017/5/10.
 */
public class UcApi {

    private static Logger logger = LoggerFactory.getLogger(UcApi.class);

    private static UcApi api = null;
    private static Properties properties = new Properties();

    private UcApi() {
        properties = PropKit.use("ucapi.properties").getProperties();
    }

    /**
     * @return
     */
    public static UcApi getInstance() {
        if (api == null) {
            api = new UcApi();
        }
        return api;
    }

    /**
     * @param obj
     */
    public JSONObject doreq(JSONObject obj, String key) throws IOException {
        logger.info("\n");
        logger.info("开始请求:{}>>>>", key);
        logger.info("请求参数:{}", obj.toJSONString());
        Request req = new Request.Builder()
                .url(properties.getProperty("host") + properties.getProperty(key))
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
     * 组装请求form表单
     *
     * @param obj
     * @return
     */
    private RequestBody getFormBody(JSONObject obj) {
        FormEncodingBuilder fb = new FormEncodingBuilder();
        for (String str : obj.keySet()) {
            if (obj.get(str) != null) {
                fb.add(str, obj.get(str).toString());
            }
        }
        return fb.build();
    }

}
