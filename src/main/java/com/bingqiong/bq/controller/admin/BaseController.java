package com.bingqiong.bq.controller.admin;

import com.bingqiong.bq.conf.ErrorCode;
import com.bingqiong.bq.constant.BizType;
import com.bingqiong.bq.constant.BqConstants;
import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.exception.UBizException;
import com.bingqiong.bq.jfinal.BqModelInjector;
import com.bingqiong.bq.utils.AESUtils;
import com.bingqiong.bq.vo.ResponseEmptyVo;
import com.jfinal.core.Controller;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 基础的controller，用于处理公共业务。
 * <p>
 * Created by hunsy on 2017/4/7.
 */
public abstract class BaseController extends Controller {

    protected Logger logger = LoggerFactory.getLogger(BaseController.class);

    protected final boolean flag = false;

    @Override
    public <T> T getModel(Class<T> modelClass) {
        return BqModelInjector.inject(modelClass, null, getRequest(), false);
    }

    /**
     * 异常处理。
     *
     * @param e
     */
    public void handleException(Exception e, String errMsg) {

        if (e instanceof BizException) {
            BqErrorCode bqErrorCode = BqErrorCode.getFromKey(((BizException) e).getErrorCode());
            errMsg = StringUtils.isEmpty(errMsg) ? bqErrorCode.getMessage() : errMsg;
            logger.info("异常信息->:msg:{}", errMsg);
            renderJson(ResponseEmptyVo.failure(bqErrorCode.getCode(), errMsg));
        } else if (e instanceof UBizException) {
            ErrorCode bqErrorCode = ErrorCode.getFromKey(((UBizException) e).getErrorCode());
            errMsg = StringUtils.isEmpty(errMsg.trim()) ? bqErrorCode.getMessage() : errMsg;
            logger.info("异常信息->:msg:{}", errMsg);
            renderJson(ResponseEmptyVo.failure(BqErrorCode.CODE_FAILED.getCode(), errMsg));
        } else {
            logger.error("异常信息->error:{}", e);
            renderJson(ResponseEmptyVo.failure(BqErrorCode.CODE_FAILED));
        }
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
            throw new UBizException(ErrorCode.PASSPORT_ILLEGLE.getCode());
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
            throw new UBizException(ErrorCode.SYSTEM_EXCEPTION.getCode());
        }
        BizType bizTypeEnum = BizType.getFromKey(bizType);

        if (bizTypeEnum == null) {
            logger.error("非法的业务类型！");
            throw new UBizException(ErrorCode.SYSTEM_EXCEPTION.getCode());
        }
        return bizTypeEnum;
    }

    /**
     * 解析Token
     *
     * @param accessToken
     * @return
     */
    public static Map<String, String> parseToken(String accessToken) {
        Map<String, String> map;
        try {
            map = new HashMap<>();
            String tokens[] = accessToken.split("\\" + "|");
            String userId = new String(
                    Base64.decodeBase64(tokens[BqConstants.USERID_POSITION].getBytes()));
            map.put(BqConstants.DOME_USER_ID, userId);
        } catch (Exception e) {
            throw e;
        }
        return map;
    }

    /**
     * 验证国内姓名
     */
    public void validateNameInner(String name) throws BizException {
        String nameReg = "^[\u4E00-\u9FFF]+$";

        if (name != null && name.length() > 32) {
            logger.error("姓名长度过长->name:{}", name);
            throw new UBizException(ErrorCode.NAME_LIMIT.getCode());
        }

        if (!name.matches(nameReg)) {
            logger.error("姓名只能输入汉字->name:{}", name);
            throw new UBizException(ErrorCode.NAME_ILLEGLE.getCode());
        }
    }

    /**
     * 验证身份证正则
     *
     * @param card
     * @return
     */
    protected void validateCardInner(String card) throws BizException {
        String reg15 = "^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$";
        String reg19 = "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X)$";
        if (card.length() != 15 && card.length() != 18) {
            logger.error("card位数不对->card:{}", card);
            throw new UBizException(ErrorCode.IDCARD_ILLEGLE.getCode());
        }
        boolean flag = false;
        if (card.length() == 15) {
            flag = card.matches(reg15);
        }

        if (card.length() == 18) {
            flag = card.toUpperCase().matches(reg19);
        }
        if (!flag) {
            logger.error("身份证格式不正确->card:{}", card);
            throw new UBizException(ErrorCode.IDCARD_ILLEGLE.getCode());
        }
    }

    protected void validatePassword(String password) throws BizException {
        if (StringUtils.isBlank(password)) {
            logger.error("参数密码为空");
            throw new UBizException(ErrorCode.PASSWORD_NULL.getCode());
        }

        //密码长度不够
        if (BqConstants.PASSWORD_MIN_LENGTH > password.length() || BqConstants.PASSWORD_MAX_LENGTH < password.length()) {
            logger.error(ErrorCode.PASSWORD_ILLEGLE.getMessage());
            throw new UBizException(ErrorCode.PASSWORD_ILLEGLE.getCode());
        }
        //包含非法字符
        for (int i = 0; i < password.length(); i++) {
            if (BqConstants.PASSWORD_ALLOW_CHAR.indexOf(password.charAt(i)) == -1) {
                logger.error(ErrorCode.PASSWORD_ILLEGLE.getMessage() + ",非法字符：" + password.charAt(i));
                throw new UBizException(ErrorCode.PASSWORD_ILLEGLE.getCode());
            }
        }
    }

//    @Override
//    public UploadFile getFile(String parameterName) {
//        List<UploadFile> uploadFiles = getFiles();
//        for (UploadFile uploadFile : uploadFiles) {
//            if (uploadFile.getParameterName().equals(parameterName)) {
//                return uploadFile;
//            }
//        }
//        return null;
//    }
//
//    @Override
//    public List<UploadFile> getFiles() {
//        HttpServletRequest request = getRequest();
//        if (request instanceof MultipartRequest == false)
//            request = new MultipartRequest(request);
//        return ((MultipartRequest) request).getFiles();
//    }
}
