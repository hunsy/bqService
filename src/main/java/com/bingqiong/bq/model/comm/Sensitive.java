package com.bingqiong.bq.model.comm;

import com.bingqiong.bq.comm.constants.Constants;
import com.bingqiong.bq.comm.utils.SensetiveUtil;
import com.bingqiong.bq.comm.vo.PageRequest;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 敏感词管理
 * Created by hunsy on 2017/5/3.
 */
public class Sensitive extends Model<Sensitive> {


    /**
     *
     */
    private static final long serialVersionUID = -2353175238803216686L;
    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final Sensitive dao = new Sensitive();
    public static final String TABLE_SENSITIVE = "t_sensitive";

    /**
     * 新增或更新，敏感词
     *
     * @param text 敏感词
     */
    @Before(Tx.class)
    public void saveOrupdateSensitive(String text) throws IOException {

        if (StringUtils.isEmpty(text)) {

            return;
        }

        if (!textExsit(text)) {
            Sensitive sensitive = new Sensitive()
                    .set("text", text)
                    .set("created_at", new Date());
            sensitive.save();
            SensetiveUtil.getInstance().addSensitiveWord(text);
            //缓存敏感词
//            Redis.use().sadd(Constants.REDIS_SENSITIVE_KEY, text);
//            File file = new File(PathKit.getRootClassPath() + "dic.txt");
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//            FileUtils.writeByteArrayToFile(file, text.getBytes());
//            Set<String> lz = new HashSet<>();
//            lz.add(text);
//            IKAnalyzerUtil.extendDic(lz);
        }
    }

    /**
     * 查询敏感词是否存在
     *
     * @param text 查询text是否存在
     * @return 返回查询结果
     */
    private boolean textExsit(String text) {

        Cache cache = Redis.use();
        boolean flag = cache.sismember(Constants.REDIS_SENSITIVE_KEY, text);
        if (flag) {
            Sensitive sensitive = findFirst("select * from t_sensitive where text = ? ", text);
            flag = sensitive != null;
            if (flag) {
                cache.sadd(Constants.REDIS_SENSITIVE_KEY, text);
//                Set<String> lz = new HashSet<>();
//                lz.add(text);
//                IKAnalyzerUtil.extendDic(lz);
                SensetiveUtil.getInstance().addSensitiveWord(text);
            }
        }
        return flag;
    }

    /**
     * 删除分词
     *
     * @param id id
     */
    public void deleteSensitive(Long id) {

        Sensitive sensitive = findById(id);
        if (sensitive == null) {
            return;
        }
        //删除缓存中的敏感词
        boolean flag = sensitive.delete();
        if (flag) {
            Redis.use().srem(Constants.REDIS_SENSITIVE_KEY, sensitive.getStr("text"));
//            IKAnalyzerUtil.stopDic(sensitive.getStr("text"));
            SensetiveUtil.getInstance().remove(sensitive.getStr("text"));
        }
    }

    /**
     * 分页查询
     *
     * @param pageRequest 查询条件
     * @return 返回查询的分页数据
     */
    public Page<Record> findPage(PageRequest pageRequest) {
        String sql = "select * ";
        String sql_ex = "from t_sensitive where 1 = 1  ";

        List<String> params = new ArrayList<>();
        if (!pageRequest.getParams().keySet().isEmpty()) {
            String text = pageRequest.getParams().get("text");
            if (StringUtils.isNotEmpty(text)) {
                params.add(text);
                sql_ex += " and text like ? ";
            }
//            sql_ex += " order by text asc,created_at desc";
//            return Db.paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, "%" + text + "%");
        }
        sql_ex += " order by text asc,created_at desc";
        return Db.paginate(pageRequest.getPageNo(), pageRequest.getPageSize(), sql, sql_ex, params.toArray());
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤的内容
     * @return 返回过滤后的内容
     * @throws IOException
     */
    public String filterSensitive(String text) throws IOException {
        if (StringUtils.isEmpty(text)) {
            return null;
        }
//        List<String> ls = IKAnalyzerUtil.getInstance().analyzer(text);
//        StringBuilder sb = new StringBuilder();
//        for (String str : ls) {
//            logger.info("str:{}", str);
//            if (Redis.use().sismember(Constants.REDIS_SENSITIVE_KEY, str)) {
////                sb.append("***");
////                text.replace(str, "***");
//                text = StringUtils.replace(text, str, "***");
//                logger.info("存在敏感词:{}", str);
//            }
//            else {
//                sb.append(str);
//            }
//        }

        text = SensetiveUtil.getInstance().fileter(text);
        String fstr = EmojiParser.parseToUnicode(text);
//        logger.info("过滤后的文字:{}", fstr);
        return fstr;
    }


}
