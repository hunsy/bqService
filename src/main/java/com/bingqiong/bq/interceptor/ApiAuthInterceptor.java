package com.bingqiong.bq.interceptor;

import com.bingqiong.bq.constant.BqConstants;
import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.vo.ResponseEmptyVo;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.plugin.redis.Redis;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * api部分的权限验证
 * <p>
 * Created by hunsy on 2017/5/22.
 */
public class ApiAuthInterceptor implements Interceptor {
    @Override
    public void intercept(Invocation inv) {

        Controller controller = inv.getController();
        HttpServletRequest request = controller.getRequest();
        String token = request.getHeader("token");
        if (StringUtils.isEmpty(token) || !Redis.use().sismember(BqConstants.BQ_APPLICATION + "user:access:token", token)) {
            controller.renderJson(ResponseEmptyVo.failure(BqErrorCode.ACCESS_REJECT));
        } else {
            inv.invoke();
        }
    }
}
