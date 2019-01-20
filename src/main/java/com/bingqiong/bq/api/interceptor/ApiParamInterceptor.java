package com.bingqiong.bq.api.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.comm.utils.DESUtil;
import com.bingqiong.bq.conf.BqCmsConf;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;

/**
 * 拦截获取参数
 * Created by hunsy on 2017/6/28.
 */
public class ApiParamInterceptor implements Interceptor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void intercept(Invocation inv) {

        Controller controller = inv.getController();
        HttpServletRequest request = controller.getRequest();
        JSONObject object = null;
        if (request.getRequestURI().startsWith("/api")) {

            String paramStr = HttpKit.readIncommingRequestData(request);
//            logger.info("请求参数:{}", paramStr);
            //有参数
            if (StringUtils.isNotEmpty(paramStr)) {
                if (BqCmsConf.enc) {
                    paramStr = DESUtil.decrypt(paramStr);
//                    logger.info("请求参数解密后:{}", paramStr);
                }
                object = JSON.parseObject(paramStr);
            } else {
                object = new JSONObject();
            }
//            else {
//                controller.removeSessionAttr("params");
//            }

            controller.setAttr("params", object);
            JSONObject mobileInfo = new JSONObject();
            //获取平台信息 ios | android
            String platform = request.getHeader("platform");
            if (platform != null) {
                mobileInfo.put("platform", platform);
            }
            //增加设备
            String deviceId = request.getHeader("deviceId");
            mobileInfo.put("deviceId", deviceId);
            controller.setAttr("mobileInfo", mobileInfo);
        }

        inv.invoke();
    }
}
