package com.bingqiong.bq.controller.api.user;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.bingqiong.bq.interceptor.ApiAuthInterceptor;
import com.jfinal.aop.Before;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.conf.ErrorCode;
import com.bingqiong.bq.constant.BqConstants;
import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.controller.admin.BaseController;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.exception.UBizException;
import com.bingqiong.bq.model.Card;
import com.bingqiong.bq.model.uc.User;
import com.bingqiong.bq.utils.AESUtils;
import com.bingqiong.bq.utils.EncodeUtils;
import com.bingqiong.bq.utils.RequestUtil;
import com.bingqiong.bq.utils.UcApi;
import com.bingqiong.bq.vo.ResponseEmptyVo;
import com.bingqiong.bq.vo.ResponseMobileDataVo;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Redis;
import sun.misc.BASE64Decoder;

/**
 * Created by hunsy on 2017/5/10.
 */
public class UserApi extends BaseController {


    /**
     * ->params[
     * mobile,
     * bizType]
     * ->return
     * {
     * "code": "0",
     * "message": "成功"
     * }
     * 获取验证码
     */
    public void getSmsCode() {
        String msg = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            obj.put("buId", BqConstants.BUID);
            obj.put("clientId", "");
            validateBizType(obj.getString("bizType"));
            //验证手机号码
            validateMobileInner(obj.getString("mobile"));
            //请求uc服务
            JSONObject json = UcApi.getInstance().doreq(obj, "getCode");
            if (json.getString("code").equals("0")) {
                renderJson(ResponseMobileDataVo.success("获取验证码成功", EncodeUtils.isEncode()));
            } else {
                msg = ErrorCode.getFromKey(json.getString("code")).getMessage();
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
        } catch (Exception e) {
            handleException(e, msg);
        }
    }

    /**
     * 验证验证码
     * <p>
     * ->params [
     * mobile,
     * clientId,
     * buId,
     * bizType,
     * mobileCode,
     * userIp
     * ]
     * ->return
     * {
     * "code": "0",
     * "message": "成功"
     * }
     */
    public void checkSmsCode() {
        String msg = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            obj.put("buId", BqConstants.BUID);
            obj.put("clientId", "");
            //验证验证码不为空
            if (StringUtils.isEmpty(obj.getString("mobileCode"))) {
                logger.error("验证码为空");
                msg = "验证码不能为空";
                throw new UBizException(ErrorCode.VERIFY_CODE_NULL.getCode());
            }
            //验证BizType是否存在
            validateBizType(obj.getString("bizType"));
            //验证手机号码
            validateMobileInner(obj.getString("mobile"));
            //请求uc服务
            JSONObject json = UcApi.getInstance().doreq(obj, "verifyCode");
            JSONObject data = json.getJSONObject("data");
            if (json.getString("code").equals("0")) {
                renderJson(ResponseMobileDataVo.success(data, EncodeUtils.isEncode()));
            } else {
                msg = ErrorCode.getFromKey(json.getString("code")).getMessage();
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
        } catch (Exception e) {
            handleException(e, msg);
        }
    }


