package com.bingqiong.bq.comm.constants;

/**
 * 错误码定义
 * <p>
 * Created by hunsy on 2017/6/21.
 */
public enum ErrorCode {

    //=============upload=================
    IMG_UPLOAD_NOT_SUPPORT(100000, "不支持的图片类型"),
    IMG_UPLOAD_BANNER_BG(100001, "Banner背景图长度必须大于750px，并且长宽比为16/9"),
    IMG_UPLOAD_BANNER(100002, "Banner长度必须大于640px，并且长宽比为16/9"),
    IMG_UPLOAD_GROUP_ICON(100003, "圈子图标必须为长度大于108px的正方形图标"),
    IMG_UPLOAD_PLATE_ICON(100003, "板块图标必须为长度大于28px的正方形图标"),
    //===========plate===============板块相关错误
    //101xx
    PLATE_NAME_EXIST(10100, "版块名称已经存在"),
    PLATE_NAME_NULL(10101, "版块名称不能为空"),
    PLATE_NAME_ILLEGLE(10102, "版块名称必须为2-6个字符"),
    PLATE_NOT_EXIST(10103, "版块不存在或已被删除"),
    PLATE_NOT_DEL(10104, "版块下存在圈子，请先删除圈子"),


    //===========group===============圈子相关错误
    //102xx
    GROUP_NAME_EXIST(10200, "圈子名称已经存在"),
    GROUP_NAME_NULL(10201, "圈子名称不能为空"),
    GROUP_NAME_ILLEGLE(10202, "圈子名称必须为2-6个字符"),
    GROUP_NOT_EXIST(10203, "圈子不存在或已被删除"),
    GROUP_FOLLOWS_NOT_EXIST(10204, "用户没有关注"),
    GROUP_FOLLOWS_EXIST(10205, "用户已关注"),

    //===========post===============帖子相关错误
    //103xx
    POST_CONTENT_NULL(10300, "帖子内容不能为空"),
    POST_TITLE_TOO_LONG(10301, "帖子标题不能超过128个字节"),
    POST_GROUP_NOT_SAME(10302, "帖子不支持不同圈子下混排"),
    POST_NOT_EXIST(10303, "帖子不存在或已被删除"),
    POST_NOT_MORE_TOP(10304, "圈子中的已经存在3个置顶贴"),
    POST_NOT_SOUCH_TAG(10305, "帖子没有该标签记录"),
    POST_TAGS_EXIST(10306, "帖子已存在该标签"),
    POST_LIKED_EXIST(10307, "用户已点过赞"),
    POST_LIKED_NOT_EXIST(10308, "用户未点过赞"),
    POST_TYPE_NAME_NOT_NULL(10309, "分类名称不能为空"),
    POST_TYPE_NAME_ILLEGLE(10310, "分类名称必须为2-6个字符"),
    POST_TYPE_NAME_NOT_EXIST(10311, "分类不存在"),
    POST_TYPE_NAME_EXIST(10312, "分类已存在"),
    POST_TITLE_LENGTH(10313, "帖子标题不能超过30个字"),
    POST_MORE_EXIST(10314, "圈子下存在帖子，请先删除帖子"),

    //===========post_tag===============帖子标签相关错误
    //104xx
    POST_TAG_NAME_NULL(10400, "标签名称不能为空"),
    POST_TAG_NAME_EXIST(10401, "标签名称已经存在"),
    POST_TAG_NAME_ILLEGLE(10402, "标签名称必须为2-6个字符"),
    POST_TAG_NOT_EXIST(10403, "标签不存在或已被删除"),
    POST_TAG_SYS(10404, "系统标签，不能删除"),
    POST_TAG_NOT_SYS(10405, "非系统标签，不参与排序"),
    POST_TAG_SYS_ICON_NULL(10406, "系统标签，图标不能为空"),

    //===========post_recommend===============帖子推荐相关错误
    //105xx
    POST_RECOMMEND_NOT_EXIST(10500, "推荐帖子不存在"),
    POST_RECOMMEND_EXIST(10501, "帖子已经推荐了"),

