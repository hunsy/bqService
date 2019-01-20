package com.bingqiong.bq.cms.controller.comment;

import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.comment.Comment;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 评论相关请求
 * <p>
 * Created by hunsy on 2017/6/25.
 */
public class CommentController extends IBaseController {


    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 分页相关请求
     */
    @Before(PageInterceptor.class)
    public void page() {

        try {

            PageRequest pageRequest = getAttr("pageRequest");
            Page<Record> prs = Comment.dao.findAdminPage(pageRequest);
            renderSuccess(prs);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 删除评论
     */
    public void delete() {

        try {
            Long id = getParaToLong("id");
            Comment.dao.deleteComment(id);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }


    public void batchdelete() {

        try {
            String[] ids = getParaValues("ids");
            if (ids[0].indexOf(",") > 0) {
                ids = ids[0].split(",");
            }

            if (ids == null || ids.length == 0) {
                logger.error("缺少参数ids");
                throw new BizException(ErrorCode.MISSING_PARM);
            }
            logger.info("ids:{}", JsonKit.toJson(ids));
            List<Comment> comments = new ArrayList<>();
            for (int i = 0; i < ids.length; i++) {
                Object id = ids[i];
                logger.info("待删除的id:{}", id);
                Comment comment = Comment.dao.findById(id);
                if (comment == null) {
                    throw new BizException(ErrorCode.COMMENT_NOT_EXIST);
                }
                comments.add(comment);
            }
            for (Comment comment : comments) {
                Comment.dao.deleteComment(comment);
            }
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }


}
