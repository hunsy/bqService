//package com.bingqiong.bq.comm.utils;
//
//import org.apache.lucene.analysis.TokenStream;
//import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
//import org.wltea.analyzer.cfg.Configuration;
//import org.wltea.analyzer.cfg.DefaultConfig;
//import org.wltea.analyzer.dic.Dictionary;
//import org.wltea.analyzer.lucene.IKAnalyzer;
//
//import java.io.IOException;
//import java.io.StringReader;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//
///**
// * Created by hunsy on 2017/4/12.
// */
//public class IKAnalyzerUtil {
//
//    private static IKAnalyzerUtil ikAnalyzerUtil = null;
//    private static IKAnalyzer ikAnalyzer;
//
//    private IKAnalyzerUtil() {
//        ikAnalyzer = new IKAnalyzer(true);
//    }
//
//    public static IKAnalyzerUtil getInstance() {
//        if (ikAnalyzerUtil == null) {
//            ikAnalyzerUtil = new IKAnalyzerUtil();
//        }
//        return ikAnalyzerUtil;
//    }
//
//    public List<String> analyzer(String text) throws IOException {
//        List<String> ls = new ArrayList<String>();
//        //创建分词对象
//        StringReader reader = new StringReader(text);
//        //分词
//        TokenStream ts = ikAnalyzer.tokenStream("", reader);
//        CharTermAttribute term = ts.getAttribute(CharTermAttribute.class);
//        //遍历分词数据
//        ts.reset();   // I added this
//        while (ts.incrementToken()) {
//            ls.add(term.toString());
//        }
//        ts.close();
//        reader.close();
//        return ls;
//    }
//
//    /**
//     * 启用词
//     *
//     * @param ls
//     */
//    public static void extendDic(Collection<String> ls) {
//        Dictionary dic = Dictionary.getSingleton();
//        dic.addWords(ls);
//    }
//
//    /**
//     * 停用词
//     *
//     * @param s
//     */
//    public static void stopDic(String s) {
//
//        Dictionary dic = Dictionary.getSingleton();
//        List<String> strings = new ArrayList<>();
//        strings.add(s);
//        dic.disableWords(strings);
//    }
//
//}
