package com.bingqiong.bq.vo;

import java.io.Serializable;
import java.util.List;

/**
 * Created by hunsy on 2017/5/15.
 */
public class ApkInfo implements Serializable {

    private String versionCode;
    private String versionName;
    private String apkPackage;
    private String minSdkVersion;
    private List<String> uses_permission;

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setApkPackage(String apkPackage) {
        this.apkPackage = apkPackage;
    }

    public void setMinSdkVersion(String minSdkVersion) {
        this.minSdkVersion = minSdkVersion;
    }

    public void setUses_permission(List uses_permission) {
        this.uses_permission = uses_permission;
    }


}
