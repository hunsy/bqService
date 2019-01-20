package com.bingqiong.bq.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by hunsy on 2017/4/7.
 */
public class RequestUtil {

    private static Logger logger = LoggerFactory.getLogger(RequestUtil.class);

    /**
     * 获取参数。
     *
     * @return
     */
    public static JSONObject getDecodeParams(HttpServletRequest request) throws Exception {
        String str = HttpKit.readIncommingRequestData(request);
        logger.info("解密前:{}", str);
        if (StringUtils.isEmpty(str)) {
            return new JSONObject();
        }
        if (EncodeUtils.isEncode()) {
            str = DESUtil.decrypt(str);
        }
        logger.info("解密后:{}", str);
        JSONObject obj = JSONObject.parseObject(str);
        return obj;
    }


    /**
     * 获取参数。
     *
     * @return
     */
    public static Map<String, String> getParams(HttpServletRequest request) {

        Enumeration<String> params = request.getParameterNames();
        Map<String, String> map = new HashMap<>();
        while (params.hasMoreElements()) {
            String key = params.nextElement();
            String value = request.getParameter(key).trim();
            if (StringUtils.startsWith(key, "param_") && StringUtils.isNotEmpty(value)) {
                map.put(key.replace("param_", ""), value);
            }
        }
        return map;
    }

    /**
     * 解析form表单数据。
     *
     * @return
     */
    public static Record parseRecordDecodeToReacord(Record record, HttpServletRequest request) throws Exception {

        String str = HttpKit.readIncommingRequestData(request);
        logger.info("解密前:{}", str);
        if (StringUtils.isEmpty(str)) {
            return new Record();
        }
        if (EncodeUtils.isEncode()) {
            str = DESUtil.decrypt(str);
        }
        logger.info("解密后:{}", str);
        JSONObject obj = JSONObject.parseObject(str);
        Iterator<String> ite = obj.keySet().iterator();
        while (ite.hasNext()) {
            String key = (String) ite.next();
            String value = (String) obj.get(key);
            record.set(key, value);
        }
        return record;
    }

    /**
     * 转为Model
     *
     * @param record  不能为空。
     * @param request
     * @return
     * @throws Exception
     */
    public static Model<?> parseRecordDecode(Model<?> record, HttpServletRequest request) throws Exception {

        String str = HttpKit.readIncommingRequestData(request);
        logger.info("解密前:{}",str);
        if (StringUtils.isEmpty(str)) {
            return record;
        }
        if (EncodeUtils.isEncode()){
            str = DESUtil.decrypt(str);
        }
        logger.info("解密后:{}", str);
        @SuppressWarnings("unchecked")
        HashMap<String, Object> map = JSON.parseObject(str, HashMap.class, Feature.AllowArbitraryCommas);
        record.setAttrs(map);
        return record;
    }

    /**
     * 解析form表单数据。
     *
     * @return
     */
    public static Record parseRecord(Record record, HttpServletRequest request) {

        Enumeration<String> params = request.getParameterNames();
        if (record == null) {
            record = new Record();
        }
        while (params.hasMoreElements()) {
            String key = params.nextElement();
            String value = request.getParameter(key);
            if (StringUtils.isNotEmpty(value.trim())) {
                record.set(key, value);
            }
        }
        return record;
    }

}
