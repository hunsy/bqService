package com.bingqiong.bq.comm.utils;

import com.bingqiong.bq.model.post.Post;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 富文本工具
 * Created by hunsy on 2017/4/11.
 */
public class RichTextUtil {

    /**
     * 解析帖子内容中的图片地址
     *
     * @return
     */
    public static void parseContent(Post post) {
        if (post.getStr("content") != null) {
            Document doc = Jsoup.parse(post.getStr("content"));
            String str = doc.text();
            if (StringUtils.isNotEmpty(str)) {
                if (str.length() < 100) {
                    post.set("intro", str);
                } else {
                    post.set("intro", str.substring(0, 100));
                }
            }
            Elements els = doc.select("img");
            StringBuilder sb = new StringBuilder();
            if (!els.isEmpty()) {
                for (int i = 0; i < els.size(); i++) {
                    Element ele = els.get(i);
                    sb.append(ele.attr("src"));
                    if (i != (els.size() - 1)) {
                        sb.append(";");
                    }
                }
                sb.substring(0, sb.length() - 1);
                post.set("thumb_url", sb.toString());
            } else {
                post.set("thumb_url", "");
            }
        }
    }


}
