package com.bingqiong.bq.controller.admin;

import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.interceptor.PageInterceptor;
import com.bingqiong.bq.model.Card;
import com.bingqiong.bq.utils.RequestUtil;
import com.bingqiong.bq.vo.PageRequest;
import com.bingqiong.bq.vo.ResponseDataVo;
import com.bingqiong.bq.vo.ResponseEmptyVo;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Page;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by hunsy on 2017/5/5.
 */
public class CardController extends BaseController {


    /**
     * 分页。
     */
    @Before(PageInterceptor.class)
    public void page() {
        String errMsg = "";
        try {
//            int page = getParaToInt("pageNo", 1);
//            int size = getParaToInt("pageSize", 10);
            PageRequest pageRequest = getAttr("pageRequest");
            logger.info("pageRequest:{}", JsonKit.toJson(pageRequest));
            Page<Card> ps = Card.dao.findPage(pageRequest.getPageNo(), pageRequest.getPageSize(), RequestUtil.getParams(getRequest()));
            renderJson(ResponseDataVo.success(ps));
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

    /**
     * 删除
     */
    public void delete() {
        String errMsg = "";
        try {
            String uid = getPara("uid");
            if (StringUtils.isEmpty(uid)) {
                errMsg = "缺少参数uid";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            Card card = Card.dao.findByUid(uid);
            if (card == null) {
                errMsg = "不存在该实名记录";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            card.delete();
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

}
