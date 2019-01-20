package com.bingqiong.bq.model.category;

import com.bingqiong.bq.comm.constants.Constants;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.utils.MDateKit;
import com.bingqiong.bq.comm.utils.ValidateUtils;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.BaseModel;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Redis;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 板块
 * Created by hunsy on 2017/6/21.
 */
public class Plate extends BaseModel<Plate> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static Plate dao = new Plate();
    public static final String TABLE_PLATE = "t_plate";

    /**
     * 创建板块
     *
     * @param plate 板块记录
     * @return 新增板块结果
     */
    @Before(Tx.class)
    public boolean savePlate(Plate plate) throws BizException {
        Date mdate = MDateKit.getNow();
        plate.set("created_at", mdate);
        plate.set("updated_at", mdate);
        //验证名称
        validatePlateName(plate.getStr("name"));
        boolean flag = plate.save();
        if (flag) {
            //查询时，会缓存
            findById(plate.getLong("id"));
            //缓存名称
            Redis.use().sadd(Constants.REDIS_PLATE_NAME_KEY, plate.getStr("name"));
            //删除板块圈子列表的缓存
            removePlateGroupCache();
        }
        return flag;
    }

    /**
     * 更新板块
     *
     * @param plate 板块记录
     * @return 返回更新结果
     */
    @Before(Tx.class)
    public boolean updatePlate(Plate plate) throws BizException {

        Plate dbPlate = findById(plate.getLong("id"));
        if (dbPlate == null) {
            logger.error("板块不存在");
            throw new BizException(ErrorCode.PLATE_NOT_EXIST);
        }
        boolean change = false;//名称是否变更
        //板块名称变更判断
        if (!StringUtils.equals(plate.getStr("name"), dbPlate.getStr("name"))) {
            //验证新的板块名称
            validatePlateName(plate.getStr("name"));
            change = true;
        }
        //更新时间
        dbPlate.set("updated_at", MDateKit.getNow());
        boolean flag = plate.update();
        if (flag) {
            //获取最新的对象，覆盖缓存对象
            plate = findFirst("select * from t_plate where valid = 1 and id = ?", plate.getLong("id"));
            Redis.use().hset(Constants.REDIS_PLATE_KEY, plate.getLong("id"), plate);
            if (change) {
                //去除原有
                Redis.use().srem(Constants.REDIS_PLATE_NAME_KEY, dbPlate.getStr("name"));
                //新增名称
                Redis.use().sadd(Constants.REDIS_PLATE_NAME_KEY, plate.getStr("name"));
            }
            //删除板块圈子列表的缓存
            removePlateGroupCache();
        }
        return flag;
    }

    /**
     * 删除
     *
     * @param id 板块id
     * @return 返回删除结果
     */
    @Before(Tx.class)
    public boolean deletePlate(Long id) throws BizException {
        if (id == null) {
            logger.error("id为空");
            throw new BizException(ErrorCode.MISSING_PARM);
        }
        Plate dbPlate = findById(id);
        return deletePlate(dbPlate);
    }

    /**
     * 删除
     *
     * @param plate 板块记录
     * @return 返回删除结果
     */
    @Before(Tx.class)
    public boolean deletePlate(Plate plate) throws BizException {
        if (plate == null) {
            logger.error("板块不存在");
            throw new BizException(ErrorCode.PLATE_NOT_EXIST);
        }
        Long groups = Db.queryLong("select count(id) from t_group where plate_id = ? and valid = 1", plate.getLong("id"));
        //存在圈子，不能删除帖子
        if (groups > 0) {
            throw new BizException(ErrorCode.PLATE_NOT_DEL);
        }
        plate.set("valid", 0);
        plate.set("updated_at", MDateKit.getNow());
        boolean flag = plate.update();
        if (flag) {
            //删除缓存对象
            Redis.use().hdel(Constants.REDIS_PLATE_KEY, plate.getLong("id"));
            //去除名称
            Redis.use().srem(Constants.REDIS_PLATE_NAME_KEY, plate.getStr("name"));
            //删除板块圈子列表的缓存
            removePlateGroupCache();
        }
        return flag;
    }

    /**
     * 获取板块记录。
     * 首先查询缓存中的记录，再从DB中查询,如果db中存在，则缓存起来。
     *
     * @param idValue 板块id
     * @return 返回获取的板块记录
     */
    public Plate findById(Long idValue) {

        logger.info("plate_id:{}", idValue);
        if (idValue == null) {
            return null;
        }
        Plate plate = Redis.use().hget(Constants.REDIS_PLATE_KEY, idValue);
        if (plate == null) {
            plate = findFirst("select * from t_plate where valid = 1 and id = ?", idValue);
            if (plate != null) {
                //缓存对象
                Redis.use().hset(Constants.REDIS_PLATE_KEY, idValue, plate);
            }
        }
        return plate;
    }

    /**
     * 查询板块名称是否存在。
     *
     * @param name 板块名称
     * @return 返回是否存在的结果
     */
    private boolean findByName(String name) {

        boolean flag = Redis.use().sismember(Constants.REDIS_PLATE_NAME_KEY, name);
        if (!flag) {
            Plate plate = findFirst("select * from " + TABLE_PLATE + " where name = ? and valid = 1", name);
            if (plate != null) {
                flag = true;
                //缓存名称
                Redis.use().sadd(Constants.REDIS_PLATE_NAME_KEY, name);
            }
        }
        return flag;
    }

    /**
     * 查询分页
     *
     * @param pageRequest 查询条件
     * @return 返回查询的分页数据
     */
    public Page<Plate> findPage(PageRequest pageRequest) {

        String sql = "select id,name,thumb_url,status,groups,created_at,updated_at,ios_show,android_show  ";
        String sql_ex = "from t_plate " +
                "where valid = 1 ";
//        if (pageRequest.getSimple() != null) {
//            sql_ex += pageRequest.getSimple();
//        }

        Map<String, String> params = pageRequest.getParams();
        List<String> lp = new ArrayList<>();
        if (StringUtils.isNotEmpty(params.get("name"))) {
            sql_ex += " and name like ? ";
            lp.add("%" + params.get("name") + "%");
        }

        if (StringUtils.isNotEmpty(params.get("id"))) {
            sql_ex += " and id = ? ";
            lp.add(params.get("id"));
        }

        if (StringUtils.isNotEmpty(params.get("status"))) {
            sql_ex += " and status = ? ";
            lp.add(params.get("status"));
        }


        if (StringUtils.isNotEmpty(params.get("ios_show"))) {
            sql_ex += " and ios_show = ? ";
            lp.add(params.get("ios_show"));
        }

        if (StringUtils.isNotEmpty(params.get("android_show"))) {
            sql_ex += " and android_show = ? ";
            lp.add(params.get("android_show"));
        }

        sql_ex += " order by idx desc,created_at desc";
        return paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, lp.toArray());
    }

    /**
     * 更新圈子数。
     * 增1 减1
     *
     * @return 返回更新结果
     */
    boolean updateGroups(Long id, int step) throws BizException {

        Plate plate = findById(id);
        if (plate == null) {
            logger.error("板块不存在");
            throw new BizException(ErrorCode.PLATE_NOT_EXIST);
        }

        Long count = Db.queryLong("select count(id) from t_group where plate_id = ? and valid = 1 and status = 1", id);
//        int num;
//        if (step < 0 && plate.getInt("groups") <= 0) {
//            num = 0;
//        } else {
//            int tmp = plate.getInt("groups") <= 0 ? 0 : plate.getInt("groups");
//            num = tmp + step;
//            num = plate.getInt("groups") + step;
//        }
        plate.set("groups", count);
        return updatePlate(plate);
    }

    /**
     * 验证板块名称
     *
     * @param name 板块名称
     */
    private void validatePlateName(String name) throws BizException {
        //名称不能为空
        if (StringUtils.isEmpty(name)) {
            logger.error("板块名称不能为空");
            throw new BizException(ErrorCode.PLATE_NAME_NULL);
        }
        //查询名称是否存在
        if (findByName(name)) {
            logger.error("板块名称已经存在->name:{}", name);
            throw new BizException(ErrorCode.PLATE_NAME_EXIST);
        }

        if (!ValidateUtils.validateStrLen(name, Constants.PLATE_NAME_LEN_MIN, Constants.PLATE_NAME_LEN_MAX)) {
            logger.error("板块名称格式错误");
            throw new BizException(ErrorCode.PLATE_NAME_ILLEGLE);
        }
    }

    /**
     * 获取板块和圈子
     *
     * @param showType ios | android
     */
    public List<Record> plateAndGroup(String showType) {


        //获取所有的板块
        List<Record> plates = Redis.use().get(Constants.REDIS_PLATE_GROUP_KEY + showType);
        if (plates == null) {
            String plate_sql = "select id,name,thumb_url from t_plate " +
                    "where valid = 1 and status = 1 ";

            plate_sql = andPlatform(plate_sql, showType);
            plate_sql += " order by idx desc,created_at desc";
            //----------------------
            plates = Db.find(plate_sql);
            if (CollectionUtils.isNotEmpty(plates)) {
                for (Record plate : plates) {
                    String sql = "select id,name,thumb_url,follows,posts,created_at " +
                            "from t_group " +
                            "where valid = 1 and status = 1 and plate_id = ?  ";
                    //----------------------
                    sql = andPlatform(sql, showType);
                    sql += "order by idx desc,created_at desc limit 0,6";
                    List<Record> groups = Db.find(sql, plate.getLong("id"));
                    if (CollectionUtils.isNotEmpty(groups)) {
                        plate.set("groups", groups);
                    }
                }
                //缓存对象
                Redis.use().set(Constants.REDIS_PLATE_GROUP_KEY, plates);
            }
        }
        return plates;
    }

    /**
     * 删除板块和圈子列表的缓存.
     * 再板块新增，编辑和删除时都要清除。
     * 再圈子新增（不需要单独删，因为更新板块的圈子数时会更新板块），编辑和删除（同圈子新增）时也要删除。
     */
    void removePlateGroupCache() {
        Redis.use().del(Constants.REDIS_PLATE_GROUP_KEY);
    }

    /**
     * 获取列表。用于下拉列表展示
     *
     * @return 返回所有的板块列表。
     */
    public List<Record> findList() {

        return Db.find("select id as value,name " +
                "from t_plate " +
                "where valid = 1 " +
                "order by idx desc,created_at desc");
    }
}
