package com.bingqiong.bq.comm.vo;

import com.jfinal.kit.JsonKit;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Created by hunsy on 2017/5/23.
 */
public class PageRequest implements Serializable {

    private int pageNo = 1;
    private int pageSize = 10;
    private Map<String, String> params = new HashMap<String, String>();

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    /**
     * 获取基本的参数拼接
     *
     * @return
     */
    public String getSimple() {
        System.out.println(JsonKit.toJson(params));
        if (!params.keySet().isEmpty()) {
            String sql_ex = "";
            Iterator<String> ite = params.keySet().iterator();
            while (ite.hasNext()) {
                String key = ite.next();
                if (StringUtils.isNotEmpty(params.get(key))) {
                    sql_ex += " and " + key + "= ? ";
                } else {
                    params.remove(key);
                }
            }
            return sql_ex;
        }
        return "";
    }

    /**
     * 获取值
     *
     * @return
     */
    public Object[] getSimpleValues() {

        if (params.keySet().isEmpty()) {
            return new Object[]{};
        }

        Iterator<Map.Entry<String, String>> ite = params.entrySet().iterator();
        List<String> ls = new ArrayList<String>();
        while (ite.hasNext()) {
            Map.Entry<String, String> entry = ite.next();
            if (entry.getValue() != null) {
                ls.add(entry.getValue());
            }
        }
        return ls.toArray();
    }
}
