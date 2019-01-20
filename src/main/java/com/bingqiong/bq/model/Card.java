package com.bingqiong.bq.model;

import com.bingqiong.bq.model.base.BaseCard;
import com.jfinal.plugin.activerecord.Page;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by hunsy on 2017/5/5.
 */
public class Card extends BaseCard {


    /**
     *
     */
    private static final long serialVersionUID = 4710054705019784264L;
    public static Card dao = new Card();

    /**
     * 保存实名认证
     */
    public void saveCard(Card card) {
        card.set("created_at", new Date());
        card.save();
    }

    /**
     * 获取分页
     *
     * @param page
     * @param size
     * @param params
     * @return
     */
    public Page<Card> findPage(int page, int size, Map<String, String> params) {

        String sql = "select * ";
        String sql_ex = "from t_card " +
                "where 1 = 1 ";
        List<String> lp = new ArrayList<>();
        for (Map.Entry<String, String> en : params.entrySet()) {
            String key = en.getKey();
            String value = en.getValue();
            sql_ex += " and " + key + "= ? ";
            lp.add(value);
        }
        if (lp.isEmpty()) {
            return paginate(page, size, sql, sql_ex);
        } else {
            return paginate(page, size, sql, sql_ex, lp.toArray());
        }
    }


    public Card findByUid(Object uid) {

        return findFirst("select * from t_card where uid = ?", uid);
    }
}
