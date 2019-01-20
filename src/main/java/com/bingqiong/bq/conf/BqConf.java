package com.bingqiong.bq.conf;

import com.bingqiong.bq.constant.BqConstants;
import com.bingqiong.bq.interceptor.GlobalInterceptor;
import com.bingqiong.bq.model._MappingKit;
import com.bingqiong.bq.utils.ChannelsUtils;
import com.bingqiong.bq.utils.IKAnalyzerUtil;
import com.bingqiong.bq.utils.MyAesUtil;
import com.bingqiong.bq.utils.QiNiuUtil;
import com.jfinal.config.*;
import com.jfinal.core.JFinal;
import com.jfinal.ext.interceptor.SessionInViewInterceptor;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.jfinal.plugin.redis.RedisPlugin;
import com.jfinal.render.ViewType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author hunsy
 */
public class BqConf extends JFinalConfig {

    public static int REQUEST_MAX_CONTENT_LENGHT;

    private Logger logger = LoggerFactory.getLogger(BqConf.class);

    //文件存储的根路径
    public static String CACHE_NAME = "";

    /**
     * 常量配置
     *
     * @param me
     */
    @Override
    public void configConstant(Constants me) {
        loadPropertyFile("jdbc.properties");
        me.setDevMode(getPropertyToBoolean("devMode"));

        me.setBaseViewPath("/WEB-INF/views/");
        me.setViewType(ViewType.FREE_MARKER);
        //最多上传2M
        REQUEST_MAX_CONTENT_LENGHT = 1024 * 1024 * 100;
        me.setUploadedFileSaveDirectory("upload");
        me.setMaxPostSize(REQUEST_MAX_CONTENT_LENGHT);
        //初始化七牛上传工具
        QiNiuUtil.getInstance().init(PropKit.use("qiniu.txt").getProperties());
        //加解密工具初始化
//        MyAesUtil.getInstance().init(getProperty("passKey"));
        me.setError404View("404.html");
    }

    /**
     * 路由配置。
     *
     * @param me
     */
    public void configRoute(Routes me) {
        me.add(new AdminRoutes());
        me.add(new ApiRoutes());
    }

    public void configPlugin(Plugins me) {

        DruidPlugin druidPlugin = new DruidPlugin(
                getProperty("jdbcUrl"),
                getProperty("user"),
                getProperty("password"));

        me.add(druidPlugin);
        ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
        arp.setShowSql(true);
        _MappingKit.mapping(arp);
        arp.setShowSql(true);
        me.add(arp);
        //redis配置
        Prop redisConf = PropKit.use("redis.properties");
        CACHE_NAME = redisConf.get("redis.cacheName");
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
//		me.add(new RequestInViewInterceptor());
//		me.add(new AuthInterceptor());
    }

    public void configHandler(Handlers me) {

    }

    @Override
    public void afterJFinalStart() {
//        InputStream is = getClass().getResourceAsStream("/words.txt");
//        try {
//            Iterator<String> ite = IOUtils.lineIterator(is, "utf-8");
//            while (ite.hasNext()) {
//                String key = ite.next();
//                logger.info("key->{}", key);
////                Redis.use().sadd(BqConstants.REDIS_SENSITIVE_KEY, key);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        Properties properties = loadPropertyFile("channels.txt");
        ChannelsUtils.getInstance().init(properties);
        //清空原有记录
        Cache cache = Redis.use();
        cache.del(BqConstants.REDIS_SENSITIVE_KEY);
        List<Record> ls = Db.find("select text from t_sensitive");
        Set<String> lz = new HashSet<>();
        if (ls != null && ls.size() > 0) {
            //增加新纪录
            for (Record r : ls) {
                String text = r.getStr("text");
                cache.sadd(BqConstants.REDIS_SENSITIVE_KEY, text);
                lz.add(text);
            }
        }
        IKAnalyzerUtil.extendDic(lz);
    }

    /**
     * 启动项目
     *
     * @param args
     */
    public static void main(String[] args) {
        JFinal.start("src/main/webapp", 89, "/", 5);
    }
}
