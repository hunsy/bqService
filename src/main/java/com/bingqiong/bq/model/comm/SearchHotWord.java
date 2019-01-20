package com.bingqiong.bq.model.comm;

import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.utils.MDateKit;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.BaseModel;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 搜索热词
 * Created by hunsy on 2017/6/25.
 */
public class SearchHotWord extends BaseModel<SearchHotWord> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final SearchHotWord dao = new SearchHotWord();
    public static final String TABLE_HOT_WORD = "t_search_hot_words";

    /**
     * 新增或更新热词
     */
    public boolean saveOrUpdateWord(String text) throws IOException {
        //// TODO: 2017/6/25 敏感词过滤
        text = Sensitive.dao.filterSensitive(text);
        //非敏感词。记录
        SearchHotWord word = findByText(text);
        if (word == null) {
            return saveWord(text);
        }
        word.set("count", word.getInt("count") + 1);
        return updateWord(word);
    }

    /**
     * 新增新热词
     *
     * @param text
     * @return
     */
    @Before(Tx.class)
    public boolean saveWord(String text) {

        SearchHotWord word = new SearchHotWord();
        word.set("text", text);
        Date date = MDateKit.getNow();
        word.set("created_at", date);
        word.set("updated_at", date);
        word.set("count", 1);
        return word.save();
    }

    /**
     * 更新热词
     *
     * @param word
     * @return
     */
    @Before(Tx.class)
    public boolean updateWord(SearchHotWord word) {

        word.set("updated_at", MDateKit.getNow());
        return word.update();
    }


    /**
     * 通过词查询
     *
     * @param text
     * @return
     */
    public SearchHotWord findByText(String text) {

        return findFirst("select * from t_search_hot_words where text = ? ", text);
    }

    /**
     * 分页查询
     *
     * @param pageRequest
     * @return
     */
    public Page<SearchHotWord> findPage(PageRequest pageRequest) {

        String sql = "select * ";
        String sql_ex = "from t_search_hot_words " +
                "where 1 = 1 ";
        List<Object> lp = new ArrayList<>();
        if (StringUtils.isNotEmpty(pageRequest.getParams().get("text"))) {
            sql_ex += " and text like ? ";
            lp.add("%" + pageRequest.getParams().get("text") + "%");
        }

        sql_ex += "order by idx desc,count desc";
        return paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, lp.toArray());
    }

    /**
     * 删除热词,物理删除
     *
     * @param word
     */
    public boolean deleteWord(SearchHotWord word) throws BizException {

        if (word == null) {
            logger.error("热词不存在");
            throw new BizException(ErrorCode.HOT_WORD_NOT_EXSIT);
        }

        return word.delete();
    }

    /**
     * 获取前一百个热词
     *
     * @return
     */
    public List<Record> findList() {

        String sql = "select id,text,count from t_search_hot_words order by idx desc,count desc limit 0,100";
        return Db.find(sql);
    }
}
