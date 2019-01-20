package com.bingqiong.bq.comm.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 敏感词工具
 * <p>
 * Created by hunsy on 2017/8/1.
 */
public class SensetiveUtil {

    private static SensetiveUtil util;
    private Set<String> sets = new HashSet<>();

    private SensetiveUtil() {

    }

    public static SensetiveUtil getInstance() {
        if (util == null)
            util = new SensetiveUtil();
        return util;
    }

    public void remove(String text) {
        sets.remove(text);
    }

    public void addSensitiveWords(Set<String> newWords) {
        sets.addAll(newWords);
    }

    public void addSensitiveWord(String newWord) {

        sets.add(newWord);
    }

    public String fileter(String text) {
        List<String> arr = Arrays.asList(exceptions);
        for (String str : sets) {
            if (!arr.contains(str)) {
                text = text.replace(str, "***");
            }
        }
        return text;
    }


    private static final String[] exceptions = new String[]{"{", "}", "[", "]", "{\"", "\"}", "[\"", "\"]", "\"", ","};

}
