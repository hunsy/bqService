package com.bingqiong.bq.comm.utils;

import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Page;

import java.util.*;

/**
 * Created by hunsy on 2017/5/15.
 */
public class ChannelsUtils {

    private static ChannelsUtils utils = null;
    private static Page<Map<String, Object>> page;

    private ChannelsUtils() {
        init();
    }

    public static ChannelsUtils getInstance() {
        if (utils == null)
            utils = new ChannelsUtils();

        return utils;
    }

    public void init() {
        Properties properties = PropKit.use("channels.properties").getProperties();
        if (properties != null && properties.entrySet() != null) {
            List<Map<String, Object>> ls = new ArrayList<>();
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                Map<String, Object> channel = new HashMap<>();
                channel.put("channel_code", entry.getKey());
                channel.put("channel_name", entry.getValue());
                ls.add(channel);
            }
            page = new Page<>(ls, 1, 15, 1, ls.size());
        }
    }

    public Page<Map<String, Object>> getChannels() {
        return page;
    }

}
