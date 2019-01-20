package com.bingqiong.bq.vo;

import java.io.Serializable;

/**
 * Created by hunsy on 2017/5/23.
 */
public class PageRequest implements Serializable {

    private int pageNo = 1;
    private int pageSize = 10;

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
}
