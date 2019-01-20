package com.bingqiong.bq.cms.controller.comm;

import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.comm.SearchHotWord;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 热词相关请求
 * Created by hunsy on 2017/6/25.
 */
public class SearchHotWordController extends IBaseController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Before(PageInterceptor.class)
    public void page() {


        try {
            PageRequest pageRequest = getAttr("pageRequest");
            Page<SearchHotWord> pss = SearchHotWord.dao.findPage(pageRequest);
            renderSuccess(pss);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 新增热词
     */
    public void save() {

        try {
            String text = getPara("text");
            if (StringUtils.isEmpty(text)) {
                logger.error("缺少参数text");
                throw new BizException(ErrorCode.MISSING_PARM);
            }

            SearchHotWord word = SearchHotWord.dao.findByText(text);
            if (word != null) {
                logger.error("热词已存在->text:{}", text);
                throw new BizException(ErrorCode.HOT_WORD_EXIST);
            }

            SearchHotWord.dao.saveWord(text);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 删除热词
     */
    public void delete() {

        try {
            Long id = getParaToLong(-1);

            SearchHotWord word = SearchHotWord.dao.findById(id);
            if (word == null) {
                logger.error("热词不存在");
                throw new BizException(ErrorCode.PLATE_NOT_EXIST);
            }

            SearchHotWord.dao.deleteWord(word);
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
            List<SearchHotWord> words = findListByIds(ids);
            for (int i = 0; i < words.size(); i++) {
                SearchHotWord.dao.updateWord(words.get(i).set("idx", words.size() - i));
            }
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 获取热词列表
     *
     * @param ids
     * @return
     */
    private List<SearchHotWord> findListByIds(String[] ids) throws BizException {

        if (ids[0].indexOf(",") > 0) {
            ids = ids[0].split(",");
        }
        if (ids == null || ids.length == 0) {
            logger.error("缺少参数ids");
            throw new BizException(ErrorCode.MISSING_PARM);
        }
        //遍历查询
        //所有的热词都存在时，才进行遍历删除
        List<SearchHotWord> words = new ArrayList<SearchHotWord>();
        for (String id : ids) {
            SearchHotWord word = SearchHotWord.dao.findById(Long.parseLong(id));
            if (word == null) {
                logger.error("热词不存在->id:{}", id);
                throw new BizException(ErrorCode.HOT_WORD_NOT_EXSIT);
            }
            words.add(word);
        }
        return words;
    }


}
