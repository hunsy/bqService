package com.bingqiong.bq.model.msg;

import com.bingqiong.bq.model.BaseModel;
import com.jfinal.plugin.activerecord.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 删除。
 * <p>
 * Created by hunsy on 2017/7/24.
 */
public class PmValid extends BaseModel<PmValid> {


    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final PmValid dao = new PmValid();
    public static final String TABLE_PM_VALID = "t_pm_valid";

    /**
     * 一条消息，会增加两条pmValid.用于分别标记谁不读这条消息。
     *
     * @param pm_id   消息id
     * @param user_id 用户id
     */
    public void savePmValid(Long pm_id, String user_id) {

        PmValid pmValid = new PmValid();
        pmValid.set("pm_id", pm_id);
        pmValid.set("user_id", user_id);
        pmValid.save();
    }


    /**
     * 删除pmValid,标记谁不读这条消息
     *
     * @param pm_id   消息id
     * @param user_id 用户id
     */
    public void deletePmValid(Long pm_id, String user_id) {

        Db.update("update t_pm_valid set valid = 0 where pm_id = ? and user_id = ?", pm_id, user_id);
    }

}
