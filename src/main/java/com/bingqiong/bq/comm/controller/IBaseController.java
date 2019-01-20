package com.bingqiong.bq.comm.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bingqiong.bq.comm.constants.BizType;
import com.bingqiong.bq.comm.constants.Constants;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.jfinal.BqModelInjector;
import com.bingqiong.bq.comm.utils.DESUtil;
import com.bingqiong.bq.comm.vo.ResponseDataVo;
import com.bingqiong.bq.comm.vo.ResponseEmptyVo;
import com.bingqiong.bq.comm.vo.ResponseMobileDataVo;
import com.bingqiong.bq.comm.vo.ResponseVoAdapter;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hunsy on 2017/6/21.
 */
public abstract class IBaseController extends Controller {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public <T> T getModel(Class<T> modelClass) {
        return BqModelInjector.inject(modelClass, null, getRequest(), false);
    }

    /**
     * 异常处理
     *
     * @param e
     * @return
     */
    protected ResponseVoAdapter handleException(Exception e) {

        logger.error("{}", e);
        if (e instanceof BizException) {
            ErrorCode errorCode = ((BizException) e).getCode();
            return ResponseEmptyVo.failure(errorCode);
        }

        return ResponseEmptyVo.failure(ErrorCode.SYSTEM_EXCEPTION);
    }

    /**
     * 异常处理
     *
     * @param e
     * @return
     */
    protected ResponseVoAdapter handleException(Exception e, Object data, boolean enc) {

        e.printStackTrace();
        if (e instanceof BizException) {
            ErrorCode errorCode = ((BizException) e).getCode();
            return ResponseMobileDataVo.failure(errorCode, data, enc);
        }
        return ResponseEmptyVo.failure(ErrorCode.SYSTEM_EXCEPTION);
    }


    /**
     * 返回成功
     */
    protected void renderSuccess() {
        renderJson(ResponseEmptyVo.success());
    }

    /**
     * 返回成功，有数据
     *
     * @param data
     */
    protected void renderSuccess(Object data) {
        renderJson(ResponseDataVo.success(data));
    }

    /**
     * 返回成功，数据可能加密
     *
     * @param data
     * @param enc
     */
    protected void renderSuccess(Object data, boolean enc) {
        renderJson(ResponseMobileDataVo.success(data, enc));
    }

    /**
     * 返回失败
     *
     * @param e
     */
    protected void renderFailure(Exception e) {
        renderJson(handleException(e));
    }

    /**
     * 返回不加密的带数据的失败信息
     *
     * @param e
     * @param data
     */
    protected void renderFailure(Exception e, Object data) {
        renderJson(handleException(e, data, false));
    }

    protected void renderFailure(Exception e, Object data, boolean enc) {
        renderJson(handleException(e, data, enc));
    }

    /**
     * 验证国内的手机号
     *
     * @return
     */
    public void validateMobileInner(String mobile) throws BizException {
        String regMobile = "^[1][3-8]\\d{9}$";
        if (!mobile.matches(regMobile)) {
            logger.error("国内手机号码格式错误->mobile:{}", mobile);
            throw new BizException(ErrorCode.PASSPORT_ILLEGLE);
        }
    }

    /**
     * 验证BizType
     *
     * @param bizType
     * @return
     * @throws Exception
     */
    protected BizType validateBizType(String bizType) throws Exception {
        if (StringUtils.isBlank(bizType)) {
            logger.error("bizType为空!");
            throw new BizException(ErrorCode.MISSING_PARM);
        }
        BizType bizTypeEnum = BizType.getFromKey(bizType);

        if (bizTypeEnum == null) {
            logger.error("非法的业务类型！");
            throw new BizException(ErrorCode.BIZTYPE_NOT_EXIST);
        }
        return bizTypeEnum;
    }


    protected void validatePassword(String password) throws BizException {
        if (StringUtils.isBlank(password)) {
            logger.error("参数密码为空");
            throw new BizException(ErrorCode.PASSWORD_NULL);
        }

        //密码长度不够
        if (Constants.PASSWORD_MIN_LENGTH > password.length() || Constants.PASSWORD_MAX_LENGTH < password.length()) {
            logger.error(ErrorCode.PASSWORD_ILLEGLE.getMsg());
            throw new BizException(ErrorCode.PASSWORD_ILLEGLE);
        }
        //包含非法字符
        for (int i = 0; i < password.length(); i++) {
            if (Constants.PASSWORD_ALLOW_CHAR.indexOf(password.charAt(i)) == -1) {
                logger.error(ErrorCode.PASSWORD_ILLEGLE.getMsg() + ",非法字符：" + password.charAt(i));
                throw new BizException(ErrorCode.PASSWORD_ILLEGLE);
            }
        }
    }

    protected JSONObject readParam(HttpServletRequest request, boolean enc) {

        String paramStr = HttpKit.readIncommingRequestData(request);
        logger.info("请求参数:{}", paramStr);
        //有参数
        if (StringUtils.isNotEmpty(paramStr)) {
            if (enc) {
                paramStr = DESUtil.decrypt(paramStr);
                logger.info("请求参数解密后:{}", paramStr);
            }
            JSONObject object = JSON.parseObject(paramStr);
            return object;
        }
        return null;
    }

}
