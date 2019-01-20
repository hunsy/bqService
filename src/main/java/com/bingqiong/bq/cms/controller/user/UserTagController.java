//package com.bingqiong.bq.cms.controller.user;
//
//import com.bingqiong.bq.comm.controller.IBaseController;
//import com.bingqiong.bq.comm.interceptor.PageInterceptor;
//import com.bingqiong.bq.comm.vo.PageRequest;
//import com.jfinal.aop.Before;
//import com.jfinal.plugin.activerecord.Page;
//
///**
// * 用户标签定义
// * Created by hunsy on 2017/6/28.
// */
//public class UserTagController extends IBaseController {
//
//
//    /**
//     * 分页查询
//     */
//    @Before(PageInterceptor.class)
//    public void page() {
//
//        try {
//
//            PageRequest pageRequest = getAttr("pageRequest");
//            Page<UserTag> tags = UserTag.dao.findPage(pageRequest);
//            renderSuccess(tags);
//        } catch (Exception e) {
//            renderFailure(e);
//        }
//    }
//
//    /**
//     * 保存
//     */
//    public void save() {
//
//        try {
//
//            UserTag tag = getModel(UserTag.class);
//            UserTag.dao.saveTag(tag);
//            renderSuccess();
//        } catch (Exception e) {
//            renderFailure(e);
//        }
//    }
//
//    /**
//     * 删除
//     */
//    public void delete() {
//
//        try {
//
//            Long id = getParaToLong(-1);
//            UserTag.dao.deleteTag(id);
//            renderSuccess();
//        } catch (Exception e) {
//            renderFailure(e);
//        }
//    }
//}
