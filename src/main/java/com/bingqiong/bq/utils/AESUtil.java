package com.bingqiong.bq.utils;


import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * Created by hunsy on 2017/4/1.
 */
public class AESUtil {


    /**
     * 加密
     *
     * @param content 需要加密的内容
     * @param passKey 加密秘钥
     * @return
     */
    public byte[] encrypt(String content, String passKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128, new SecureRandom(passKey.getBytes()));
        SecretKey secretKey = kgen.generateKey();
        byte[] enCodeFormat = secretKey.getEncoded();
        SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
        Cipher cipher = Cipher.getInstance("AES");// 创建密码器
        byte[] byteContent = content.getBytes("utf-8");
        cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
        byte[] result = cipher.doFinal(byteContent);
        return result; // 加密
    }

    /**
     * 解密
     *
     * @param content 待解密内容
     * @param passKey 解密密钥
     * @return
     */
    public byte[] decrypt(byte[] content, String passKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128, new SecureRandom(passKey.getBytes()));
        SecretKey secretKey = kgen.generateKey();
        byte[] enCodeFormat = secretKey.getEncoded();
        SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
        Cipher cipher = Cipher.getInstance("AES");// 创建密码器
        cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
        byte[] result = cipher.doFinal(content);
        return result; // 解密内容

    }

    public String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    public byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

//    public static void main(String[] args) throws Exception {
//        AESUtil util = new AESUtil();
//        String oripwd = "MQ|YnFfMDAwMDAwMDIy|1478769176865|70bb28f92672dde511581574e0e2c532";
//        String content = "C9543BF9DEA8159C34A379625BCF260CACB96D3812CEB813296D9EC8B2321E1AF6CE3E00CCE64A6F7AA91B2A3846FFEAA168A0742B2432B60008CF3CCB97D396B8AB44612B6D26CA266567729386D66F";
//        String salt = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCQhIvT60RAT0FJ8tqlV9wN1i8hE37h3IXOxxBwAxkqonFgHw8ucGKT3w8ApGhEgvdRegkHM8/y8MB3l/Q1YOdQMq5DNV5/nHSuYZOTyPE7YlgRj6Xve9KgsRdQT76JIPfCyJd2qCZzsVxsUENZppYPZHAITiOvd8zK9M4cagtkJwIDAQAB";
////        //加密
////        System.out.println("加密前：" + "123456");
////        byte[] encryptResult = util.encrypt("123456", salt);
////        String encryptResultStr = util.parseByte2HexStr(encryptResult);
////        System.out.println("加密后：" + encryptResultStr);
////
//        byte[] encryptResult2 = util.parseHexStr2Byte("3A1A029100B5DD3AD4863E038F7B4651");
//        byte[] strs = util.decrypt(encryptResult2, salt);
////        //解密
////        byte[] decryptFrom = parseHexStr2Byte(content);
////        byte[] decryptResult = decrypt(decryptFrom,salt);
//        System.out.println("解密后：" + new String(strs));
//    }


}
