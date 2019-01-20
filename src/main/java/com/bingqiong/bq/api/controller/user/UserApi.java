package com.bingqiong.bq.api.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.api.interceptor.ApiAuthInterceptor;
import com.bingqiong.bq.comm.constants.Constants;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.constants.ThirdPlatform;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.http.UcApi;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.utils.aes.AESUtils;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.comm.vo.ResponseMobileDataVo;
import com.bingqiong.bq.conf.BqCmsConf;
import com.bingqiong.bq.model.category.Group;
import com.bingqiong.bq.model.comment.Comment;
import com.bingqiong.bq.model.msg.PrivateMsg;
import com.bingqiong.bq.model.post.Post;
import com.bingqiong.bq.model.user.*;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Redis;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 用户相关请求
 * Created by hunsy on 2017/5/10.
 */
public class UserApi extends IBaseController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 获取验证码
     */
    @Clear({
            ApiAuthInterceptor.class
    })
    public void getSmsCode() {
        try {
            JSONObject obj = getAttr("params");
//                    readParam(getRequest(), BqCmsConf.enc);
            obj.put("buId", Constants.BUID);
            obj.put("clientId", Constants.BUID);
            validateBizType(obj.getString("bizType"));
            //验证手机号码
            validateMobileInner(obj.getString("mobile"));
            //请求uc服务
            JSONObject json = UcApi.getInstance().doreq(obj, "getCode");
            if (json.getString("code").equals("0")) {
                renderSuccess();
            } else {
                throw new BizException(ErrorCode.getFromKey(json.getIntValue("code")));
            }
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 验证验证码
     */
    @Clear({
            ApiAuthInterceptor.class
    })
    public void checkSmsCode() {
        try {
            JSONObject obj = getAttr("params");
            obj.put("buId", Constants.BUID);
            obj.put("clientId", Constants.BUID);
            //验证验证码不为空
            if (StringUtils.isEmpty(obj.getString("mobileCode"))) {
                logger.error("验证码为空");
                throw new BizException(ErrorCode.VERIFY_CODE_NULL);
            }
            //验证BizType是否存在
            validateBizType(obj.getString("bizType"));
            //验证手机号码
            validateMobileInner(obj.getString("mobile"));
            //请求uc服务
            JSONObject json = UcApi.getInstance().doreq(obj, "verifyCode");
            if (json.getString("code").equals("0")) {
                JSONObject data = json.getJSONObject("data");
                renderSuccess(data, BqCmsConf.enc);
            } else {
                throw new BizException(ErrorCode.getFromKey(json.getIntValue("code")));
            }
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    /**
     * 注册。
     */
    @Clear({
            ApiAuthInterceptor.class
    })
    public void register() {
        try {
            JSONObject obj = getAttr("params");
            String password = obj.getString("password");
            if (StringUtils.isEmpty(obj.getString("mobileCode"))) {
                logger.error("验证码为空");
                throw new BizException(ErrorCode.VERIFY_CODE_NULL);
            }
            obj.put("bizType", 1);
            obj.put("buId", Constants.BUID);
            //验证手机号码
            validateMobileInner(obj.getString("passport"));
            validatePassword(password);
            //密码加密
            obj.put("password", AESUtils.encrypt(password));
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "register");
            //注册成功
            if (resp.get("code").equals("0")) {
                JSONObject data = resp.getJSONObject("data");
                String uid = data.getString(Constants.DOME_USER_ID);
                //保存本地记录
                if (uid != null) {
                    logger.info("保存本User");
                    User record = new User();
                    record.set("user_id", uid);
                    record.set("user_name", RandomStringUtils.randomAlphabetic(6));
                    record.set("mobile", obj.getString("passport"));
                    record.set("gender", obj.getString("gender"));
                    User.dao.saveUser(record);
                }
                String accessToken = data.getString("accessToken");
                String userTokenKey = Constants.REDIS_USER_TOKEN_PRIFEX + uid;
                String dtoken = Redis.use().get(userTokenKey);
                if (StringUtils.isEmpty(dtoken)) {
                    dtoken = RandomStringUtils.randomAlphanumeric(16);
                    Redis.use().set(userTokenKey, dtoken);
                }
                Redis.use().expire(userTokenKey, 7 * 24 * 60 * 60 - 60);
                Redis.use().set(Constants.REDIS_TOKEN_PRIFEX + dtoken, accessToken);
                Redis.use().expire(Constants.REDIS_TOKEN_PRIFEX + dtoken, 7 * 24 * 60 * 60 - 60);
                data.put("accessToken", dtoken);
                renderSuccess(data, BqCmsConf.enc);
            } else {
                throw new BizException(ErrorCode.getFromKey(resp.getIntValue("code")));
            }

        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 注册环信
     */
    private String registerHx(String bqId, String nickName) throws Exception {
        String hxId = bqId;
        User user = User.dao.findByUserId(bqId);
        if (BqCmsConf.dev) {
            hxId = bqId + "-dev";
        }

        //查看是否已经注册环信了
        boolean registered = Redis.use().sismember(Constants.REDIS_HX, hxId);
        if (registered) {
            return hxId;
        }

        JSONObject params = new JSONObject();
        params.put("username", bqId);
        params.put("password", bqId);
        params.put("nickname", nickName);
        //注册环信

        JSONObject resp = UcApi.getInstance().doreq(params, "hxUser");
        if (resp != null) {
            JSONObject data = resp.getJSONObject("entities");
        }

        return hxId;
    }

    /**
     * 登录<br/>
     */
    @Clear({
            ApiAuthInterceptor.class
    })
    public void login() {

        try {
            JSONObject obj = getAttr("params");
            obj.put("buId", Constants.BUID);
            //验证手机号码
            validateMobileInner(obj.getString("passport"));
            //验证密码格式
            validatePassword(obj.getString("password"));
//            String limitKey = Constants.REDIS_LOGIN_LIMIT + obj.getString("passport");
            //密码加密
            obj.put("password", AESUtils.encrypt(obj.getString("password")));
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "login");
            //注册成功
            if (resp.get("code").equals("0")) {
                JSONObject data = resp.getJSONObject("data");
                String uid = data.getString("user_id");
                User.dao.findByUc(uid);
                //保存本地token
                String accessToken = data.getString("accessToken");
                //获取是否存在dtoken
                String dtoken = Redis.use().get(Constants.REDIS_USER_TOKEN_PRIFEX + uid);
                if (StringUtils.isNotEmpty(dtoken)) {
                    logger.info("清除已有token:{}----------------", dtoken);
                    Redis.use().del(Constants.REDIS_USER_TOKEN_PRIFEX + uid, dtoken);
                    Redis.use().del(Constants.REDIS_TOKEN_PRIFEX + dtoken);
                }
                dtoken = RandomStringUtils.randomAlphanumeric(16);

                Redis.use().set(Constants.REDIS_USER_TOKEN_PRIFEX + uid, dtoken);
                Redis.use().expire(Constants.REDIS_USER_TOKEN_PRIFEX + uid, 7 * 24 * 60 * 60 - 60);
                Redis.use().set(Constants.REDIS_TOKEN_PRIFEX + dtoken, accessToken);
                Redis.use().expire(Constants.REDIS_TOKEN_PRIFEX + dtoken, 7 * 24 * 60 * 60 - 60);
                //去除登录次数限制
//                Redis.use().del(limitKey);
                data.put("accessToken", dtoken);
                hxRegister(uid, data);
                renderSuccess(data, BqCmsConf.enc);
            } else {
                //获取登录限制
//                Long num = Redis.use().incrBy(limitKey, 1);
//                Redis.use().expire(limitKey, 60 * 60 * 24);
//                logger.info("times:{}", num);
//                if (num > 5) {
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("needCaptcha", true);
//                    ErrorCode errorCode = ErrorCode.getFromKey(resp.getIntValue("code"));
//                    renderJson(ResponseMobileDataVo.failure(errorCode, map, true));
//                } else {
//
//                }
                JSONObject object = resp.getJSONObject("data");
                if (object != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("needCaptcha", true);
                    ErrorCode errorCode = ErrorCode.getFromKey(resp.getIntValue("code"));
                    renderJson(ResponseMobileDataVo.failure(errorCode, map, true));
                } else {
                    throw new BizException(ErrorCode.getFromKey(resp.getIntValue("code")));
                }
            }
        } catch (Exception e) {
            renderFailure(e);
        }

    }


    /**
     * 第三方登录。
     * <p>
     * 应用调用第三方sdk之后，获取授权信息，请求当前接口，将信息传到uc，uc返回accessToken，
     * 当前接口返回给应用。
     */
    @Clear({
            ApiAuthInterceptor.class
    })
    public void thirdLogin() {

        try {
            JSONObject obj = getAttr("params");

            //第三方类型
            String platform = obj.getString("platform");
            String openId = obj.getString("openId");
            if (StringUtils.isEmpty(platform) || StringUtils.isEmpty(openId)) {
                logger.info("缺少平台信息|openId");
                throw new BizException(ErrorCode.MISSING_PARM);
            }
            Integer thirdId = ThirdPlatform.getFromKey(platform.toLowerCase()).getThirdId();
            JSONObject params = new JSONObject();
            params.put("state", Constants.BUID);
            params.put("thirdId", thirdId);
            params.put("openId", openId);
            params.put("gender", obj.getString("gender"));
            params.put("age", obj.getIntValue("age"));
            params.put("avatarUrl", obj.getString("avatarUrl"));
            params.put("userName", obj.getString("userName"));

            JSONObject resp = UcApi.getInstance().doreq(params, "thirdLogin");
            if (resp.get("code").equals("0")) {
                JSONObject data = resp.getJSONObject("data");
                String uid = data.getString("domeUserId");
                //保存本地记录
                if (uid != null) {

                    User user = User.dao.findByUserId(uid);
                    if (user == null) {
                        user = new User();
                        user.set("user_id", uid);
                        user.set("user_name", obj.getString("userName"));
                        user.set("gender", obj.getString("gender"));
                        user.set("age", obj.getIntValue("age"));
                        user.set("avatar_url", obj.getString("avatarUrl"));
                        User.dao.saveUser(user);
                    } else {
                        user.set("user_name", obj.getString("userName"));
                        user.set("gender", obj.getString("gender"));
                        user.set("age", obj.getIntValue("age"));
                        user.set("avatar_url", obj.getString("avatarUrl"));
                        User.dao.updateUser(user);
                    }
                }
                String accessToken = data.getString("accessToken");
                String userTokenKey = Constants.REDIS_USER_TOKEN_PRIFEX + uid;
                String dtoken = Redis.use().get(userTokenKey);
                if (StringUtils.isEmpty(dtoken)) {
                    dtoken = RandomStringUtils.randomAlphanumeric(16);
                    Redis.use().set(userTokenKey, dtoken);
                }
                Redis.use().expire(userTokenKey, 7 * 24 * 60 * 60 - 60);
                Redis.use().set(Constants.REDIS_TOKEN_PRIFEX + dtoken, accessToken);
                Redis.use().expire(Constants.REDIS_TOKEN_PRIFEX + dtoken, 7 * 24 * 60 * 60 - 60);
                JSONObject ret = new JSONObject();
                ret.put("accessToken", dtoken);
                ret.put("user_id", uid);
                //是否实名认证
                ret.put("checked", data.getBooleanValue("checked"));
                hxRegister(uid, ret);
                renderSuccess(ret, BqCmsConf.enc);
            } else {
                throw new BizException(ErrorCode.getFromKey(resp.getIntValue("code")));
            }
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 注册环信用户
     *
     * @param uid
     * @param data
     * @throws Exception
     */
    private void hxRegister(String uid, JSONObject data) throws Exception {


        String hxusername = uid;
        if (BqCmsConf.dev) {
            hxusername = uid + "-dev";
        }

        //查看是否已经注册环信了
        boolean registered = Redis.use().sismember(Constants.REDIS_HX, hxusername);
        if (!registered) {
            User user = User.dao.findByUserId(uid);
            JSONObject hxParams = new JSONObject();
            JSONObject params = new JSONObject();
            hxParams.put("username", hxusername);
            hxParams.put("password", hxusername);
            hxParams.put("nickname", user.getStr("user_name"));

            JSONObject hxResp = UcApi.getInstance().doreq(params, "hxUser");
            if (hxResp != null) {
                JSONObject entities = hxResp.getJSONObject("entities");
                if (entities != null) {
                    user.set("hx_uid", entities.getString("uid"));
                    User.dao.updateUser(user);
                    Redis.use().sadd(Constants.REDIS_HX, hxusername);
                }
            }
            data.put("hxUid", user.getStr("hx_uid"));
        }
        data.put("hxUsername", hxusername);
    }

    /**
     * 获取验证码
     */
    @Clear({
            ApiAuthInterceptor.class
    })
    public void getCaptcha() {

        try {
            JSONObject obj = new JSONObject();
            obj.put("buId", Constants.BUID);
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "getValidateCode");
            if (resp.get("code").equals("0")) {
                JSONObject data = resp.getJSONObject("data");
                String b64 = data.getString("captcha");
                byte[] bt64 = new BASE64Decoder().decodeBuffer(b64);

                String fileName = DateTime.now().toString("yyyyMMddHHmmsss") + UUID.randomUUID().toString() + ".png";
                String fullFilePath = BqCmsConf.CAPTCHA_DIR + fileName;
                logger.info("fullFilePath:{}", fullFilePath);
                FileUtils.writeByteArrayToFile(new File(fullFilePath), bt64);
                data.put("captcha", "admin/captcha?img=" + fileName);
                renderSuccess(data, BqCmsConf.enc);
            } else {
                throw new BizException(ErrorCode.getFromKey(resp.getIntValue("code")));
            }
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 获取用户信息。
     * <p>
     * 优先本地信息。没有本地信息再从uc获取
     * <p>
     * accessToken=xxxx &clientId=xxx&buId=xxx
     */
    public void queryUserInfo() {

        try {

            JSONObject object = getAttr("params");
            //当前用户
            User user = getAttr("bq_user");
//            String cuser_id = user.getStr("user_id");
            if ((object == null || StringUtils.isEmpty(object.getString("user_id"))) && user == null) {

                throw new BizException(ErrorCode.MISSING_PARM);
            }
            boolean followed = false;
            boolean shield = false;
            String user_id = "";
            if (object != null && object.containsKey("user_id")) {
                user_id = object.getString("user_id");
            }
            //查询用户是否被当前用户关注
            if (StringUtils.isNotEmpty(user_id)) {
                if (user != null) {
                    //查询当前用户是否屏蔽该用户的消息
                    shield = UserShields.dao.shiled(user.getStr("user_id"), object.getString("user_id"));
                    UserFollows follows = UserFollows.dao.findByUserIdAndFollowed(user.getStr("user_id"), user_id);
                    if (follows != null) {
                        followed = true;
                    }
                }
                user = User.dao.findByUserId(user_id);
            }

//            Record record = new Record();
//            for (String name : user.getAttrNames()) {
//                record.set(name, user.get(name));
//            }
            user.put("followed", followed);
            //查询当前用户的标签
            List<Record> tags = UserTags.dao.findList(user.getStr("user_id"), true);
            user.put("tags", tags);
            user.put("shield", shield);
            renderSuccess(user, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    /**
     * 他的帖子|我的帖子
     */
    @Before(PageInterceptor.class)
    public void posts() {

        try {
            PageRequest pageRequest = getAttr("pageRequest");
            User user = getAttr("bq_user");
            Map<String, String> params = pageRequest.getParams();
            if (user == null && params.get("user_id") == null) {

                throw new BizException(ErrorCode.MISSING_PARM);
            }

            if (StringUtils.isNotEmpty(params.get("user_id"))) {
                user = User.dao.findByUserId(params.get("user_id"));
            }
            JSONObject object = getAttr("mobileInfo");
            //请求来自哪个平台
            String platform;
            if (object.containsKey("platform")) {
                platform = object.getString("platform");
                pageRequest.getParams().put(platform + "_show", "1");
            }
            pageRequest.getParams().put("user_id", user.getStr("user_id"));
            Page<Record> page = Post.dao.findMyPosts(pageRequest);
            logger.info("page:{}", JsonKit.toJson(page));
            renderSuccess(page, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    /**
     * 获取用户的所有的标签
     */
    public void tags() {


        try {
            JSONObject object = getAttr("params");
            User user = getAttr("bq_user");
            logger.info(JsonKit.toJson(user));
            if ((object == null || StringUtils.isEmpty(object.getString("user_id"))) && user == null) {

                throw new BizException(ErrorCode.MISSING_PARM);
            }

            if (object != null && StringUtils.isNotEmpty(object.getString("user_id"))) {
                user = User.dao.findByUserId(object.getString("user_id"));
            }
            List<Record> ls = UserTags.dao.findList(user.getStr("user_id"), false);
            renderSuccess(ls, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 删除用户标签
     */
    public void deltag() {

        try {

            JSONObject object = getAttr("params");
            if (object == null || object.getLong("tag_id") == null) {
                logger.error("缺少标签id");
                throw new BizException(ErrorCode.MISSING_PARM);
            }
            User user = getAttr("bq_user");
            UserTags.dao.deleteTags(user.getStr("user_id"), object.getLong("tag_id"));
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    /**
     * 关注用户
     */
    public void follow() {

        try {
            User user = getAttr("bq_user");
            JSONObject object = getAttr("params");
            if (object == null || StringUtils.isEmpty(object.getString("user_id"))) {
                throw new BizException(ErrorCode.MISSING_PARM);
            }
            String user_id = object.getString("user_id");
            UserFollows follows = new UserFollows();
            follows.set("user_id", user.getStr("user_id"));
            follows.set("followed_id", user_id);
            UserFollows.dao.saveFollows(follows);
            //发送一条私信
            //添加一条消息
            PrivateMsg.dao.send(user.getStr("user_id"), user_id, UserFollows.msg);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 取消关注用户
     */
    public void unfollow() {

        try {
            User user = getAttr("bq_user");
            JSONObject object = getAttr("params");
            if (object == null) {
                throw new BizException(ErrorCode.MISSING_PARM);
            }
            String user_id = object.getString("user_id");
            UserFollows.dao.deleteFollows(user.getStr("user_id"), user_id);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 用户的圈子列表
     */
    @Before(PageInterceptor.class)
    public void groups() {

        try {

            PageRequest pageRequest = getAttr("pageRequest");
            User user = getAttr("bq_user");
            Map<String, String> params = pageRequest.getParams();
            if (StringUtils.isEmpty(params.get("user_id")) && user == null) {

                throw new BizException(ErrorCode.MISSING_PARM);
            }

            if (StringUtils.isNotEmpty(params.get("user_id"))) {
                user = User.dao.findByUserId(params.get("user_id"));
            }

            JSONObject object = getAttr("mobileInfo");
            //请求来自哪个平台
            String platform;
            if (object.containsKey("platform")) {
                platform = object.getString("platform");
                pageRequest.getParams().put(platform + "_show", "1");
            }
            pageRequest.getParams().put("user_id", user.getStr("user_id"));

            Page<Record> page = Group.dao.findFollowsPage(pageRequest);
            renderSuccess(page, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 当前用户的关注的用户
     */
    @Before(PageInterceptor.class)
    public void follows() {

        try {
            PageRequest pageRequest = getAttr("pageRequest");
            User user = getAttr("bq_user");

            Map<String, String> params = pageRequest.getParams();
            if (StringUtils.isEmpty(params.get("user_id")) && user == null) {

                throw new BizException(ErrorCode.MISSING_PARM);
            }

            if (StringUtils.isNotEmpty(params.get("user_id"))) {
                user = User.dao.findByUserId(params.get("user_id"));
            }
            pageRequest.getParams().put("user_id", user.getStr("user_id"));
            Page<Record> page = User.dao.findFollows(pageRequest);
            renderSuccess(page, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 关注当前用户的用户
     */
    @Before(PageInterceptor.class)
    public void fans() {

        try {

            PageRequest pageRequest = getAttr("pageRequest");
            User user = getAttr("bq_user");

            Map<String, String> params = pageRequest.getParams();
            if (StringUtils.isEmpty(params.get("user_id")) && user == null) {

                throw new BizException(ErrorCode.MISSING_PARM);
            }

            if (StringUtils.isNotEmpty(params.get("user_id"))) {
                user = User.dao.findByUserId(params.get("user_id").toString());
            }
            pageRequest.getParams().put("user_id", user.getStr("user_id"));
            Page<Record> page = User.dao.findFans(pageRequest);
            renderSuccess(page, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 我的|他的回复列表
     */
    @Before(PageInterceptor.class)
    public void replies() {

        try {
            User user = getAttr("bq_user");
            PageRequest pageRequest = getAttr("pageRequest");
            Map<String, String> params = pageRequest.getParams();

            if (StringUtils.isEmpty(params.get("user_id")) && user == null) {
                throw new BizException(ErrorCode.MISSING_PARM);
            }

            if (StringUtils.isNotEmpty(params.get("user_id"))) {
                user = User.dao.findByUserId(params.get("user_id").toString());
            }

            pageRequest.getParams().put("user_id", user.getStr("user_id"));
            Page<Record> page = Comment.dao.findMyReplies(pageRequest);
            renderSuccess(page, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 回复我的
     */
    @Before(PageInterceptor.class)
    public void replymes() {
        try {

            User user = getAttr("bq_user");
            PageRequest pageRequest = getAttr("pageRequest");
            pageRequest.getParams().put("user_id", user.getStr("user_id"));
            Page<Record> page = Comment.dao.replyMes(pageRequest);
            renderSuccess(page, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    /**
     * 禁止发消息
     */
    public void shield() {

        try {

            JSONObject object = getAttr("params");
            if (object == null || object.getString("user_id") == null) {

                throw new BizException(ErrorCode.MISSING_PARM);
            }
            User user = getAttr("bq_user");
            //当前用户，屏蔽其他用户
            UserShields shields = new UserShields();
            shields.set("user_id", user.getStr("user_id"));
            shields.set("shield_id", object.getString("user_id"));
            UserShields.dao.saveShield(shields);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 取消禁止发消息
     */
    public void unshield() {

        try {

            JSONObject object = getAttr("params");
            if (object == null || object.getString("user_id") == null) {

                throw new BizException(ErrorCode.MISSING_PARM);
            }
            User user = getAttr("bq_user");
            UserShields.dao.deleteShield(user.getStr("user_id"), object.getString("user_id"));
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 获取修改性别
     * accessToken=xxxx &clientId=xxx&buId=xxx
     */
    public void editGender() {
        try {
            JSONObject obj = getAttr("params");
            obj.put("buId", Constants.BUID);

            if (StringUtils.isEmpty(obj.getString("gender"))) {
                logger.error("gender为空");
                throw new BizException(ErrorCode.GENDER_NOT_NULL);
            }
            obj.put("accessToken", AESUtils.encrypt(getAttr("accessToken").toString()));
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "editGender");
            //
            if (resp.get("code").equals("0")) {

                User user = getAttr("bq_user");
                user.set("gender", obj.getString("gender"));
                User.dao.updateUser(user);
                renderSuccess();
            } else {

                throw new BizException(ErrorCode.getFromKey(resp.getIntValue("code")));
            }
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 修改密码
     * mobile=18061651013&passwordOld=xxx
     * & passwordNew=xxx&clientId=xxx&buId=xxx
     */
    public void editPassword() {
        try {
            JSONObject obj = getAttr("params");
            obj.put("buId", Constants.BUID);
            //验证就密码格式
            validatePassword(obj.getString("passwordOld"));
            //验证新密码格式
            validatePassword(obj.getString("passwordNew"));

            obj.put("passwordOld", AESUtils.encrypt(obj.getString("passwordOld")));
            obj.put("passwordNew", AESUtils.encrypt(obj.getString("passwordNew")));
            obj.put("accessToken", getAttr("accessToken").toString());
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "editPassword");
            //
            if (resp.get("code").equals("0")) {
                deleteToken(getRequest());
                renderSuccess();
            } else {
                throw new BizException(ErrorCode.getFromKey(resp.getIntValue("code")));
            }
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 重置密码
     * mobile=18061651013&password=xxx
     * &smsToken=xxx&clientId=xxx&buId=xxx
     */
    @Clear(ApiAuthInterceptor.class)
    public void setNewPassword() {
        try {
            JSONObject obj = getAttr("params");
            obj.put("buId", Constants.BUID);
            validateMobileInner(obj.getString("mobile"));
            //验证码为空
            if (StringUtils.isEmpty(obj.getString("smsToken"))) {
                logger.error("smsToken为空");
                throw new BizException(ErrorCode.VERIFY_CODE_NULL);
            }

            validatePassword(obj.getString("password"));
            //密码加密
            obj.put("password", AESUtils.encrypt(obj.getString("password")));
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "setNewPassword");
            //
            if (resp.get("code").equals("0")) {
                renderSuccess();
            } else {
                throw new BizException(ErrorCode.getFromKey(resp.getIntValue("code")));
            }
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    /**
     * 获取修改年龄
     * accessToken=xxxx &clientId=xxx&buId=xxx
     */
    public void modAge() {
        try {
            JSONObject obj = getAttr("params");
            obj.put("buId", Constants.BUID);

            if (obj.getInteger("age") == null) {
                logger.error("age为空");
                throw new BizException(ErrorCode.AGE_NOT_NULL);
            }

            if (obj.getInteger("age") > 120 || obj.getInteger("age") < 1) {
                logger.error("年龄范围1-120");
                throw new BizException(ErrorCode.AGE_ILLEGLE);
            }

            obj.put("accessToken", AESUtils.encrypt(getAttr("accessToken").toString()));
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "modAge");
            //
            if (resp.get("code").equals("0")) {
                User user = getAttr("bq_user");
                user.set("age", obj.getIntValue("age"));
                User.dao.updateUser(user);
                renderSuccess();
            } else {
                throw new BizException(ErrorCode.getFromKey(resp.getIntValue("code")));
            }
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 获取修改昵称
     * accessToken=xxxx &clientId=xxx&buId=xxx
     */
    public void editNickName() {
        try {
            User user = getAttr("bq_user");
            JSONObject obj = getAttr("params");
            obj.put("buId", Constants.BUID);
            if (obj == null) {
                throw new BizException(ErrorCode.MISSING_PARM);
            }
            String userName = obj.getString("userName");
            if (StringUtils.isEmpty(userName)) {
                logger.error("userName为空");
                throw new BizException(ErrorCode.MISSING_PARM);
            }

            if (userName.length() > 16 || userName.length() < 2) {
                logger.error("昵称格式不对->2~16位");
                throw new BizException(ErrorCode.USER_NAME_LENGTH);
            }
            //每24小时可以编辑5次
            long times = Redis.use().incrBy(Constants.REDIS_USER_NAME_EDIT_PREFIX + user.getStr("user_id"), 1);
            if (times > 5) {
                logger.error("修改次数->times:{}", times);
                throw new BizException(ErrorCode.USER_NAME_EDIT_TIMES_MORE);
            }

            obj.put("accessToken", AESUtils.encrypt(getAttr("accessToken").toString()));
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "editNickName");
            //
            if (resp.get("code").equals("0")) {

                user.set("user_name", obj.getString("userName"));
                User.dao.updateUser(user);
                //设置次数限制
                times = Redis.use().incrBy(Constants.REDIS_USER_NAME_EDIT_PREFIX + user.getStr("user_id"), 1);
                if (times == 1) {
                    Redis.use().expire(Constants.REDIS_USER_NAME_EDIT_PREFIX + user.getStr("user_id"), 60 * 60 * 24);
                }
                renderSuccess();
            } else {
                throw new BizException(ErrorCode.getFromKey(resp.getIntValue("code")));
            }
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 登出accessToken
     */
    @Before(ApiAuthInterceptor.class)
    public void quit() {
        try {
            JSONObject obj = getAttr("params");
            obj.put("buId", Constants.BUID);
            obj.put("accessToken", AESUtils.encrypt(getAttr("accessToken").toString()));
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "quit");
            //
            if (resp.get("code").equals("0")) {
                deleteToken(getRequest());
                renderSuccess();
            } else {
                throw new BizException(ErrorCode.getFromKey(resp.getIntValue("code")));
            }
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    /**
     * 验证是否实名认证
     */
    public void checkCard() {
        try {


            User user = getAttr("bq_user");
            Record card = UserCard.dao.getCard(user.getStr("user_id"));
            boolean checked = false;
            String cardNo = "";
            //本地不存在实名信息
            //从Uc获取是否实名认证
            if (card == null) {
                JSONObject obj = new JSONObject();
                obj.put("buId", Constants.BUID);
                obj.put("clientId", Constants.BUID);
                obj.put("accessToken", getAttr("accessToken").toString());
                //请求uc服务
                JSONObject resp = UcApi.getInstance().doreq(obj, "checkCard");
                if (resp.get("code").equals("0")) {
                    //
                    JSONObject data = resp.getJSONObject("data");
                    checked = data.getBoolean("checked");
                    if (checked) {
                        //如果uc已经认证
                        //保存本地实名认证
                        cardNo = data.getString("idcard");
                        card = new Record();
                        card.set("user_id", user.getStr("user_id"));
                        card.set("card", cardNo);
                        card.set("name", "NUL");
                        UserCard.dao.saveCard(card);
                    }
                } else {
                    throw new BizException(ErrorCode.getFromKey(resp.getIntValue("code")));
                }
            } else {
                cardNo = card.getStr("card");
                checked = true;
            }
            Record record = new Record();
            record.set("checked", checked);
            record.set("card", cardNo);
            renderSuccess(record, BqCmsConf.enc);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 实名认证
     * accessToken=xxx&clientId=xxx&buId=xxx&mobile=xxx&verifyCode=xxx&name=xxx&countryCode=xxx&card=xxx
     */
    public void card() {
        try {
            JSONObject obj = getAttr("params");
            User user = getAttr("bq_user");
            //实名查验
            Record card = UserCard.dao.getCard(user.getStr("user_id"));
            if (card != null) {

                throw new BizException(ErrorCode.IDCARD_CHECKED);
            }

            String name = obj.getString("name");
            if (StringUtils.isEmpty(name)) {
                throw new BizException(ErrorCode.NAME_NOT_NULL);
            }
            if (name.length() < 2 || name.length() > 6) {

                throw new BizException(ErrorCode.NAME_LIMIT);
            }
            String nameReg = "^[\u4E00-\u9FFF]+$";
            if (!name.matches(nameReg)) {
                logger.error("姓名只能输入汉字->name:{}", name);
                throw new BizException(ErrorCode.NAME_ILLEGLE);
            }


            obj.put("buId", Constants.BUID);
            obj.put("clientId", Constants.BUID);
            obj.put("accessToken", getAttr("accessToken").toString());
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "card");
            if (resp.get("code").equals("0")) {
                //保存本地记录
                Record record = new Record();
                record.set("user_id", user.getStr("user_id"));
                record.set("name", obj.getString("name"));
                record.set("card", obj.getString("card"));
                UserCard.dao.saveCard(record);
                renderSuccess(resp.getJSONObject("data"), BqCmsConf.enc);
            } else {
                throw new BizException(ErrorCode.getFromKey(resp.getIntValue("code")));
            }
        } catch (Exception e) {
            renderFailure(e);
        }

    }

    /**
     * 修改头像
     */
    public void modAvatar() {
        try {
            JSONObject obj = getAttr("params");
            if (obj == null || StringUtils.isEmpty(obj.getString("avatarUrl"))) {

                throw new BizException(ErrorCode.MISSING_PARM);
            }
            obj.put("buId", Constants.BUID);
            User user = getAttr("bq_user");
            obj.put("accessToken", AESUtils.encrypt(getAttr("accessToken").toString()));
            //请求uc服务
            JSONObject resp = UcApi.getInstance().doreq(obj, "modAvatar");
            //
            if (resp.get("code").equals("0")) {
                user.set("avatar_url", obj.getString("avatarUrl"));
                User.dao.updateUser(user);
                renderSuccess();
            } else {
                throw new BizException(ErrorCode.getFromKey(resp.getIntValue("code")));
            }
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * @param request
     */
    private void deleteToken(HttpServletRequest request) {
        String dtoken = request.getHeader("token");
        Redis.use().del(Constants.REDIS_TOKEN_PRIFEX + dtoken);
//        removeSessionAttr("bq_user");
    }

}
