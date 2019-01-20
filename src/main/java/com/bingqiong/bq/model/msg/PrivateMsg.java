package com.bingqiong.bq.model.msg;

import com.bingqiong.bq.comm.utils.JpushUtil;
import com.bingqiong.bq.comm.utils.MDateKit;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.BaseModel;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 私信
 * Created by hunsy on 2017/7/3.
 */
public class PrivateMsg extends BaseModel<PrivateMsg> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final PrivateMsg dao = new PrivateMsg();
    public static final String TABLE_PRIVATE_MSG = "t_user_private_msg";


    /**
     * 发送私信
     *
     * @param from 发送者
     * @param to   接受者
     * @param msg  消息内容
     * @return 发送结果
     */
    @Before(Tx.class)
    public boolean send(String from, String to, String msg) {

        PrivateMsg pm = new PrivateMsg();
        pm.set("from_user", from);
        pm.set("to_user", to);
        pm.set("content", msg);
        pm.set("created_at", MDateKit.getNow());
        pm.set("msg_tag", msgTag(to, from));

        boolean flag = pm.save();
        if (flag) {
            //发送消息
            logger.info("开始发送私信->from:{},to:{}", from, to);
            JpushUtil.getInstance().pushPm(pm.getLong("id").toString(), to);

            //增加两条是否读消息记录
            PmValid.dao.savePmValid(pm.getLong("id"), from);
            PmValid.dao.savePmValid(pm.getLong("id"), to);

            //给当前用户增加一条消息发自谁的记录
            PmFrom.dao.saveFrom(to, from, pm.getLong("id"));
        }
        return flag;
    }

    /**
     * 获取消息
     *
     * @param pageRequest 请求参数
     * @return 返回请求分页数据
     */
    public Page<Record> findPage(PageRequest pageRequest) {

        String sql = "select pm.id,pm.content,pm.created_at," +
                "pm.from_user,tu.avatar_url as from_avatar_url,pm.read," +
                "pm.to_user,tuu.user_name as to_user_name,tuu.avatar_url as to_avatar_url ";
        String sql_ex = " from t_pm_valid pv " +
                "left join t_user_private_msg pm on pv.pm_id = pm.id " +
                "left join t_user tu on pm.from_user = tu.user_id " +
                "left join t_user tuu on pm.to_user = tuu.user_id " +
                "where pv.valid = 1 and pv.user_id = ? and pm.msg_tag = ? ";
        sql_ex += "order by created_at desc ";
        List<String> lp = new ArrayList<>();
        Map<String, String> params = pageRequest.getParams();
        lp.add(params.get("to"));
        lp.add(msgTag(params.get("from"), params.get("to")));

        Page<Record> page = Db.paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, lp.toArray());
        if (CollectionUtils.isNotEmpty(page.getList())) {
//            for (Record record : page.getList()) {
//                int i = 0;
//                //未读，则清除未读记录
//                if (record.getInt("read") == 0) {
////                    clearCount(record.getLong("id"));
//                    i = i + 1;
//                }
            //清除未读数
            PmFrom.dao.clearCount(params.get("to"), params.get("from"));
//            }
        }
        return page;
    }

    /**
     * @param to 当前用户
     */
    public List<Record> msgcount(String to) {

        String sql = "select  pm.from_user,pm.count,tu.user_name,pm.last_msg," +
                "tu.avatar_url from t_pm_from pm " +
                "left join t_user tu on pm.from_user = tu.user_id " +
                "where pm.user_id = ? order by pm.updated_at desc";

        List<Record> users = Db.find(sql, to);

        for (Record record : users) {
//            long count = Db.queryLong("select count(id) " +
//                    "from t_user_private_msg " +
//                    "where to_user = ? and from_user = ?  and `read` = 0", to, record.getStr("from_user"));
//            record.set("count", count);
//            Record msg = findFirstMsg(to, record.getStr("from_user"));
            Record msg = Db.findFirst("select * from t_user_private_msg " +
                    "where id = ? ", record.getLong("last_msg"));
            record.set("msg", msg);
        }
        return users;
    }


//    /**
//     * 清空未读数
//     *
////     * @param msgId 消息id
//     */
//    private void clearCount(Long msgId) {
//
//        PrivateMsg pm = findById(msgId);
//        pm.set("read", 1);
//        pm.update();
//    }

//    /**
//     * 获取从发送者那里发来的发送的最后一条消息。
//     *
//     * @param to   接受者
//     * @param from 发送者
//     * @return 返回消息
//     */
//    private Record findFirstMsg(String to, String from) {
//        return Db.findFirst("select * from t_user_private_msg  where " +
//                " to_user = ? and from_user = ? " +
//                " order by created_at desc", to, from);
//    }

    /**
     * 消息标签。用来定位两个人之间的消息的唯一值。
     * 按user_id的大小排序。从小->大
     * 例如：bq_00000001bq_000000002
     *
     * @return 返回tag
     */
    private String msgTag(String user1, String user2) {

        long l1 = parseUserId(user1);
        long l2 = parseUserId(user2);
        if (l1 > l2) {
            return user2 + user1;
        } else {
            return user1 + user2;
        }
    }

    /**
     * 解析userId获取其序列。
     *
     * @param userId 用户id
     * @return 返回去掉前缀后的long类型值
     */
    private long parseUserId(String userId) {
        String str = userId.substring(3);
        return Long.parseLong(str);
    }


    public static void main(String[] args) {

        System.out.print(new PrivateMsg().parseUserId("bq_100001"));
    }
}