    /**
     * ->params[
     * passport,
     * password,
     * clientId,
     * buId,
     * mobileCode]
     * ->return
     * {
     * "code": "0",
     * "data": {
     * "domeUserId":"bq_000050011",
     * "domeUserName":"mwqhvsok",
     * "accessToken": "MTIz|YnFfODQwNTA2MDAw|1462342334917|c8a99fe75a07e87414a2b7fb01ef3c48"
     * },
     * "message": "成功"
     * }
     * <p>
     * 注册。
     */
    public void register() {
        String msg = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            String password = obj.getString("password");
            if (StringUtils.isEmpty(obj.getString("mobileCode"))) {
                logger.error("验证码为空");
                msg = "验证码不能为空";
                throw new UBizException(ErrorCode.VERIFY_CODE_NULL.getCode());
            }
            obj.put("bizType", 1);
            obj.put("buId", BqConstants.BUID);
            //验证手机号码
            validateMobileInner(obj.getString("passport"));

            validatePassword(password);

            logger.info("password:{}", password);
            String temp = AESUtils.encrypt(password);
            logger.info("加密后temp:{}", temp);
            //密码加密
            obj.put("password", temp);
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "register");
            JSONObject data = resp.getJSONObject("data");
            //注册成功
            if (resp.get("code").equals("0")) {
                String uid = data.getString(BqConstants.DOME_USER_ID);
                //保存本地记录
                if (uid != null) {
                    logger.info("保存本User");
                    Record record = new Record();
                    record.set("user_id", uid);
                    record.set("user_name", data.getString(BqConstants.DOME_USER_NAME));
                    record.set("mobile", obj.getString("passport"));
                    Db.save("uc_user", record);
                }
                renderJson(ResponseMobileDataVo.success(resp.getJSONObject("data"), EncodeUtils.isEncode()));
            } else {
                msg = ErrorCode.getFromKey(resp.getString("code")).getMessage();
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

        } catch (Exception e) {
            handleException(e, msg);
        }
    }

    /**
     * 登录<br/>
     * passport
     * password
     * &clientId
     * buId
     */
    public void login() {

        String msg = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());

            obj.put("buId", BqConstants.BUID);
            //验证手机号码
            validateMobileInner(obj.getString("passport"));
            //验证密码格式
            validatePassword(obj.getString("password"));

            //获取登录限制
            Long num = Redis.use().incrBy(BqConstants.BQ_APPLICATION + "login:limit:" + obj.getString("passport"), 1);
            if (num < 5) {
                //密码加密
                obj.put("password", AESUtils.encrypt(obj.getString("password")));
                //请求uc服务
                JSONObject resp = UcApi.getInstance().doreq(obj, "login");
                JSONObject data = resp.getJSONObject("data");
                //注册成功
                if (resp.get("code").equals("0")) {

                    //
                    String accessToken = data.getString("accessToken");
                    Redis.use().sadd(BqConstants.BQ_APPLICATION + "user:access:token", accessToken);
                    Redis.use().expire(BqConstants.BQ_APPLICATION + "user:access:token", 15 * 24 * 60 * 60 - 60);
//                    Redis.use().expire(BqConstants.BQ_APPLICATION + "user:access:token", 2 * 60);
                    //去除登录次数限制
                    Redis.use().del(BqConstants.BQ_APPLICATION + "login:limit:" + obj.getString("passport"));
                    renderJson(ResponseMobileDataVo.success(data, EncodeUtils.isEncode()));
                } else {
                    msg = ErrorCode.getFromKey(resp.getString("code")).getMessage();
                    throw new BizException(BqErrorCode.CODE_FAILED.getCode());
                }
            } else {
                Map<String,Object> map = new HashMap<>();
                map.put("needCaptcha",true);
                renderJson(ResponseMobileDataVo.success(map, EncodeUtils.isEncode()));
            }

        } catch (Exception e) {
            handleException(e, msg);
        }

    }

    /**
     * 获取验证码
     */
    public void getCaptcha() {

        String msg = "";
        try {
            JSONObject obj = new JSONObject();
            obj.put("buId", BqConstants.BUID);
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "getValidateCode");
            JSONObject data = resp.getJSONObject("data");
            if (resp.get("code").equals("0")) {
                String b64 = data.getString("captcha");
                byte[] bt64 = new BASE64Decoder().decodeBuffer(b64);
                String root = PathKit.getWebRootPath();
                String filePath = "/captcha/" +
                        DateTime.now().toString("yyyy-MM-dd") + "/" +
                        DateTime.now().toString("yyyyMMddHHmmsss") + UUID.randomUUID().toString() + ".png";
                FileUtils.writeByteArrayToFile(new File(root + filePath), bt64);
                data.put("captcha", filePath);
                renderJson(ResponseMobileDataVo.success(data, EncodeUtils.isEncode()));
            } else {
                msg = ErrorCode.getFromKey(resp.getString("code")).getMessage();
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
        } catch (Exception e) {
            handleException(e, msg);
        }
    }

    /**
     * 获取用户信息
     * accessToken=xxxx &clientId=xxx&buId=xxx
     */
    @Before(ApiAuthInterceptor.class)
    public void queryUserInfo() {

        String msg = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            obj.put("buId", BqConstants.BUID);
            if (StringUtils.isEmpty(obj.getString("accessToken"))) {
                logger.error("accessToken为空");
                throw new UBizException(ErrorCode.SYSTEM_EXCEPTION.getCode());
            }

            obj.put("accessToken", AESUtils.encrypt(obj.getString("accessToken")));
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "queryUserInfo");
            JSONObject data = resp.getJSONObject("data");
            //
            if (resp.get("code").equals("0")) {
                renderJson(ResponseMobileDataVo.success(data, EncodeUtils.isEncode()));
            } else {
                msg = ErrorCode.getFromKey(resp.getString("code")).getMessage();
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
        } catch (Exception e) {
            handleException(e, msg);
        }
    }

    /**
     * 获取修改性别
     * accessToken=xxxx &clientId=xxx&buId=xxx
     */
    @Before(ApiAuthInterceptor.class)
    public void editGender() {
        String msg = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            obj.put("buId", BqConstants.BUID);
            if (StringUtils.isEmpty(obj.getString("accessToken"))) {
                logger.error("accessToken为空");
                throw new UBizException(ErrorCode.SYSTEM_EXCEPTION.getCode());
            }
            if (StringUtils.isEmpty(obj.getString("gender"))) {
                logger.error("gender为空");
                msg = "性别不能为空";
                throw new UBizException(ErrorCode.SYSTEM_EXCEPTION.getCode());
            }
            obj.put("accessToken", AESUtils.encrypt(obj.getString("accessToken")));
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "editGender");
            //
            if (resp.get("code").equals("0")) {

                Record user = User.dao.getByUid(parseToken(obj.getString("accessToken")).get(BqConstants.DOME_USER_ID));
                user.set("gender", obj.getString("gender"));
                User.dao.updateUser(user);
                renderJson(ResponseEmptyVo.success());
            } else {
                msg = ErrorCode.getFromKey(resp.getString("code")).getMessage();
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
        } catch (Exception e) {
            handleException(e, msg);
        }
    }

    /**
     * 修改密码
     * mobile=18061651013&passwordOld=xxx
     * & passwordNew=xxx&clientId=xxx&buId=xxx
     */
    @Before(ApiAuthInterceptor.class)
    public void editPassword() {
        String msg = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            obj.put("buId", BqConstants.BUID);

            if (StringUtils.isEmpty(obj.getString("accessToken"))) {
                logger.error("accessToken为空");
                throw new UBizException(ErrorCode.SYSTEM_EXCEPTION.getCode());
            }
            //验证就密码格式
            validatePassword(obj.getString("passwordOld"));
            //验证新密码格式
            validatePassword(obj.getString("passwordNew"));
//
//            if (StringUtils.isEmpty(obj.getString("passwordOld")) || StringUtils.isEmpty(obj.getString("passwordNew"))) {
//                logger.error("passwordOld为空");
//                msg = "密码不能为空";
//                throw new UBizException(ErrorCode.PASSWORD_NULL.getCode());
//            }
            obj.put("passwordOld", AESUtils.encrypt(obj.getString("passwordOld")));
            obj.put("passwordNew", AESUtils.encrypt(obj.getString("passwordNew")));
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "editPassword");
            //
            if (resp.get("code").equals("0")) {
                renderJson(ResponseEmptyVo.success());
            } else {
                msg = ErrorCode.getFromKey(resp.getString("code")).getMessage();
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
        } catch (Exception e) {
            handleException(e, msg);
        }
    }

    /**
     * 重置密码
     * mobile=18061651013&password=xxx
     * &smsToken=xxx&clientId=xxx&buId=xxx
     */
    public void setNewPassword() {
        String msg = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            obj.put("buId", BqConstants.BUID);
            validateMobileInner(obj.getString("mobile"));
            //验证码为空
            if (StringUtils.isEmpty(obj.getString("smsToken"))) {
                logger.error("smsToken为空");
                throw new UBizException(ErrorCode.VERIFY_CODE_NULL.getCode());
            }

            if (StringUtils.isEmpty(obj.getString("password"))) {
                logger.error("password为空");
                msg = "密码不能为空";
                throw new UBizException(ErrorCode.PASSWORD_NULL.getCode());
            }
            //密码加密
            obj.put("password", AESUtils.encrypt(obj.getString("password")));
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "setNewPassword");
            //
            if (resp.get("code").equals("0")) {
                renderJson(ResponseEmptyVo.success());
            } else {
                msg = ErrorCode.getFromKey(resp.getString("code")).getMessage();
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
        } catch (Exception e) {
            handleException(e, msg);
        }
    }


    /**
     * 获取修改年龄
     * accessToken=xxxx &clientId=xxx&buId=xxx
     */
    @Before(ApiAuthInterceptor.class)
    public void modAge() {
        String msg = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            obj.put("buId", BqConstants.BUID);
            if (StringUtils.isEmpty(obj.getString("accessToken"))) {
                logger.error("accessToken为空");
                throw new UBizException(ErrorCode.SYSTEM_EXCEPTION.getCode());
            }
            if (obj.getInteger("age") == null) {
                logger.error("age为空");
                msg = "age不能为空";
                throw new UBizException(ErrorCode.SYSTEM_EXCEPTION.getCode());
            }

            if (obj.getInteger("age") > 120 || obj.getInteger("age") < 1) {

                msg = "年龄范围1-120";
                throw new UBizException(ErrorCode.SYSTEM_EXCEPTION.getCode());
            }

            String user_id = parseToken(obj.getString("accessToken")).get(BqConstants.DOME_USER_ID);
            logger.info("user_id:{}", parseToken(obj.getString("accessToken")));
            obj.put("accessToken", AESUtils.encrypt(obj.getString("accessToken")));
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "modAge");
            //
            if (resp.get("code").equals("0")) {

                Db.update("update uc_user set age = ? where user_id = ?", obj.getInteger("age"), user_id);
                renderJson(ResponseEmptyVo.success());
            } else {
                msg = ErrorCode.getFromKey(resp.getString("code")).getMessage();
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
        } catch (Exception e) {
            handleException(e, msg);
        }
    }

    /**
     * 获取修改昵称
     * accessToken=xxxx &clientId=xxx&buId=xxx
     */
    @Before(ApiAuthInterceptor.class)
    public void editNickName() {
        String msg = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            obj.put("buId", BqConstants.BUID);
            if (StringUtils.isEmpty(obj.getString("accessToken"))) {
                logger.error("accessToken为空");
                throw new UBizException(ErrorCode.SYSTEM_EXCEPTION.getCode());
            }
            if (StringUtils.isEmpty(obj.getString("userName"))) {
                logger.error("userName为空");
                msg = "昵称不能为空";
                throw new UBizException(ErrorCode.SYSTEM_EXCEPTION.getCode());
            }
            String user_id = parseToken(obj.getString("accessToken")).get(BqConstants.DOME_USER_ID);
            obj.put("accessToken", AESUtils.encrypt(obj.getString("accessToken")));
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "editNickName");
            //
            if (resp.get("code").equals("0")) {
                Db.update("update uc_user set user_name = ? where user_id = ?", obj.getString("userName"), user_id);
                Db.update("update t_comment set user_name = ? where user_id = ? ", obj.getString("userName"), user_id);
                Db.update("update t_comment set reply_user_name = ? where reply_user_id = ? ", obj.getString("userName"), user_id);
                renderJson(ResponseEmptyVo.success());
            } else {
                msg = ErrorCode.getFromKey(resp.getString("code")).getMessage();
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
        } catch (Exception e) {
            handleException(e, msg);
        }
    }

    /**
     * 登出accessToken
     */
    @Before(ApiAuthInterceptor.class)
    public void quit() {

        String msg = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            obj.put("buId", BqConstants.BUID);
            if (StringUtils.isEmpty(obj.getString("accessToken"))) {
                logger.error("accessToken为空");
                throw new UBizException(ErrorCode.SYSTEM_EXCEPTION.getCode());
            }

            obj.put("accessToken", AESUtils.encrypt(obj.getString("accessToken")));
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "quit");
            //
            if (resp.get("code").equals("0")) {
                renderJson(ResponseEmptyVo.success());
            } else {
                msg = ErrorCode.getFromKey(resp.getString("code")).getMessage();
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
        } catch (Exception e) {
            handleException(e, msg);
        }
    }


    /**
     * 验证是否实名认证
     */
    @Before(ApiAuthInterceptor.class)
    public void checkCard() {
        String msg = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            obj.put("buId", BqConstants.BUID);
            obj.put("clientId", BqConstants.BUID);
            if (StringUtils.isEmpty(obj.getString("accessToken"))) {
                logger.error("accessToken为空");
                throw new UBizException(ErrorCode.SYSTEM_EXCEPTION.getCode());
            }

            obj.put("accessToken", obj.getString("accessToken"));
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "checkCard");
            //
            JSONObject data = resp.getJSONObject("data");
            if (resp.get("code").equals("0")) {
                renderJson(ResponseMobileDataVo.success(data, EncodeUtils.isEncode()));
            } else {
                msg = ErrorCode.getFromKey(resp.getString("code")).getMessage();
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
        } catch (Exception e) {
            handleException(e, msg);
        }
    }

    /**
     * 实名认证
     * accessToken=xxx&clientId=xxx&buId=xxx&mobile=xxx&verifyCode=xxx&name=xxx&countryCode=xxx&card=xxx
     */
    @Before(ApiAuthInterceptor.class)
    public void card() {
        String msg = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            obj.put("buId", BqConstants.BUID);
            obj.put("clientId", BqConstants.BUID);
            if (StringUtils.isEmpty(obj.getString("accessToken"))) {
                logger.error("accessToken为空");
                throw new UBizException(ErrorCode.SYSTEM_EXCEPTION.getCode());
            }
            validateMobileInner(obj.getString("mobile"));
            if (StringUtils.isEmpty(obj.getString("verifyCode"))) {
                logger.error("验证码为空");
                msg = "验证码不能为空";
                throw new UBizException(ErrorCode.VERIFY_CODE_NULL.getCode());
            }
            //验证姓名
            validateNameInner(obj.getString("name"));
            //验证身份证格式
            validateCardInner(obj.getString("card"));
            obj.put("accessToken", obj.getString("accessToken"));
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "card");
            //
            JSONObject data = resp.getJSONObject("data");
            if (resp.get("code").equals("0")) {
                //保存本地记录
                Card card = new Card()
                        .set("uid", parseToken(obj.getString("accessToken")).get(BqConstants.DOME_USER_ID))
                        .set("name", obj.getString("name"))
                        .set("mobile", obj.get("mobile"))
                        .set("card", obj.getString("card"))
                        .set("bu_id", BqConstants.BUID);
                Card.dao.saveCard(card);
                renderJson(ResponseMobileDataVo.success(data, EncodeUtils.isEncode()));
            } else {
                msg = ErrorCode.getFromKey(resp.getString("code")).getMessage();
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
        } catch (Exception e) {
            handleException(e, msg);
        }

    }

    /**
     * 修改头像
     */
    @Before(ApiAuthInterceptor.class)
    public void modAvatar() {
        String msg = "";
        try {
            JSONObject obj = RequestUtil.getDecodeParams(getRequest());
            obj.put("buId", BqConstants.BUID);
            if (StringUtils.isEmpty(obj.getString("accessToken"))) {
                logger.error("accessToken为空");
                throw new UBizException(ErrorCode.SYSTEM_EXCEPTION.getCode());
            }
            if (StringUtils.isEmpty(obj.getString("avatarUrl"))) {
                logger.error("avatarUrl为空");
                msg = "头像不能为空";
                throw new UBizException(ErrorCode.SYSTEM_EXCEPTION.getCode());
            }
            String user_id = parseToken(obj.getString("accessToken")).get(BqConstants.DOME_USER_ID);
            obj.put("accessToken", AESUtils.encrypt(obj.getString("accessToken")));
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "modAvatar");
            //
            if (resp.get("code").equals("0")) {
                Db.update("update uc_user set avatar = ? where user_id = ?", obj.getString("avatarUrl"), user_id);
                Db.update("update t_comment set user_avatar = ? where user_id = ? ", obj.getString("avatarUrl"), user_id);
                Db.update("update t_comment set reply_user_avatar = ? where reply_user_id = ? ", obj.getString("avatarUrl"), user_id);
                renderJson(ResponseEmptyVo.success());
            } else {
                msg = ErrorCode.getFromKey(resp.getString("code")).getMessage();
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
        } catch (Exception e) {
            handleException(e, msg);
        }
    }


}
