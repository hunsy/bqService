package com.bingqiong.bq.controller.admin;

import com.bingqiong.bq.constant.BqErrorCode;
import com.bingqiong.bq.exception.BizException;
import com.bingqiong.bq.interceptor.PageInterceptor;
import com.bingqiong.bq.model.Sensitive;
import com.bingqiong.bq.utils.IKAnalyzerUtil;
import com.bingqiong.bq.utils.MyFileUtils;
import com.bingqiong.bq.vo.PageRequest;
import com.bingqiong.bq.vo.ResponseDataVo;
import com.bingqiong.bq.vo.ResponseEmptyVo;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.symmetric.csv.CsvReader;

import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 敏感词
 * Created by hunsy on 2017/5/5.
 */
public class SensitiveController extends BaseController {

    /**
     * 上传敏感词
     */
    public void upload() {
        String msg = "";
        try {
            UploadFile uf = getFile("file");
            logger.info("文件编码:{}", MyFileUtils.getFilecharset(uf.getFile()));
            String charset = MyFileUtils.getFilecharset(uf.getFile());
            if (!StringUtils.equalsIgnoreCase("GBK", charset) && StringUtils.equalsIgnoreCase("UTF-8", charset)) {
                msg = "不支持的文本编码";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }

            String fileSubfix = uf.getOriginalFileName().substring(uf.getOriginalFileName().lastIndexOf(".") + 1);
            if (!fileSubfix.equals("csv")) {
                msg = "请上传csv文件";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            CsvReader reader = new CsvReader(new FileInputStream(uf.getFile()), Charset.forName(charset));
            reader.readHeaders();
            Set<String > lz = new HashSet<>();
            while (reader.readRecord()) {
                String s = reader.getRawRecord();
                logger.info("xxx:{}",s);
//                String ss[] = s.split(",");
//                for (String sx : ss) {
                    if (StringUtils.isNotEmpty(s)){
                        Sensitive.dao.updateSensitive(s);
                        lz.add(s);
                    }
//                    lz.add(sx);
//                }

            }
            IKAnalyzerUtil.extendDic(lz);
            uf.getFile().delete();
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, msg);
        }
    }

    /**
     * 保存敏感词
     */
    public void save() {
        String msg = "";
        try {
            String s = getPara("text");
            Sensitive.dao.updateSensitive(s);
            Set<String > lz = new HashSet<>();
            lz.add(s);
            IKAnalyzerUtil.extendDic(lz);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, msg);
        }

    }

    /**
     * 保存敏感词
     */
    public void delete() {
        String msg = "";
        try {
            Long id = getParaToLong("id");
            if (id == null) {
                msg = "缺少参数id";
                throw new BizException(BqErrorCode.CODE_FAILED.getCode());
            }
            Db.update("delete from t_sensitive where id = ? ", id);
            renderJson(ResponseEmptyVo.success());
        } catch (Exception e) {
            handleException(e, msg);
        }

    }

    /**
     * 分页。
     */
    @Before(PageInterceptor.class)
    public void page() {
        String errMsg = "";
        try {
            String text = getPara("param_text");
            logger.info("text -> text:{}", text);
//            int page = getParaToInt("pageNo", 1);
//            int size = getParaToInt("pageSize", 10);
            PageRequest pageRequest = getAttr("pageRequest");
            logger.info("pageRequest:{}", JsonKit.toJson(pageRequest));
            Page<Record> ps = Sensitive.dao.recordPage(pageRequest.getPageNo(), pageRequest.getPageSize(), text);
            renderJson(ResponseDataVo.success(ps));
        } catch (Exception e) {
            handleException(e, errMsg);
        }
    }

}
