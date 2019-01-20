package com.bingqiong.bq.comm.vo;

import com.alibaba.fastjson.JSONArray;

import java.io.Serializable;

/**
 * Created by hunsy on 2017/4/10.
 */
public class RespData implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -3024157744385007263L;
    private int totalCount = 0;
    private JSONArray list;

    RespData(int total, JSONArray list) {
        this.totalCount = total;
        this.list = list;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public JSONArray getList() {
        return list;
    }

    public void setList(JSONArray list) {
        this.list = list;
    }
}