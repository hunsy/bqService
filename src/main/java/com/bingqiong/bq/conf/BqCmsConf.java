package com.bingqiong.bq.conf;

import com.bingqiong.bq.api.controller.ApiRoutes;
import com.bingqiong.bq.api.interceptor.ApiAuthInterceptor;
import com.bingqiong.bq.api.interceptor.ApiParamInterceptor;
import com.bingqiong.bq.cms.controller.AdminRoutes;
import com.bingqiong.bq.comm.constants.EsIndexType;
import com.bingqiong.bq.comm.interceptor.GlobalInterceptor;
import com.bingqiong.bq.comm.interceptor.RequestInViewInterceptor;
import com.bingqiong.bq.comm.utils.EsUtils;
import com.bingqiong.bq.comm.utils.QiNiuUtil;
import com.bingqiong.bq.comm.utils.SensetiveUtil;
import com.bingqiong.bq.model._MappingKit;
import com.bingqiong.bq.model.category.Group;
import com.bingqiong.bq.model.post.Post;
import com.bingqiong.bq.model.user.User;
import com.jfinal.config.*;
import com.jfinal.core.JFinal;
import com.jfinal.ext.interceptor.SessionInViewInterceptor;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.jfinal.plugin.redis.RedisPlugin;
import com.jfinal.render.FreeMarkerRender;
import com.jfinal.render.ViewType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by hunsy on 2017/6/21.
 */
public class BqCmsConf extends JFinalConfig {


    public static boolean enc = false;
    public static final String ADMIN_ID = "sys_000001";
    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final String CAPTCHA_DIR = "/var/captcha/";
    //    public static final String CAPTCHA_DIR = "e:\\imgs\\";
    public static boolean dev = false;

    public void configConstant(Constants me) {
        loadPropertyFile("jdbc.properties");
        me.setBaseViewPath("/WEB-INF/page");
        dev = getPropertyToBoolean("devMode", false);
        me.setDevMode(dev);
        me.setViewType(ViewType.FREE_MARKER);

    }

    /**
     * 路由
     *
     * @param me
     */
    public void configRoute(Routes me) {
        //后端接口
        AdminRoutes._routes(me);
        //app接口
        ApiRoutes._route(me);

    }

    public void configPlugin(Plugins me) {
        DruidPlugin druidPlugin = new DruidPlugin(getProperty("jdbcUrl"), getProperty("user"), getProperty("password"));

        me.add(druidPlugin);
        ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
        arp.setShowSql(true);
        me.add(arp);
        //添加映射
        _MappingKit.mapping(arp);

        //redis配置
        Prop redisConf = PropKit.use("redis.properties");
        RedisPlugin rp = new RedisPlugin(
                redisConf.get("redis.cacheName"),
                redisConf.get("redis.host", "localhost"),
                redisConf.getInt("redis.port", 6379)
        );
        me.add(rp);
    }

    public void configInterceptor(Interceptors me) {
        me.add(new SessionInViewInterceptor());
        me.add(new GlobalInterceptor());
//        me.add(new AdminAuthInterceptor());
        me.add(new ApiAuthInterceptor());
        me.add(new ApiParamInterceptor());
        me.add(new RequestInViewInterceptor());
    }

    public void configHandler(Handlers me) {

    }

    @Override
    public void afterJFinalStart() {
        //初始化elastic search工具
        Prop prop = PropKit.use("params.properties");
        try {
            EsUtils.getInstance().init(prop.getProperties());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        enc = prop.getBoolean("mobile.enc");

        //初始化七牛上传工具
        QiNiuUtil.getInstance().init(PropKit.use("qiniu.properties").getProperties());

//        //初始化系统标签
//        try {
//            Inits.init();
//        } catch (BizException e) {
//            e.printStackTrace();
//        }

        try {
            User admin = User.dao.findByUserId("sys_000001");

            if (admin == null) {
                admin = new User();
                admin.set("user_id", "sys_000001");
                admin.set("user_name", "冰趣管理员");
                admin.set("avatar_url", "https://qn-store.qbcdn.com/2017071415240306557.png");
                admin.set("valid", 1);
                User.dao.saveAdmin(admin);
            } else {
                admin.set("avatar_url", "https://qn-store.qbcdn.com/2017071415240306557.png");
                admin.update();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        //初始化敏感词
        //清空原有记录
        Cache cache = Redis.use();
        cache.del(com.bingqiong.bq.comm.constants.Constants.REDIS_SENSITIVE_KEY);
        List<Record> ls = Db.find("select text from t_sensitive");
        Set<String> lz = new HashSet<>();
        if (ls != null && ls.size() > 0) {
            //增加新纪录
            for (Record r : ls) {
                String text = r.getStr("text");
                cache.sadd(com.bingqiong.bq.comm.constants.Constants.REDIS_SENSITIVE_KEY, text);
                lz.add(text);
            }
        }
        SensetiveUtil.getInstance().addSensitiveWords(lz);
        FreeMarkerRender.setProperties(PropKit.use("freemarker.properties").getProperties());
        super.afterJFinalStart();
    }


    /**
     * 启动项目
     *
     * @param args
     */
    public static void main(String[] args) {
        JFinal.start("src/main/webapp", 9901, "/", 20);
    }

}
