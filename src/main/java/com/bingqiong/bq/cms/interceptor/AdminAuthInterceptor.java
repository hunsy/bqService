//package com.bingqiong.bq.cms.interceptor;
//
//import com.alibaba.fastjson.JSON;
//import com.bingqiong.bq.model.user.User;
//import com.dome.store.user.BaUser;
//import com.jfinal.aop.Interceptor;
//import com.jfinal.aop.Invocation;
//import com.jfinal.core.Controller;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.servlet.http.HttpServletRequest;
//
///**
// * 权限拦截
// * <p>
// * Created by hunsy on 2017/6/23.
// */
//public class AdminAuthInterceptor implements Interceptor {
//
//    private Logger logger = LoggerFactory.getLogger(getClass());
//
//    public void intercept(Invocation inv) {
//        Controller controller = inv.getController();
//        HttpServletRequest request = controller.getRequest();
//
//        if (request.getRequestURI().startsWith("/admin")) {
//            //走到这步，一定是登录的用户
//            //因为这个是使用的domestore中的用户，非uc用户
//            //这里要进行一次切换，主动注册一个冰趣
//            BaUser baUser = (BaUser) controller.getSession().getAttribute("USER");
//            logger.info("baUser", JSON.toJSONString(baUser));
//            if (baUser != null) {
//                User user = null;
//                try {
//                    user = User.dao.findByUserId(baUser.getId() + "");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                logger.info("bqUser:{}", JSON.toJSONString(user));
//                if (user == null) {
//                    user = new User();
//                    user.set("user_id", baUser.getId());
//                    user.set("user_name", baUser.getUsername());
//                    User.dao.saveAdmin(user);
//                }
//                controller.setSessionAttr("bq_user", user);
//            }
//        }
//        inv.invoke();
//
//    }
//}