    //===========comment===============评论相关错误
    //106xx
    COMMENT_NOT_EXIST(10600, "评论不存在或已被删除"),
    COMMENT_CONTENT_NOT_NULL(10601, "评论内容不能为空"),
    COMMENT_NO_AUTH(10602, "只能删除自己的评论"),
    COMMENT_LIKE_NOT_EXIST(10603, "点赞记录不存在或已被删除"),
    COMMENT_CONTENT_LENGTH_MORE(10604, "您输入字数过长，请编辑后重试"),
    COMMENT_CONTENT_LENGTH_LESS(10606, "您输入字数过短，请编辑后重试"),
    COMMENT_LIMIT(10605, "发言过于频繁"),

    //===========hot_word===============热词相关错误
    //107xx
    HOT_WORD_EXIST(10700, "热词已存在"),
    HOT_WORD_NOT_EXSIT(10701, "热词不存在"),

    //===========hot_word===============热词相关错误
    //108xx
    BANNER_TYPE_NOT_EXSIT(10800, "Banner类型不存在"),
    BANNER_NOT_EXIST(10801, "Banner不存在"),
    BANNER_REMARK_NOT_NULL(10802, "Banner关联内容不能为空"),
    BANNER_BG_PLATFORM_SEL(10803, "至少选择一个平台"),
    BANNER_BG_IOS_EXIST(10804, "IOS平台背景已存在"),
    BANNER_BG_ANDROID_EXIST(10805, "Android平台背景已存在"),
    BANNER_BG_NOT_EXIST(10806, "背景图不存在"),

    //=============common=================
    MISSING_PARM(20100, "缺少参数"),


    //=============upload=================
    //上传相关错误
    //202xx
    FILE_TYPE_NOT_SUPPORT(20200, "不支持的文件类型"),

    //=============sensitive=================
    //敏感词相关错误
    //203xx
    SENSITIVE_CHARSET_NOT_SUPPORT(20300, "不支持的文件编码"),
    SENSITIVE_CSV_SUPPORT(20301, "请上传csv格式的文件"),
    SENSITIVE_NOT_EXIST(20302, "敏感词不存在或已被删除"),

    //=============feedback=================
    //反馈相关错误
    //204xx
    FEEDBACK_MOBILE_NULL(20400, "联系方式为空"),
    FEEDBACK_CONTENT_NULL(20401, "反馈内容为空"),

    //=============sysmsg=================
    //系统消息相关错误
    //205xx
    SYSMSG_CONTENT_NULL(20500, "系统消息内容为空"),
    SYSMSG_NOT_EXSIT(20501, "系统消息不存在"),
    SYSMSG_TITLE_TOO_LONG(20502, "消息标题过长"),


    //=============usertag=================
    //用户标签定义相关错误
    //206xx
    USER_TAG_NAME_NULL(20600, "用户标签名称为空"),
    USER_TAG_EXIST(20601, "用户标签已存在"),
    USER_TAG_NOT_EXIST(20602, "用户标签不存在"),
    USER_FOLLOWED_NOT_NULL(20603, "关注人不能为空"),
    USER_FOLLOWED_NOT_EXIST(20604, "关注人不存在"),
    USER_FOLLOWED_EXIST(20605, "已经关注"),

    //=============usertag=================
    //用户标签定义相关错误
    //207xx
    APP_VERSION_NOT_EXIST(20700, "应用不存在"),
    APP_VERSIO_EXIST(20701, "应用已存在"),

    //=============usertag=================
    //用户标签定义相关错误
    //208xx
    USER_SHIELD_EXIST(20800, "已经被屏蔽"),
    USER_NAME_LENGTH(20801, "请输入2~16个字符的昵称"),
    USER_NAME_EDIT_TIMES_MORE(20802, "修改次数超过上限"),

    //APPVERSIOn
    APK_FORMAT_SUPPORT(20900, "仅支持APK格式文件上次"),


    //===========================uc相关错误码=====================================
    //----------------------------common--------------------------//保留-1到-50
    SUCCESS(0, "成功"),

    SYSTEM_EXCEPTION(99, "操作失败，请重试"),

    IP_NULL(-1, "ip为空"),

    IP_ILLEGLE(-2, "ip格式错误"),

    PASSPORT_NULL(-3, "请输入账号"),

    PASSPORT_NOT_EXIST(-4, "账号不存在"),

    PASSPORT_ILLEGLE(-5, "请输入格式正确的账号"),

    PASSWORD_NULL(-6, "请输入密码"),

