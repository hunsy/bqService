package com.bingqiong.bq.model.comm;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import com.bingqiong.bq.comm.constants.Constants;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.utils.JpushUtil;
import com.bingqiong.bq.comm.utils.MDateKit;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.BaseModel;
import com.bingqiong.bq.model.msg.MsgReadAt;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Redis;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 系统通知
 * Created by hunsy on 2017/6/27.
 */
public class SysMsg extends BaseModel<SysMsg> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final SysMsg dao = new SysMsg();
    public static final String TABLE_SYS_MSG = "t_sys_msg";

    /**
     * 因为系统消息，
     * 会有很多人查看，
     * 所以要进行缓存
     *
     * @param idValue
     * @return
     */
    @Override
    public SysMsg findById(Object idValue) {

        SysMsg msg = Redis.use().hget(Constants.REDIS_SYS_MSG_KEY, idValue.toString());
        if (msg == null) {
            msg = findFirst("select * from t_sys_msg where valid = 1 and id  = ? ", idValue);
            //缓存消息
            if (msg != null) {
                Redis.use().hset(Constants.REDIS_SYS_MSG_KEY, msg.get("id").toString(), msg);
            }
        }
        return msg;
    }

    /**
     * 新增系统通知
     *
     * @param msg
     */
    @Before(Tx.class)
    public boolean saveMsg(SysMsg msg) throws BizException, APIConnectionException, APIRequestException {

        if (StringUtils.isEmpty(msg.getStr("content"))) {
            logger.error("系统消息内容为空");
            throw new BizException(ErrorCode.SYSMSG_CONTENT_NULL);
        }

        msg.set("created_at", MDateKit.getNow());
        boolean flag = msg.save();
        if (flag) {
            //
            Redis.use().hset(Constants.REDIS_SYS_MSG_KEY, msg.get("id").toString(), msg);
        }
        return flag;
    }


    /**
     * 获取分页
     *
     * @param pageRequest
     * @return
     */
    public Page<SysMsg> findAdminPage(PageRequest pageRequest) {
        String sql = "select id,title,content,status,created_at ";
        String sql_ex = "from t_sys_msg where valid = 1 ";

        Map<String, String> params = pageRequest.getParams();
        List<String> lp = new ArrayList<>();
        if (StringUtils.isNotEmpty(params.get("title"))) {
            sql_ex += " and title like ? ";
            lp.add("%" + params.get("title") + "%");
        }
        if (StringUtils.isNotEmpty(params.get("content"))) {
            sql_ex += " and content like ? ";
            lp.add("%" + params.get("content") + "%");
        }

        sql_ex += " order by created_at desc ";
        int page = pageRequest.getPageNo();
        int size = pageRequest.getPageSize();
        return paginate(page, size, sql, sql_ex, lp.toArray());
    }

    /**
     * 获取分页
     *
     * @param pageRequest
     * @return
     */
    public Page<SysMsg> findPage(PageRequest pageRequest) {
        String sql = "select id,title,content,created_at ";
        String sql_ex = "from t_sys_msg where valid = 1 and status = 1 ";
        int page = pageRequest.getPageNo();
        int size = pageRequest.getPageSize();
        String user_id = pageRequest.getParams().get("user_id");
        if (StringUtils.isNotEmpty(user_id)) {
            String uid = user_id.toString();
            MsgReadAt.dao.createdReadAt(uid, 3);
            MsgReadAt.dao.createdReadAt(pageRequest.getParams().get("device_id"), 3);
        } else {
            MsgReadAt.dao.createdReadAt(pageRequest.getParams().get("device_id"), 3);
        }
        sql_ex += " order by created_at desc";
        return paginate(page, size, sql, sql_ex);
    }

    /**
     * 获取当前用户未读的系统消息
     *
     * @param user_id
     * @return
     */
    public Long getMsgCount(String user_id) {

        MsgReadAt readAt = MsgReadAt.dao.get(user_id, 3);
        String sql = "select count(id) from t_sys_msg where valid = 1 and status = 1  ";
        if (readAt == null) {
            return Db.queryLong(sql);
        } else {
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(readAt.getDate("read_at"));
            return Db.queryLong(sql + " and created_at > ? ", date);
        }
    }

    /**
     * 发送消息
     *
     * @param id
     */
    @Before(Tx.class)
    public void send(Long id) throws BizException, APIConnectionException, APIRequestException {
        SysMsg msg = findById(id);
        if (msg == null) {

            throw new BizException(ErrorCode.SYSMSG_NOT_EXSIT);
        }
        msg.set("status", 1);
        boolean flag = updateMsg(msg);
        if (flag) {
            JpushUtil.getInstance().setSysMsg(msg.getLong("id").toString());
        }
    }

    /**
     * 删除消息
     *
     * @param id
     * @throws BizException
     */
    @Before(Tx.class)
    public void deleteMsg(Long id) throws BizException {
        SysMsg msg = findById(id);
        if (msg == null) {

            throw new BizException(ErrorCode.SYSMSG_NOT_EXSIT);
        }

        msg.set("valid", 0);
        boolean flag = msg.update();
        if (flag) {
            Redis.use().hdel(Constants.REDIS_SYS_MSG_KEY, id.toString());
        }
    }

    @Before(Tx.class)
    public boolean updateMsg(SysMsg msg) throws BizException {
        SysMsg dbMsg = findById(msg.getLong("id"));
        if (dbMsg == null) {
            throw new BizException(ErrorCode.SYSMSG_NOT_EXSIT);
        }
        boolean flag = msg.update();
        if (flag) {
            msg = findById(msg.getLong("id"));
            Redis.use().hset(Constants.REDIS_SYS_MSG_KEY, msg.getLong("id"), msg);
        }
        return flag;
    }
}
