package com.bingqiong.bq.cms.controller.comm;

import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.comm.SysMsg;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 系统消息相关请求
 * <p>
 * Created by hunsy on 2017/6/27.
 */
public class SysmsgController extends IBaseController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 保存
     */
    public void save() {

        try {
            SysMsg msg = getModel(SysMsg.class);
            if (StringUtils.isNotEmpty(msg.getStr("title")) && msg.getStr("title").length() > 128) {
                logger.error("消息标题过长");
                throw new BizException(ErrorCode.SYSMSG_TITLE_TOO_LONG);
            }
            logger.info("msg:{}", msg.getStr("content"));
            SysMsg.dao.saveMsg(msg);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 保存
     */
    public void update() {

        try {
            SysMsg msg = getModel(SysMsg.class);
            logger.info("msg:{}", msg.getStr("content"));
            if (StringUtils.isNotEmpty(msg.getStr("title")) && msg.getStr("title").length() > 128) {
                logger.error("消息标题过长");
                throw new BizException(ErrorCode.SYSMSG_TITLE_TOO_LONG);
            }
            SysMsg.dao.updateMsg(msg);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 分页获取数据
     */
    @Before(PageInterceptor.class)
    public void page() {

        try {
            PageRequest pageRequest = getAttr("pageRequest");
            Page<SysMsg> ps = SysMsg.dao.findAdminPage(pageRequest);
            renderSuccess(ps);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     *
     */
    public void send() {

        try {

            Long id = getParaToLong("id");
            SysMsg.dao.send(id);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    public void delete() {

        try {
            Long id = getParaToLong("id");
            SysMsg.dao.deleteMsg(id);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

}
