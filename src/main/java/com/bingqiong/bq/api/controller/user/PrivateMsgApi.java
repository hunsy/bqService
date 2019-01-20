package com.bingqiong.bq.api.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.conf.BqCmsConf;
import com.bingqiong.bq.model.comm.SysMsg;
import com.bingqiong.bq.model.comment.Comment;
import com.bingqiong.bq.model.msg.MsgReadAt;
import com.bingqiong.bq.model.msg.PrivateMsg;
import com.bingqiong.bq.model.user.User;
import com.bingqiong.bq.model.user.UserShields;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 私信
 * Created by hunsy on 2017/7/5.
 */
public class PrivateMsgApi extends IBaseController {


    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 发送私信
     */
    public void send() {

        try {
            //当前用户
            User user = getAttr("bq_user");
            //获取发送人
            //获取发送内容
            JSONObject object = getAttr("params");
            if (object == null || object.getString("to") == null || object.getString("content") == null) {
                throw new BizException(ErrorCode.MISSING_PARM);
            }

            String to = object.getString("to");
            //查询对方是否屏蔽当前用户
            boolean shield = UserShields.dao.shiled(to, user.getStr("user_id"));
            if (shield) {
                throw new BizException(ErrorCode.USER_SHIELD_EXIST);
            }
            String content = object.getString("content");
            PrivateMsg.dao.send(user.getStr("user_id"), to, content);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    /**
     * 获取消息
     */
    @Before(PageInterceptor.class)
    public void pms() {

        try {
            PageRequest pageRequest = getAttr("pageRequest");
            Map<String, String> params = pageRequest.getParams();
            //消息来自
            Object from = params.get("from");
            if (from == null) {

                throw new BizException(ErrorCode.MISSING_PARM);
            }
            //当前用户
            User user = getAttr("bq_user");
            params.put("to", user.getStr("user_id"));
            Page<Record> page = PrivateMsg.dao.findPage(pageRequest);
            renderSuccess(page, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 获取消息数量
     */
    public void count() {

        try {
            User user = getAttr("bq_user");
            logger.info("{}", JsonKit.toJson(user));

            //系统消息是以deviceId设备号未标记的
            String deviceId = getRequest().getHeader("deviceId");
            //总记录数
            long total = 0;

            long sysCount = 0;
            //回复数
            long replyCount = 0;
            //私信数量记录
            List<Record> records = new ArrayList<>();
            //只有用户登录，才获取回复数和私信数量
            if (user != null) {
                String user_id = user.getStr("user_id");
                //系统消息数
                sysCount = SysMsg.dao.getMsgCount(user_id);
                total = total + sysCount;
                //回复数量
                replyCount = Comment.dao.findReplyCount(user_id);
                total = total + replyCount;
                //私信
                List<Record> tos = PrivateMsg.dao.msgcount(user_id);
                if (CollectionUtils.isNotEmpty(tos)) {
                    records.addAll(tos);
                    for (Record record : tos) {
                        total = total + record.getInt("count");
                    }
                }
            } else {
                //系统消息数
                sysCount = SysMsg.dao.getMsgCount(deviceId);
                total = total + sysCount;
            }
            Record record = new Record();
            record.set("total", total);
            record.set("replyCount", replyCount);
            record.set("pms", records);
            record.set("sysCount", sysCount);
            renderSuccess(record, BqCmsConf.enc);
        } catch (Exception e) {
            handleException(e);
        }
    }


//    /**
//     * 删除
//     */
//    public void delete() {
//
//        try {
//
//        } catch (Exception e) {
//            renderFailure(e);
//        }
//    }

}
