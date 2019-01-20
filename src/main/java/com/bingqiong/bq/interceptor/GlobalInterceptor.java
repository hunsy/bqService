package com.bingqiong.bq.interceptor;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;

public class GlobalInterceptor implements Interceptor {

    private Logger logger = LoggerFactory.getLogger(GlobalInterceptor.class);

    @Override
    public void intercept(Invocation in) {

        //跨域支持
        Controller ctr = in.getController();
        ctr.getResponse().setHeader("Access-Control-Allow-Origin", "*");
        ctr.getResponse().setHeader("Access-Control-Allow-Headers", "Origin,X-Requested-With,Content-Type,Accept,Authorization,dtoken,Content-Disposition");
        ctr.setAttr("startTime", System.currentTimeMillis());
        //OPTIONS操作
        logger.info("\n");
        logger.info("request start:------------------------------------------->>>");
        HttpServletRequest request = ctr.getRequest();
        if (request.getMethod().toUpperCase().equals("OPTIONS")) {
            ctr.renderNull();
        } else {
            logger.info("request method:{}", request.getMethod());
            logger.info("request path:{}", request.getRequestURI());
            Enumeration<String> emus = request.getParameterNames();
            if (emus != null) {
                logger.info("request parameters:{}", JsonKit.toJson(emus));
            }
            in.invoke();
            long startTime = (long) request.getAttribute("startTime");
            long time = System.currentTimeMillis() - startTime;
            logger.info("request use time:{} {}", time, "ms");
            logger.info("request end:------------------------------------------->>>", time);
        }

    }

}
