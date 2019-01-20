package com.bingqiong.bq.cms.controller.comm;

import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.comm.Banner;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * banner相关请求
 * <p>
 * Created by hunsy on 2017/6/26.
 */
public class BannerController extends IBaseController {

    private Logger logger = LoggerFactory.getLogger(getClass());

//    public void index() {
//
//        render("banner_page.html");
//    }
//
//    public void form() {
//        Long id = getParaToLong(-1);
//        if (id == null) {
//            setAttr("banner", new Banner());
//            setAttr("action", "save");
//        } else {
//            setAttr("banner", Banner.dao.findById(id));
//            setAttr("action", "update");
//        }
//        render("banner_form.html");
//    }
//
//    @Before(PageInterceptor.class)
//    public void page2() {
//
//        try {
//
//            int offset = getParaToInt("iDisplayStart", 0);
//            int size = getParaToInt("iDisplayLength", 10);
//            int page;
//            if (offset > 0 && offset % size == 0) {
//                page = (offset / size) + 1;
//            } else {
//                page = (offset / size) + 1;
//            }
//
//            PageRequest pageRequest = getAttr("pageRequest");
//            pageRequest.setPageNo(page);
//            pageRequest.setPageSize(size);
//
//            Page<Record> ps = Banner.dao.findPage(pageRequest);
//            Map<String, Object> map = new HashMap<String, Object>();
//            map.put("aaData", ps.getList());
//            map.put("iTotalRecords", ps.getTotalRow());
//            map.put("iTotalDisplayRecords", ps.getTotalRow());
//            map.put("sEcho", getPara("sEcho"));
//            map.put("sColumns", getPara("sColumns"));
//            renderJson(map);
//        } catch (Exception e) {
//            renderFailure(e);
//        }
//    }

    /**
     * 分页查询
     */
    @Before(PageInterceptor.class)
    public void page() {

        try {

            PageRequest pageRequest = getAttr("pageRequest");
            Page<Record> page = Banner.dao.findPage(pageRequest);
            renderSuccess(page);
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    /**
     * 保存
     */
    public void save() {

        try {
            Banner banner = getModel(Banner.class);
            Banner.dao.saveBanner(banner);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 更新
     */
    public void update() {

        try {

            Banner banner = getModel(Banner.class);
            Banner.dao.updateBanner(banner);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 删除
     */
    public void delete() {

        try {
            Long id = getParaToLong("id");
            Banner.dao.deleteBanner(id);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 排序
     */
    public void sort() {

        try {
            String[] ids = getParaValues("ids");
            List<Banner> banners = findListByIds(ids);
            for (int i = 0; i < banners.size(); i++) {
                Banner.dao.updateBanner(banners.get(i).set("idx", banners.size() - i));
            }
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 上下架
     */
    public void sold() {

        try {
            Long id = getParaToLong("id");
            Integer status = getParaToInt("status");
            if (id == null || status == null) {
                logger.error("缺少参数");
                throw new BizException(ErrorCode.MISSING_PARM);
            }
            Banner banner = Banner.dao.findById(id);
            if (banner == null) {
                logger.error("Banner不存在");
                throw new BizException(ErrorCode.BANNER_NOT_EXIST);
            }
            banner.set("status", status);
            Banner.dao.updateBanner(banner);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * @param ids
     * @return
     */
    private List<Banner> findListByIds(String[] ids) throws BizException {
        if (ids[0].indexOf(",") > 0) {
            ids = ids[0].split(",");
        }
        if (ids == null || ids.length == 0) {
            logger.error("缺少参数ids");
            throw new BizException(ErrorCode.MISSING_PARM);
        }
        //遍历查询
        //所有的板块都存在时，才进行遍历删除
        List<Banner> banners = new ArrayList<Banner>();
        for (String id : ids) {
            Banner banner = Banner.dao.findById(id);
            if (banner == null) {
                logger.error("Banner不存在->id:{}", id);
                throw new BizException(ErrorCode.BANNER_NOT_EXIST);
            }
            banners.add(banner);
        }
        return banners;
    }

}
