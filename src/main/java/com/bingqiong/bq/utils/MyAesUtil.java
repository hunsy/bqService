package com.bingqiong.bq.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by hunsy on 2017/4/12.
 */
public class MyAesUtil {

    private static MyAesUtil myAesUtil;
    private static String passKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCQhIvT60RAT0FJ8tqlV9wN1i8hE37h3IXOxxBwAxkqonFgHw8ucGKT3w8ApGhEgvdRegkHM8/y8MB3l/Q1YOdQMq5DNV5/nHSuYZOTyPE7YlgRj6Xve9KgsRdQT76JIPfCyJd2qCZzsVxsUENZppYPZHAITiOvd8zK9M4cagtkJwIDAQAB";
    private AESUtil aesUtil = new AESUtil();

    private MyAesUtil() {

    }

    public static MyAesUtil getInstance() {
        if (myAesUtil == null) {
            myAesUtil = new MyAesUtil();
        }
        return myAesUtil;
    }

    public void init(String passKey) {
        this.setPassKey(passKey);
    }

    /**
     * 加密
     *
     * @param str
     * @return
     * @throws Exception
     */
    public String encode(String str) throws Exception {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        byte[] bytes = aesUtil.encrypt(str, getPassKey());
        return aesUtil.parseByte2HexStr(bytes);
    }

    /**
     * 加密
     *
     * @param str
     * @param salt
     * @return
     * @throws Exception
     */
    public String encode(String str, String salt) throws Exception {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        byte[] bytes = aesUtil.encrypt(str, salt);
        return aesUtil.parseByte2HexStr(bytes);
    }

    public String decode(String str) throws Exception {
        byte[] bytes = aesUtil.parseHexStr2Byte(str);
        byte[] decoded_bytes = aesUtil.decrypt(bytes, getPassKey());
        return new String(decoded_bytes, "utf-8");
    }

    public static String getPassKey() {
        return passKey;
    }

    public static void setPassKey(String passKey) {
        MyAesUtil.passKey = passKey;
    }

}
