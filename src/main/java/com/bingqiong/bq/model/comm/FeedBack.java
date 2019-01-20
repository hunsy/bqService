package com.bingqiong.bq.model.comm;

import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.utils.MDateKit;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户反馈
 * Created by hunsy on 2017/5/19.
 */
public class FeedBack extends Model<FeedBack> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static FeedBack dao = new FeedBack();
    public static final String TABLE_FEEDBACK = "t_feedback";

    /**
     * 保存反馈
     *
     * @param feedBack
     * @return
     */
    @Before(Tx.class)
    public boolean saveFb(FeedBack feedBack) throws BizException {

        if (StringUtils.isEmpty(feedBack.getStr("mobile"))) {
            logger.error("联系方式为空");
            throw new BizException(ErrorCode.FEEDBACK_MOBILE_NULL);
        }

        if (StringUtils.isEmpty(feedBack.getStr("content"))) {
            logger.error("反馈内容为空");
            throw new BizException(ErrorCode.FEEDBACK_CONTENT_NULL);
        }
        feedBack.set("created_at", MDateKit.getNow());
        return feedBack.save();
    }

    /**
     * 分页查询
     *
     * @param pageRequest
     * @return
     */
    public Page<FeedBack> findPage(PageRequest pageRequest) {
        String sql = "select * ";
        String sql_ex = " form t_feedback where 1= 1";
        List<String> params = new ArrayList<>();
        if (!pageRequest.getParams().isEmpty()) {
            if (StringUtils.isNotEmpty(pageRequest.getParams().get("content"))) {
                sql_ex += " and content like ? ";
                params.add("%" + pageRequest.getParams().get("content") + "%");
            }

            if (StringUtils.isNotEmpty(pageRequest.getParams().get("mobile"))) {
                sql_ex += " and mobile = ? ";
                params.add(pageRequest.getParams().get("mobile"));
            }
        }
        sql_ex += " order by created_at desc ";
        return paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, params.toArray());
    }
}
