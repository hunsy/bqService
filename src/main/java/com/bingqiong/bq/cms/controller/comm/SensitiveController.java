package com.bingqiong.bq.cms.controller.comm;

import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.controller.IBaseController;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.interceptor.GlobalInterceptor;
import com.bingqiong.bq.comm.interceptor.PageInterceptor;
import com.bingqiong.bq.comm.utils.MyFileUtils;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.bingqiong.bq.model.comm.Sensitive;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.symmetric.csv.CsvReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 敏感词相关请求
 * Created by hunsy on 2017/5/5.
 */
public class SensitiveController extends IBaseController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static String charsets[] = new String[]{"gbk", "utf-8"};

    /**
     * 上传敏感词
     */
    @Clear
    @Before(GlobalInterceptor.class)
    public void upload() {
        try {
            UploadFile uf = getFile("file");
            String charset = MyFileUtils.getFilecharset(uf.getFile());
            logger.info("文件编码:{}", charset);

            if (!ArrayUtils.contains(charsets, charset.toLowerCase())) {
                logger.error("不支持的文本编码");
                throw new BizException(ErrorCode.SENSITIVE_CHARSET_NOT_SUPPORT);
            }

            String fileSubfix = uf.getOriginalFileName().substring(uf.getOriginalFileName().lastIndexOf(".") + 1);
            if (!fileSubfix.equals("csv")) {
                logger.error("请上传csv文件");
                throw new BizException(ErrorCode.SENSITIVE_CSV_SUPPORT);
            }
            CsvReader reader = new CsvReader(new FileInputStream(uf.getFile()), Charset.forName(charset));
            reader.readHeaders();
//            Set<String> lz = new HashSet<>();
            while (reader.readRecord()) {
                String str = reader.getRawRecord();
                if (StringUtils.isNotEmpty(str)) {
                    Sensitive.dao.saveOrupdateSensitive(str);
//                    lz.add(str);
                }
            }
//            IKAnalyzerUtil.extendDic(lz);
            uf.getFile().delete();
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 保存敏感词
     */
    public void save() {
        try {
            String text = getPara("text");
            Sensitive.dao.saveOrupdateSensitive(text);
//            Set<String> lz = new HashSet<>();
//            lz.add(text);
//            IKAnalyzerUtil.extendDic(lz);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }

    }

    /**
     * 保存敏感词
     */
    public void delete() {
        try {
            Long id = getParaToLong("id");
            if (id == null) {
                logger.error("缺少参数");
                throw new BizException(ErrorCode.MISSING_PARM);
            }
            Sensitive.dao.deleteSensitive(id);
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 分页。
     */
    @Before(PageInterceptor.class)
    public void page() {

        try {

            PageRequest pageRequest = getAttr("pageRequest");
            logger.info("pageRequest:{}", JsonKit.toJson(pageRequest));
            Page<Record> ps = Sensitive.dao.findPage(pageRequest);
            renderSuccess(ps);
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
            List<Sensitive> ls = findListByIds(ids);
            for (Sensitive s : ls) {
                Sensitive.dao.deleteSensitive(s.getLong("id"));
            }
            renderSuccess();
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * @param ids
     * @return
     */
    private List<Sensitive> findListByIds(String[] ids) throws BizException {
        if (ids[0].indexOf(",") > 0) {
            ids = ids[0].split(",");
        }
        if (ids == null || ids.length == 0) {
            logger.error("缺少参数ids");
            throw new BizException(ErrorCode.MISSING_PARM);
        }
        //遍历查询
        //所有的板块都存在时，才进行遍历删除
        List<Sensitive> sensitives = new ArrayList<Sensitive>();
        for (String id : ids) {
            Sensitive sensitive = Sensitive.dao.findById(id);
            if (sensitive == null) {
                logger.error("Sensitive不存在->id:{}", id);
                throw new BizException(ErrorCode.SENSITIVE_NOT_EXIST);
            }
            sensitives.add(sensitive);
        }
        return sensitives;
    }

}
