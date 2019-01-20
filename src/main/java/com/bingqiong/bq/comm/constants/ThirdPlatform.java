package com.bingqiong.bq.comm.constants;

/**
 * Created by hunsy on 2017/10/13.
 */
public enum ThirdPlatform {
    WB("sinaweibo",2),
    WX("wechat",6),
    QQ("qq",3);

    private String platform;
    private Integer thirdId;

    ThirdPlatform(String platform, Integer thirdId) {
        this.platform = platform;
        this.thirdId = thirdId;
    }

    public String getPlatform() {
        return platform;
    }

    public Integer getThirdId() {
        return thirdId;
    }

    public static final ThirdPlatform getFromKey(String platform) {
        for (ThirdPlatform e : ThirdPlatform.values()) {
            if (e.platform.equals(platform)) {
                return e;
            }
        }
        return null;
    }

}
