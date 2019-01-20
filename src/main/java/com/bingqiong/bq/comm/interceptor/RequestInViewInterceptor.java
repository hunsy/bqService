package com.bingqiong.bq.comm.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;

public class RequestInViewInterceptor implements Interceptor {

    public void intercept(Invocation ai) {
        ai.invoke();
        Controller c = ai.getController();
        c.setAttr("request", c.getRequest());
    }

}
