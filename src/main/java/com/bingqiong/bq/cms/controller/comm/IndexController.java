package com.bingqiong.bq.cms.controller.comm;

import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.constants.EsIndexType;
import com.bingqiong.bq.comm.exception.BizException;
import com.bingqiong.bq.comm.interceptor.GlobalInterceptor;
import com.bingqiong.bq.comm.utils.EsUtils;
import com.bingqiong.bq.comm.utils.QiNiuUtil;
import com.bingqiong.bq.conf.BqCmsConf;
import com.bingqiong.bq.model.category.Group;
import com.bingqiong.bq.model.post.Post;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.upload.UploadFile;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by hunsy on 2017/6/27.
 */
public class IndexController extends com.bingqiong.bq.comm.controller.IndexController {

    private Logger logger = LoggerFactory.getLogger(getClass());


    public void geo() {

        String str = HttpKit.readIncommingRequestData(getRequest());
        logger.info("数据:{}", "------------");
        renderJson("success","ok");
    }

    /**
     * 上传BannerBg图片
     * 最大为 1500 * 884 最小为 750 * 442 ,尺寸最大为 800k
     */
    @Clear
    @Before(GlobalInterceptor.class)
    public void uploadBannerBg() {

        try {
            UploadFile file = getFile("filedata");
            Map<String, String> ret = doUpload(file, 800 * 1024, 750, 442, 1500,
                    new BizException(ErrorCode.IMG_UPLOAD_BANNER_BG));
            renderSuccess(ret);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 上传Banner图片
     * 最大为 1280 * 720 最小为 640 * 360 ,尺寸最大为 600k
     */
    @Clear
    @Before(GlobalInterceptor.class)
    public void uploadBanner() {

        try {
            UploadFile file = getFile("filedata");
            Map<String, String> ret = doUpload(file, 600 * 1024, 640, 360, 1280,
                    new BizException(ErrorCode.IMG_UPLOAD_BANNER));
            renderSuccess(ret);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 上传圈子图标
     * 最大为 240 * 240 最小为 108 * 108 ,尺寸最大为 150k
     */
    @Clear
    @Before(GlobalInterceptor.class)
    public void uploadGroupIcon() {

        try {
            UploadFile file = getFile("filedata");
            Map<String, String> ret = doUpload(file, 150 * 1024, 108, 108, 240,
                    new BizException(ErrorCode.IMG_UPLOAD_GROUP_ICON));
            renderSuccess(ret);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 上传板块图标
     * 最大为 56 * 56 最小为 28 * 28 ,尺寸最大为 150k
     */
    @Clear
    @Before(GlobalInterceptor.class)
    public void uploadPlateIcon() {

        try {
            UploadFile file = getFile("filedata");
            Map<String, String> ret = doUpload(file, 20 * 1024, 28, 28, 56,
                    new BizException(ErrorCode.IMG_UPLOAD_PLATE_ICON));
            renderSuccess(ret);
        } catch (Exception e) {
            renderFailure(e);
        }
    }

    /**
     * 进行上传
     *
     * @param file      上传文件
     * @param max       最大的文件大小
     * @param minwidth  最小宽度
     * @param minheight 最小高度
     * @param maxwidth  最大宽度
     * @param biz       抛出异常
     * @return
     * @throws BizException
     * @throws IOException
     */
    private Map<String, String> doUpload(UploadFile file,
                                         long max, int minwidth, int minheight,
                                         int maxwidth, BizException biz) throws BizException, IOException {

        String contentType = file.getContentType();
        if (!ArrayUtils.contains(contentTypes, contentType)) {
            logger.info("不支持的图片类型；{}", contentType);
            throw new BizException(ErrorCode.IMG_UPLOAD_NOT_SUPPORT);
        }
        BufferedImage img = Thumbnails.of(FileUtils.openInputStream(file.getFile()))
                .scale(1)
                .asBufferedImage();
        int width = img.getWidth();
        int height = img.getHeight();
        //Banner背景图尺寸的高长比必须大于9/16，尺寸必须大于640x360
        if (width < minwidth || (height * minwidth != minheight * width)) {
            logger.info("长度不对，或者比例不对");
            throw biz;
        }
        if (width >= minwidth && width < maxwidth) {
            width = minwidth;
        } else {
            width = maxwidth;
        }
        double quality = getQuality(max, file.getFile().length());
        String fileSubfix = file.getOriginalFileName().substring(file.getOriginalFileName().lastIndexOf(".") + 1);
        String tempFileName = UUID.randomUUID().toString() + "." + fileSubfix;
        File temp = new File(file.getSaveDirectory() + tempFileName);
        Thumbnails.of(file.getFile())
                .width(width)
                .outputQuality(quality)
                .toFile(temp);
        String url = QiNiuUtil.getInstance().upload(temp, "thumb_" + RandomStringUtils.randomAlphanumeric(32) + "." + fileSubfix);
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("imgUrl", url);
        temp.deleteOnExit();
        return ret;
    }


    public void index() {

        renderJson("xx", "111");
    }

    public void captcha() {

        String path = getPara("img");
        logger.info("file:{}", path);
        renderFile(new File(BqCmsConf.CAPTCHA_DIR + path));
    }

    public void init() {

//        EsUtils.getInstance().deleteAll(EsIndexType.group.name());
        List<Group> groups = Group.dao.find("select * from t_group where valid = 1");
        for (Group group : groups) {
            logger.info("初始化es->group:{}", group.getLong("id"));
            group.remove("created_at");
            group.remove("updated_at");
            EsUtils.getInstance().createIndex(group.getLong("id").toString(), EsIndexType.group.name(), JsonKit.toJson(group));
        }
//        EsUtils.getInstance().deleteAll(EsIndexType.post.name());

        List<Post> posts = Post.dao.find("select * from t_post where valid = 1");
        for (Post post : posts) {
            logger.info("初始化es->post:{}", post.getLong("id"));
            post.remove("created_at");
            post.remove("updated_at");
            EsUtils.getInstance().createIndex(post.getLong("id").toString(), EsIndexType.post.name(), JsonKit.toJson(post));
        }

        renderJson("xx", "success");
    }

    /**
     * 获取图片质量
     *
     * @param max
     * @param size
     * @return
     */
    private double getQuality(long max, long size) {
        BigDecimal b1 = new BigDecimal(String.valueOf(max));
        BigDecimal b2 = new BigDecimal(String.valueOf(size));
        double quality = b1.divide(b2, 1, 0).doubleValue();
        if (quality > 1) {
            quality = 1;
        }
        return quality;
    }

//    public static void main(String[] args) throws IOException {
//
//        File file = new File("C:\\Users\\hunsy\\Desktop\\QQ截图20170817100539.png");
//        long size = file.length();
//        System.out.println(size);
//        long max = 200 * 1024;
//        System.out.println(max);
//        BigDecimal b1 = new BigDecimal(String.valueOf(max));
//        BigDecimal b2 = new BigDecimal(String.valueOf(size));
//        double quality = b1.divide(b2, 1, 0).doubleValue();
//        System.out.println("图片质量为" + quality);
//        if (quality > 1) {
//            quality = 1;
//        }
//        System.out.println("图片质量为" + quality);
//        BufferedImage img = Thumbnails.of(file)
//                .scale(1)
//                .asBufferedImage();
//
//        int width = img.getWidth();
//        int height = img.getHeight();
//        if (height / width != 9 / 16) {
//            System.out.println("图片比例不对");
//            return;
//        }
//
//        //Banner背景图尺寸的高长比必须大于9/16，尺寸必须大于640x360
//        if (width < 640 || height < 360) {
//            System.out.println("图片太小了");
//            return;
//        }
//
//        if (width > 640 && width < 1500) {
//            width = 640;
//        } else {
//            width = 1500;
//        }
//        height = width == 640 ? 360 : 884;
//        Thumbnails.of(file)
//                .width(width)
//                .outputQuality(quality)
//                .toFile(new File("C:\\Users\\hunsy\\Desktop\\xxx.png"));
//    }


}
