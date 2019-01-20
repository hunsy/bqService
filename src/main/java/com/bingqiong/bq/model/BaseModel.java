package com.bingqiong.bq.model;

import com.jfinal.plugin.activerecord.Model;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 基础类
 * Created by hunsy on 2017/6/21.
 */
public class BaseModel<M extends Model<M>> extends Model<M> {


    protected String andPlatform(String sql, String showType) {

        if (StringUtils.isNotEmpty(showType)) {

            if (StringUtils.equals(showType, "ios")) {
                sql += " and ios_show = 1 ";
            }

            if (StringUtils.equals(showType, "android")) {
                sql += " and android_show = 1 ";
            }
        }
        return sql;
    }
}
