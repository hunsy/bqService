package com.bingqiong.bq.comm.http;

import com.squareup.okhttp.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by hunsy on 2017/5/2.
 */
public class MyHttpHolder {

    private static MyHttpHolder myHttpHolder = null;
    private OkHttpClient client = null;

    private MyHttpHolder() {
        client = new OkHttpClient();
        client.setWriteTimeout(30, TimeUnit.SECONDS);
        client.setReadTimeout(30, TimeUnit.SECONDS);
        client.setConnectionPool(new ConnectionPool(100, 60));
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
