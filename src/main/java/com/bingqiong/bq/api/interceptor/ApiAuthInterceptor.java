package com.bingqiong.bq.api.interceptor;

import com.bingqiong.bq.comm.constants.Constants;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.utils.TokenUtils;
import com.bingqiong.bq.comm.vo.ResponseEmptyVo;
import com.bingqiong.bq.model.user.User;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.redis.Redis;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 冰趣用户登录拦截
 * Created by hunsy on 2017/6/25.
 */
public class ApiAuthInterceptor implements Interceptor {

    private Logger logger = LoggerFactory.getLogger(getClass());
    //可登录可不登录的接口
    private static final String[] exceptions = new String[]{
            "/api/post/type/list",
            "/api/recommend/page",
            "/api/post/page",
            "/api/post/get",
            "/api/comment/page",
            "/api/comment/replies",
            "/api/user/queryUserInfo",
            "/api/user/groups",
            "/api/user/follows",
            "/api/user/fans",
            "/api/user/tags",
            "/api/user/posts",
            "/api/user/replies",
            "/api/group/list",
            "/api/group/get",
            "/api/group/all",
            "/api/search/group",
            "/api/search/post",
            "/api/sysmsg/page",
            "/api/pm/count",
            "/api/domeStore/isUpgrade",
            "/api/version/check"
    };

    public void intercept(Invocation invocation) {

        Controller controller = invocation.getController();
        HttpServletRequest request = controller.getRequest();
        String uri = request.getRequestURI();
        String dtoken = controller.getRequest().getHeader("token");
        logger.info("token:{}", dtoken);
//        logger.info("---->user:{}", JsonKit.toJson(controller.getSessionAttr("bq_user")));

        boolean auth = false;
        boolean exception = false;

        if (uri.startsWith("/api")) {

            //只有在没有token的情况下，忽略登录才有效
            //有token说明原来是登录的，所以报1004无权限错误
            if (ArrayUtils.contains(exceptions, uri) && dtoken == null) {
                exception = true;
            }

            if (StringUtils.isNotEmpty(dtoken)) {
                String uToken = Redis.use().get(Constants.REDIS_TOKEN_PRIFEX + dtoken);
                if (StringUtils.isNotEmpty(uToken)) {
                    controller.setAttr("accessToken", uToken);
                    try {
                        Map<String, String> map = TokenUtils.parseToken(uToken);
                        User user = User.dao.findByUserId(map.get("userId"));
                        controller.setAttr("bq_user", user);
                        auth = true;
                    } catch (Exception e) {

                    }
                }
            }

        } else {
            auth = true;
        }

        //没有token
        if (auth || exception) {
            invocation.invoke();
        } else {
            controller.renderJson(ResponseEmptyVo.noauth());
        }
    }
}
