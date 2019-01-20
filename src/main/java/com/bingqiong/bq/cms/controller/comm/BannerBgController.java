package com.bingqiong.bq.cms.controller.comm;

import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.comm.BannerBg;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * 轮播图背景相关请求
 * <p>
 * Created by hunsy on 2017/7/26.
 */
public class BannerBgController extends IBaseController {


    public void save() {

        try {
            BannerBg bannerBg = getModel(BannerBg.class);
            if (bannerBg.getInt("ios_show") == null){
                bannerBg.set("ios_show",1);
            }
            if (bannerBg.getInt("android_show") == null){
                bannerBg.set("android_show",1);
            }
            BannerBg.dao.saveBg(bannerBg);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    public void update() {

        try {
            BannerBg bannerBg = getModel(BannerBg.class);
            if (bannerBg.getInt("ios_show") == null){
                bannerBg.set("ios_show",1);
            }
            if (bannerBg.getInt("android_show") == null){
                bannerBg.set("android_show",1);
            }
            BannerBg.dao.updateBg(bannerBg);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    public void delete() {

        try {
            Long id = getParaToLong(-1);
            BannerBg.dao.deleteBg(id);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    public void sold() {

        try {
            Long id = getParaToLong("id");
            int status = getParaToInt("status");

            BannerBg bannerBg = BannerBg.dao.findById(id);
            if (bannerBg == null) {
                throw new BizException(ErrorCode.BANNER_BG_NOT_EXIST);
            }
            bannerBg.set("status", status);
            BannerBg.dao.updateBg(bannerBg);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    @Before(PageInterceptor.class)
    public void page() {

        try {
            PageRequest pageRequest = getAttr("pageRequest");
            Page<Record> page = BannerBg.dao.findPage(pageRequest);
            renderSuccess(page);
        } catch (Exception e) {
            renderFailure(e);
        }
    }
}