    PASSWORD_WRONG(-7, "密码错误"),

    PASSWORD_ILLEGLE(-8, "密码应为6-16个字符，限字母、数字，区分大小写"),

    VERIFY_CODE_NULL(-9, "验证码为空"),

    VERIFY_CODE_ERROR(-10, "验证码错误"),

    SEND_EMAIL_CODE_ONCE_MIN(-11, "一分钟内发送邮件验证码,暂停发送"),

    SEND_SMS_ONCE_MIN(-12, "一分钟内发送短信,暂停发送"),

    USER_STATUS_INVALID(-13, "账号被锁"),

    CAPTCHA_NULL(-14, "请输入验证码"),

    CAPTCHA_WRONG(-15, "验证码错误"),

    CAPTCHA_EXPIRED(-16, "验证码过期"),

    TOKEN_EXCEPTION(-17, "请重新登录"),

    SEND_SMS_LIMIT(-18, "发送手机验证码已达上限"),

    //----------------------------register--------------------------//保留-51到-55
    PASSPORT_EXIST(-51, "账号已注册"),

    //----------------------------login--------------------------//保留-56到-60
    USER_NOT_EXIST(-56, "用户不存在"),
    //----------------------------重置密码--------------------------//保留-61到-65

    //----------------------------修改密码--------------------------//保留-66到-75
    CONFIRM_RESET_NOT_SAME(-66, "两次密码不一致"),

    SAME_WITH_OLD_PWD(-67, "新密码与原密码相同"),

    OLD_PWD_NULL(-68, "请输入原密码"),

    NEW_PWD_NULL(-69, "请输入新密码"),

    OLD_PWD_ERROR(-70, "原密码有误，请重新输入"),

    CONFIRM_PWD_NULL(-71, "确认密码为空"),

    //------------------------------修改用户信息------------------------//保留-76到-85
    AVATAR_PARAM_NULL(-76, "请选择头像"),

    GENDER_PARAM_NULL(-77, "请选择性别"),

    GENDER_PARAM_ILLEGLE(-78, "性别参数非法"),

    USERNAME_NULL(-79, "请输入昵称"),

    USERNAME_ILLEGLE(-80, "昵称非法"),

    //------------------------第三方错误码-------------------------//保留-86到-95
    BIND_FAIL(-86, "绑定失败,请重试"),

    BIND_PASSPORT_EXIST(-87, "仅可绑定未注册账号"),

    HAS_BIND(-88, "账号已绑定"),

    //------------------------开放平台错误码-------------------------//保留-96到-105
    OPEN_PASSPORT_NULL(-3, "请输入账号"),

    OPEN_PASSPORT_NOT_EXIST(-4, "账号不存在"),

    OPEN_PASSPORT_ILLEGLE(-5, "账号格式不正确"),

    OPEN_PASSPORT_EXIST(-51, "该账号已注册"),

    //--------------------------根据id获取用户信息---------------------//保留-106到-115
    USERID_NULL(-106, "参数为空"),

    USERID_ILLEGLE(-107, "参数非法"),

    //--------------------------------------------
    IDCARD_CHECK_LIMIT(-120, "请联系客服"),

    IDCARD_CHECKED(-121, "账号已进行实名认证"),

    IDCARD_ILLEGLE(-122, "请输入格式正确的身份证号"),

    NAME_ILLEGLE(-123, "姓名只支持汉字"),
    NAME_LIMIT(-124, "姓名必须是2~6个字符"),
    NAME_NOT_NULL(-125, "姓名不能为空"),

    BIZTYPE_NOT_EXIST(-550, "非法的业务类型"),
    GENDER_NOT_NULL(-551, "性別不能为空"),
    AGE_NOT_NULL(-552, "年龄不能为空"),
    AGE_ILLEGLE(-553, "年龄范围1-120"),
    NEED_CAPTCHA(-554, "");

    private int code;
    private String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public static final ErrorCode getFromKey(int code) {
        for (ErrorCode e : ErrorCode.values()) {
            if (e.getCode() == code) {
                return e;
            }
        }
        return null;
    }

    public static final ErrorCode getFromValue(String message) {
        for (ErrorCode e : ErrorCode.values()) {
            if (e.getMsg().equals(message)) {
                return e;
            }
        }
        return null;
    }
}
