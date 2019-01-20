package com.bingqiong.bq.utils;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by hunsy on 2017/5/2.
 */
public class MyHttpHolder {

    private static MyHttpHolder myHttpHolder = null;
    private OkHttpClient client = null;

    private MyHttpHolder() {
        client = new OkHttpClient.Builder()
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(100, 60, TimeUnit.SECONDS))
                .build();
    }

    public static MyHttpHolder getInstance() {
        if (myHttpHolder == null) {
            myHttpHolder = new MyHttpHolder();
        }
        return myHttpHolder;
    }

    /**
     * 同步
     *
     * @param req
     * @return
     */
    public Response exec(Request req) {
        try {
            Response resp = client.newCall(req).execute();
            return resp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 异步
     *
     * @param req
     * @param callback
     */
    public void exec(Request req, Callback callback) {
        client.newCall(req).enqueue(callback);
    }
}
