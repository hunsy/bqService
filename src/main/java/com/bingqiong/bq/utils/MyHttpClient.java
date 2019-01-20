package com.bingqiong.bq.utils;

import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by hunsy on 2017/1/4.
 */
public class MyHttpClient {


    public static void exec(Request req, Callback cb) {
        MyHttpHolder.getInstance().exec(req, cb);
    }

    public static Response exec(Request req) {
        return MyHttpHolder.getInstance().exec(req);
    }
}
