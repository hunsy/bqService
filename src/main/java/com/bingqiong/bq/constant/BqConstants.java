package com.bingqiong.bq.constant;

/**
 * Created by hunsy on 2017/4/7.
 */
public interface BqConstants {


    final String BQ_APPLICATION = "bq_v2:";

    /**
     * 用户收藏列表key
     */
    final String REDIS_USER_COLLECTION_PRIFEX = BQ_APPLICATION + "user:collection:";
    /**
     * 用户关注列表key
     */
    final String REDIS_USER_FOLLOW_PRIFEX = BQ_APPLICATION + "user:follow:";
    /**
     * 文章类型列表key
     */
    final String REDIS_ARTICLE_TYPE_LIST = BQ_APPLICATION + "article_type:list";

    /**
     * 敏感词key
     */
    final String REDIS_SENSITIVE_KEY = BQ_APPLICATION + "sensitive";// sensitive

    /**
     * BUid
     */
    final String BUID = "DOME002";


    /**
     * 用户id所在位置，参见tokenUtil类中token格式
     */
    public static final int USERID_POSITION = 1;

    /**
     * 冰穹用户id
     */
    public static final String DOME_USER_ID = "domeUserId";

    /**
     * 冰穹用户名称
     */
    public static final String DOME_USER_NAME = "domeUserName";

    int PASSWORD_MIN_LENGTH = 6;
    int PASSWORD_MAX_LENGTH = 16;

    String  PASSWORD_ALLOW_CHAR = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!|@#$%^&*()_-+=.:'<>?/~`";

}
