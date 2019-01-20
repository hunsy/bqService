
package com.bingqiong.bq.conf;

/**
 * **********************************************************
 * 内容摘要	：<p>
 * <p>
 * 作者	：94841
 * 创建时间	：2016年4月28日 上午11:37:57
 * 当前版本号：v1.0
 * 历史记录	:
 * 日期	: 2016年4月28日 上午11:37:57 	修改人：niuzan
 * 描述	:
 * **********************************************************
 */
public enum ErrorCode {
    //----------------------------common--------------------------//保留-1到-50
    SUCCESS("0", "成功"),
    SYSTEM_EXCEPTION("99", "操作失败，请重试"),
    IP_NULL("-1", "ip为空"),
    IP_ILLEGLE("-2", "ip格式错误"),
    PASSPORT_NULL("-3", "请输入账号"),
    PASSPORT_NOT_EXIST("-4", "账号不存在"),
    PASSPORT_ILLEGLE("-5", "请输入格式正确的账号"),
    PASSWORD_NULL("-6", "请输入密码"),
    PASSWORD_WRONG("-7", "密码错误"),
    PASSWORD_ILLEGLE("-8", "密码应为6-16个字符，限字母、数字，区分大小写"),
    VERIFY_CODE_NULL("-9", "验证码为空"),
    VERIFY_CODE_ERROR("-10", "验证码错误"),
    SEND_EMAIL_CODE_ONCE_MIN("-11", "一分钟内发送邮件验证码,暂停发送"),
    SEND_SMS_ONCE_MIN("-12", "一分钟内发送短信,暂停发送"),
    USER_STATUS_INVALID("-13", "账号被锁"),
    CAPTCHA_NULL("-14", "请输入验证码"),
    CAPTCHA_WRONG("-15", "验证码错误"),
    CAPTCHA_EXPIRED("-16", "验证码过期"),
    TOKEN_EXCEPTION("-17", "请重新登录"),
    SEND_SMS_LIMIT("-18", "发送手机验证码已达上限"),
    //----------------------------register--------------------------//保留-51到-55
    PASSPORT_EXIST("-51", "账号已注册"),
    //----------------------------login--------------------------//保留-56到-60
    USER_NOT_EXIST("-56", "用户不存在"),
    //----------------------------重置密码--------------------------//保留-61到-65

    //----------------------------修改密码--------------------------//保留-66到-75
    CONFIRM_RESET_NOT_SAME("-66", "两次密码不一致"),
    SAME_WITH_OLD_PWD("-67", "新密码与原密码相同"),
    OLD_PWD_NULL("-68", "请输入原密码"),
    NEW_PWD_NULL("-69", "请输入新密码"),
    OLD_PWD_ERROR("-70", "原密码有误，请重新输入"),
    CONFIRM_PWD_NULL("-71", "确认密码为空"),
    //------------------------------修改用户信息------------------------//保留-76到-85
    AVATAR_PARAM_NULL("-76", "请选择头像"),
    GENDER_PARAM_NULL("-77", "请选择性别"),
    GENDER_PARAM_ILLEGLE("-78", "性别参数非法"),
    USERNAME_NULL("-79", "请输入昵称"),
    USERNAME_ILLEGLE("-80", "昵称非法"),
    //------------------------第三方错误码-------------------------//保留-86到-95
    BIND_FAIL("-86", "绑定失败,请重试"),
    BIND_PASSPORT_EXIST("-87", "仅可绑定未注册账号"),
    HAS_BIND("-88", "账号已绑定"),
    //------------------------开放平台错误码-------------------------//保留-96到-105
    OPEN_PASSPORT_NULL("-3", "请输入账号"),
    OPEN_PASSPORT_NOT_EXIST("-4", "账号不存在"),
    OPEN_PASSPORT_ILLEGLE("-5", "账号格式不正确"),
    OPEN_PASSPORT_EXIST("-51", "该账号已注册"),
    //--------------------------根据id获取用户信息---------------------//保留-106到-115
    USERID_NULL("-106", "参数为空"),
    USERID_ILLEGLE("-107", "参数非法"),
    //--------------------------------------------
    IDCARD_CHECK_LIMIT("-120", "请联系客服"),
    IDCARD_CHECKED("-121", "账号已进行实名认证"),
    IDCARD_ILLEGLE("-122", "请输入格式正确的身份证号"),
    NAME_ILLEGLE("-123", "姓名只支持汉字"),
    NAME_LIMIT("-124", "最多支持32位长度的姓名");


    public String code;

    public String message;

    private ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static final ErrorCode getFromKey(String code) {
        for (ErrorCode e : ErrorCode.values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    public static final ErrorCode getFromValue(String message) {
        for (ErrorCode e : ErrorCode.values()) {
            if (e.getMessage().equals(message)) {
                return e;
            }
        }
        return null;
    }

}
