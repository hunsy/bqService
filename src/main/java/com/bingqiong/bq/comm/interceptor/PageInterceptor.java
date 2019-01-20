package com.bingqiong.bq.comm.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * 组装分页查询条件
 * <p>
 * Created by hunsy on 2017/5/23.
 */
public class PageInterceptor implements Interceptor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void intercept(Invocation inv) {

        Controller con = inv.getController();
        HttpServletRequest request = con.getRequest();
        PageRequest pageRequest = new PageRequest();
        String uri = request.getRequestURI();

        if (StringUtils.startsWith(uri, "/admin")) {
            if (StringUtils.isNotEmpty(request.getParameter("isPage")) && !Boolean.parseBoolean(request.getParameter("isPage"))) {
                pageRequest.setPageNo(1);
                pageRequest.setPageSize(999);
            } else {
                if (StringUtils.isNotEmpty(request.getParameter("pageNo"))) {
                    pageRequest.setPageNo(Integer.parseInt(request.getParameter("pageNo")));
                }
                if (StringUtils.isNotEmpty(request.getParameter("pageSize"))) {
                    pageRequest.setPageSize(Integer.parseInt(request.getParameter("pageSize")));
                }
            }

            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String name = paramNames.nextElement();
                if (name.startsWith("param_")) {
                    String obj = request.getParameter(name);
                    if (StringUtils.isNotEmpty(obj)) {
                        pageRequest.getParams().put(name.replace("param_", ""), obj);
                    }
                }
            }
            //app端的分页
        } else if (StringUtils.startsWith(uri, "/api")) {

            JSONObject params = con.getAttr("params");
            if (params != null) {
                Iterator<String> ite = params.keySet().iterator();
                while (ite.hasNext()) {
                    String key = ite.next();
                    if (key.equals("pageNo")) {
                        pageRequest.setPageNo(params.getIntValue("pageNo"));
                    } else if (key.equals("pageSize")) {
                        pageRequest.setPageSize(params.getIntValue("pageSize"));
                    } else {
                        String val = params.getString(key);
                        if (StringUtils.isNotEmpty(val)) {
                            pageRequest.getParams().put(key, val);
                        }
                    }
                }
            }
            logger.info(JsonKit.toJson(pageRequest));
        }

        con.setAttr("pageRequest", pageRequest);
        inv.invoke();
    }
}
