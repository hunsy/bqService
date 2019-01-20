package com.bingqiong.bq.comm.utils;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * 时间工具
 * Created by hunsy on 2017/6/21.
 */
public class MDateKit {


    /**
     * 获取当前的时间
     *
     * @return
     */
    public static Date getNow() {

        return DateTime.now().toDate();
    }

}
