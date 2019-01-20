package com.bingqiong.bq.cms.controller.category;

import com.alibaba.fastjson.JSON;
import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.category.Group;
import com.bingqiong.bq.model.category.Plate;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 板块请求
 * Created by hunsy on 2017/6/21.
 */
public class PlateController extends IBaseController {

    private Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * 获取板块列表
     */
    public void list() {

        try {

            List<Record> records = Plate.dao.findList();
            renderSuccess(records);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 分页查询
     */
    @Before(PageInterceptor.class)
    public void page() {

        try {
            PageRequest pageRequest = getAttr("pageRequest");
            Page<Plate> pd = Plate.dao.findPage(pageRequest);
            renderSuccess(pd);
        } catch (Exception e) {
            renderJson(handleException(e));
        }
    }

    /**
     * 保存
     */
    public void save() {

        try {
            Plate plate = getModel(Plate.class);
            logger.info("plate:{}", JSON.toJSONString(plate));
            Plate.dao.savePlate(plate);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 更新
     */
    public void update() {

        try {
            Plate plate = getModel(Plate.class);
            Plate.dao.updatePlate(plate);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 删除板块
     */
    public void delete() {

        try {
            Long id = getParaToLong(-1);
            Plate.dao.deletePlate(id);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 批量删除
     */
    public void batchdelete() {

        try {
            String[] ids = getParaValues("ids");
            List<Plate> plates = findListByIds(ids);
            for (Plate plate : plates) {
                Plate.dao.deletePlate(plate);
            }
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
            List<Plate> plates = findListByIds(ids);
            //倒序
            for (int i = 0; i < plates.size(); i++) {
                Plate plate = plates.get(i);
                plate.set("idx", plates.size() - i);
                Plate.dao.updatePlate(plate);
            }
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 板块上下架
     */
    public void sold() {

        try {
            Long id = getParaToLong("id");
            Integer status = getParaToInt("status");
            if (id == null || status == null) {
                logger.error("缺少参数");
                throw new BizException(ErrorCode.MISSING_PARM);
            }

            Plate plate = Plate.dao.findById(id);
            if (plate == null) {
                logger.error("板块不存在->id:{}", id);
                throw new BizException(ErrorCode.PLATE_NOT_EXIST);
            }

            plate.set("status", status);
            Plate.dao.updatePlate(plate);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }

    }


    /**
     * 根据id，获取板块列表
     *
     * @param ids
     * @return
     * @throws BizException
     */
    private List<Plate> findListByIds(String[] ids) throws BizException {
        if (ids[0].indexOf(",") > 0) {
            ids = ids[0].split(",");
        }
        if (ids == null || ids.length == 0) {
            logger.error("缺少参数ids");
            throw new BizException(ErrorCode.MISSING_PARM);
        }
        //遍历查询
        //所有的板块都存在时，才进行遍历删除
        List<Plate> plates = new ArrayList<Plate>();
        for (String id : ids) {
            Plate plate = Plate.dao.findById(Long.parseLong(id));
            if (plate == null) {
                logger.error("板块不存在->id:{}", id);
                throw new BizException(ErrorCode.PLATE_NOT_EXIST);
            }
            plates.add(plate);
        }
        return plates;
    }

}
