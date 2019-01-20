package com.bingqiong.bq.utils;

import com.jfinal.plugin.activerecord.Record;

import net.sf.json.JSONObject;

import java.util.Iterator;

/**
 * Created by hunsy on 2017/4/7.
 */
public class JsonRecordUtil {

    /**
     * 解析json str
     *
     * @param str
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Record parseJson(String str) {
        JSONObject object = JSONObject.fromObject(str);
        Iterator<String> ite = object.keys();
        Record record = new Record();
        while (ite.hasNext()) {
            String key = ite.next();
            Object value = object.opt(key);
            if (value != null) {
                record.set(key, value);
            }
        }
        return record;
    }


}
