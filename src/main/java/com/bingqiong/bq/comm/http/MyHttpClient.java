package com.bingqiong.bq.comm.http;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

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
