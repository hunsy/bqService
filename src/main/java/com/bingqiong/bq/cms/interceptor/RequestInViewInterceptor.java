package com.bingqiong.bq.cms.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;

public class RequestInViewInterceptor implements Interceptor {


    public void intercept(Invocation inv) {
        inv.invoke();
        Controller c = inv.getController();
        c.setAttr("request", c.getRequest());
    }
}
