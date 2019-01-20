package com.bingqiong.bq.comm.utils;

import cn.jiguang.common.ClientConfig;
import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by hunsy on 2017/7/3.
 */
public class JpushUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private static JpushUtil util = null;
    private static JPushClient client;

    private JpushUtil() {
        Prop prop = PropKit.use("params.properties");
        client = new JPushClient(
                prop.get("jpush.secret"),
                prop.get("jpush.key"),
//                "3d764ff2dd1fa1673f3fd9b5",
//                "100cfe6b35a37805e36370b8",
                null,
                ClientConfig.getInstance()
        );
    }

    public static JpushUtil getInstance() {

        if (util == null) {
            util = new JpushUtil();
        }
        return util;
    }


    /**
     * 组装payload
     *
     * @param msg
     */
    public PushPayload buildPayload(String msg, String tag) {

        PushPayload pushPayload = PushPayload.newBuilder()
                .setAudience(Audience.tag(tag))
                .setMessage(Message.content(msg))
                .setPlatform(Platform.android())
//                .setNotification(No)
                .build();
        return pushPayload;
    }

    /**
     * 发送私信
     *
     * @param msg
     * @param tag
     * @return
     */
    public Long pushPm(String msg, String tag) {
        try {
            JSONObject object = new JSONObject();
            object.put("type", "pm");
            object.put("id", msg);
            PushPayload pushPayload = buildPayload(object.toJSONString(), tag);
            PushResult result = client.sendPush(pushPayload);
            return result.msg_id;
        } catch (APIConnectionException e) {
            e.printStackTrace();
        } catch (APIRequestException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 发送私信
     *
     * @param msg
     * @param tag
     * @return
     */
    public Long pushRep(String msg, String tag) {
        try {
            JSONObject object = new JSONObject();
            object.put("type", "rep");
            object.put("id", msg);
            PushPayload pushPayload = buildPayload(object.toJSONString(), tag);
            PushResult result = client.sendPush(pushPayload);
            logger.info("发送私信结果:{}", JsonKit.toJson(result));
            return result.msg_id;
        } catch (APIConnectionException e) {
            e.printStackTrace();
        } catch (APIRequestException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 发送系统消息
     *
     * @param msg
     * @throws APIConnectionException
     * @throws APIRequestException
     */
    public void setSysMsg(String msg) throws APIConnectionException, APIRequestException {
        System.out.println(msg);
        JSONObject object = new JSONObject();
        object.put("type", "sm");
        object.put("id", msg);
//        PushPayload pushPayload = PushPayload.newBuilder()
//                .setAudience(Audience.all())
//                .setMessage(Message.content(object.toJSONString()))
//                .setPlatform(Platform.android())
////                .setNotification(No)
//                .build();
        PushPayload pushPayload = PushPayload.messageAll(object.toJSONString());
        PushResult result = client.sendPush(pushPayload);
        logger.info("发送系统消息结果:{}", JsonKit.toJson(result));
    }


    public static void main(String[] args) {

//        PushPayload pushPayload = buildPayload("11111111111111");
//        getInstance().push(pushPayload);

        try {
            getInstance().setSysMsg("222222222222222");
        } catch (APIConnectionException e) {
            e.printStackTrace();
        } catch (APIRequestException e) {
            e.printStackTrace();
        }
    }

}
