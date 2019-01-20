package com.bingqiong.bq.comm.constants;

/**
 * Created by hunsy on 2017/6/21.
 */
public interface Constants {

    String APPLICATION = "bq_v2:";

    //============plate=================

    /**
     * 板块名称的最小长度
     */
    int PLATE_NAME_LEN_MIN = 2;
    /**
     * 板块名称的最大长度
     */
    int PLATE_NAME_LEN_MAX = 6;

    /**
     * 板块。用于保存板块对象的hash，以板块id未field。
     */
    String REDIS_PLATE_KEY = APPLICATION + "plate";
    /**
     * 板块名称。保存已存在的板块名称的set，用于判断名称是否已用
     */
    String REDIS_PLATE_NAME_KEY = APPLICATION + "plate:name";

    /**
     * 板块圈子。保存板块，以及板块下的圈子的key value。
     */
    String REDIS_PLATE_GROUP_KEY = APPLICATION + "plate:group:";

    //============group==================
    /**
     * 圈子名称的最小长度
     */
    int GROUP_NAME_LEN_MIN = 2;

    /**
     * 圈子名称的最大长度
     */
    int GROUP_NAME_LEN_MAX = 6;

    /**
     * 用于保存圈子对象的hash，以圈子id未field。
     */
    String REDIS_GROUP_KEY = APPLICATION + "group";

    /**
     * 圈子名称。保存已存在的板圈子名称的set，用于判断名称是否已用
     */
    String REDIS_GROUP_NAME_KEY = APPLICATION + "group:name";
    //==============group_follows==============
    /**
     * 圈子关注记录缓存
     */
    String REDIS_GROUP_FOLLOWS_KEY = APPLICATION + "group:follows";


    //============post==================


    /**
     * 用于保存帖子对象的hash，以圈子id未field。
     */
    String REDIS_POS_KEY = APPLICATION + "post";

    /**
     * 帖子点赞的用户列表
     */
    String POST_LIKE_PREFIX = APPLICATION + "post:likes:";

    /**
     * 评论限制频率前缀
     */
    String COMMENT_LIMIT_PREFIX = APPLICATION + "comment:limit:";
    /**
     * 评论点赞的用户列表
     */
    String COMMENT_LIKE_PREFIX = APPLICATION + "comment:likes:";
    /**
     * 帖子标签名称的最小长度
     */
    int POST_TAG_NAME_LEN_MIN = 2;

    /**
     * 帖子标签名称的最大长度
     */
    int POST_TAG_NAME_LEN_MAX = 6;


    String REDIS_POST_TYPE_KEY = APPLICATION + "post:type";
    /**
     * 缓存帖子类型的名称的set的key。
     */
    String REDIS_POST_TYPE_NAME_KEY = APPLICATION + "post:type:name";

    /**
     * 缓存帖子类型列表的key.
     */
    String REDIS_POST_TYPE_LIST_KEY = APPLICATION + "post:type:list";

    String REDIS_POST_TAG_KEY = APPLICATION + "post:tag";

    String REDIS_POST_TAG_NAME_KEY = APPLICATION + "post:tag:name";

    String REDIS_POST_TAGS_KEY = APPLICATION + "post:tags";

    /**
     * 帖子下的标签列表
     */
    String REDIS_POST_TAGS_LIST_KEY = APPLICATION + "post:tags:list:";

    //============upload=================
    /**
     * 上传的文件大小
     */
    int REQUEST_MAX_CONTENT_LENGHT = 10 * 1024 * 1024;

    //==========sensitive=========

    /**
     * 敏感词key
     */
    String REDIS_SENSITIVE_KEY = APPLICATION + "sensitive";

    //==========sysmsg=========

    /**
     * 系统消息key
     */
    String REDIS_SYS_MSG_KEY = APPLICATION + "sys_msg";

    //----------user相关-------------

    /**
     * bq的user的Token的前缀
     */
    String REDIS_USER_TOKEN_PRIFEX = APPLICATION + "user:dtoken:";

    /**
     * bq的Token的前缀
     */
    String REDIS_TOKEN_PRIFEX = APPLICATION + "dtoken:";
    /**
     * 用户信息
     */
    String REDIS_USER_KEY = APPLICATION + "user";

    /**
     * 用户关注信息
     */
    String REDIS_USER_FOLLOWS_KEY = APPLICATION + "user:follows";

    /**
     * 用户标签信息
     */
    String REDIS_USER_TAG_KEY = APPLICATION + "user:tag";
    /**
     * 用户与用户标签信息
     */
    String REDIS_USER_TAGS_KEY = APPLICATION + "user:tags";

    /**
     * 用户实名认证
     */
    String REDIS_USER_CARD_KEY = APPLICATION + "user:card";

    /**
     * 用户屏蔽
     */
    String REDIS_USER_SHIELD_KEY = APPLICATION + "user:shield:";

    /**
     * 用户编辑次数前缀
     */
    String REDIS_USER_NAME_EDIT_PREFIX = APPLICATION + "user:username:times:";
    /**
     * 用户信息
     */
    String REDIS_LOGIN_LIMIT = APPLICATION + "login:limit:";


    String BUID = "DOME002";
    /**
     * 冰穹用户id
     */
    String DOME_USER_ID = "domeUserId";
    /**
     * 冰穹用户userName
     */
    String DOME_USER_NAME = "domeUserName";

    //------------password------------------
    int PASSWORD_MIN_LENGTH = 6;
    int PASSWORD_MAX_LENGTH = 16;

    String PASSWORD_ALLOW_CHAR = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!|@#$%^&*()_-+=.:'<>?/~`";

    //-----------msg----------
    /**
     * 私信统计前缀信息
     */
    String REDIS_PM_TIMES = APPLICATION + "pm:times:";

    //-----------msg----------
    /**
     * 私信统计前缀信息
     */
    String REDIS_APP_VERSION = APPLICATION + "app_version:";

    //-----------banner-----------
    /**
     * 存储Banner列表数据的key
     */
    String REDIS_BANNER_LIST_KEY = APPLICATION + "banner:list:";
    /**
     * banner背景图
     */
    String REDIS_BANNER_BG_KEY = APPLICATION + "banner:bg:";

    /**
     * 存放环信的set
     */
    String REDIS_HX = APPLICATION + "hx";
}
