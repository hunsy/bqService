package com.bingqiong.bq.comm.utils;

import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.exception.BizException;
import org.apache.commons.codec.binary.Base64;

import java.util.HashMap;
import java.util.Map;

/**
 * Uc令牌解析
 * Created by hunsy on 2017/6/28.
 */
public class TokenUtils {

    /**
     * 继续Uc的令牌，获取用户id
     *
     * @param accessToken
     * @return
     * @throws Exception
     */
    public static Map<String, String> parseToken(String accessToken) throws Exception {
        Map<String, String> map;
        try {
            map = new HashMap<String, String>();
            String tokens[] = accessToken.split("\\" + "|");
            String userId = new String(Base64.decodeBase64(tokens[1].getBytes()));
            map.put("userId", userId);
        } catch (Exception e) {
            throw new BizException(ErrorCode.TOKEN_EXCEPTION);
        }
        return map;
    }
}
