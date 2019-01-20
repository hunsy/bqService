package com.bingqiong.bq.interceptor;

import com.bingqiong.bq.vo.PageRequest;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by hunsy on 2017/5/23.
 */
public class PageInterceptor implements Interceptor {


    @Override
    public void intercept(Invocation inv) {

        Controller con = inv.getController();
        HttpServletRequest request = con.getRequest();
        PageRequest pi = new PageRequest();
        if (StringUtils.isNotEmpty(request.getParameter("isPage")) && !Boolean.parseBoolean(request.getParameter("isPage"))) {
            pi.setPageNo(1);
            pi.setPageSize(999);
        } else {
            if (StringUtils.isNotEmpty(request.getParameter("pageNo"))) {
                pi.setPageNo(Integer.parseInt(request.getParameter("pageNo")));
            }
            if (StringUtils.isNotEmpty(request.getParameter("pageSize"))) {
                pi.setPageSize(Integer.parseInt(request.getParameter("pageSize")));
            }
        }

        con.setAttr("pageRequest", pi);
        inv.invoke();
    }
}
