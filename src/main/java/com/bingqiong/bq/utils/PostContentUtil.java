package com.bingqiong.bq.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by hunsy on 2017/4/11.
 */
public class PostContentUtil {

    /**
     * 解析帖子内容中的图片地址
     *
     * @return
     */
    public static String parseImgs(String content) {
        if (content == null)
            return null;
        Document doc = Jsoup.parse(content);
        Elements els = doc.select("img");
        StringBuilder sb = new StringBuilder();
        if (!els.isEmpty()) {
            for (Element ele : els) {
                sb.append(ele.attr("src")).append(";");
            }
        }
        return sb.toString();
    }


}
