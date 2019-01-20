package com.bingqiong.bq.comm.utils;

/**
 * Created by hunsy on 2017/5/18.
 */

import com.jfinal.kit.JsonKit;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CsvParser
 * 此类参考了网上方案，在此表示感谢
 * 2013-12-10 21:43:48
 */
public class CsvParser {
    // Saved input CSV file pathname
    private String inputCsvFile;

    // Space mark , ; : etc.
    private String spaceMark = ",";


    public CsvParser(){

    }

    /**
     * Contructor
     *
     * @param inputCsvFile
     */
    public CsvParser(String inputCsvFile, String spaceMark) {
        this.inputCsvFile = inputCsvFile;
        this.spaceMark = spaceMark;
    }

    /**
     * Contructor
     *
     * @param inputCsvFile
     */
    public CsvParser(String inputCsvFile) {
        this.inputCsvFile = inputCsvFile;
        this.spaceMark = ",";
    }

    /**
     * Get parsed array from CSV file
     *
     * @return
     */
    public List<List<String>> getParsedArray() throws Exception {
        List<List<String>> retval = new ArrayList<List<String>>();

        String regExp = getRegExp();
        BufferedReader in = new BufferedReader(new FileReader(this.inputCsvFile));
        String strLine;
        String str;
        List<String> lines = FileUtils.readLines(new File(this.inputCsvFile));
        System.out.println(JsonKit.toJson(lines));
        for (String line : lines) {
            System.out.println(new String(line.getBytes("GBK"),"UTF-8"));
        }
        return retval;
    }

    /**
     * Regular Expression for CSV parse
     *
     * @return
     */
    private String getRegExp() {
        final String SPECIAL_CHAR_A = "[^\",\\n 　]";
        final String SPECIAL_CHAR_B = "[^\"" + spaceMark + "\\n]";

        StringBuffer strRegExps = new StringBuffer();
        strRegExps.append("\"((");
        strRegExps.append(SPECIAL_CHAR_A);
        strRegExps.append("*[" + spaceMark + "\\n 　])*(");
        strRegExps.append(SPECIAL_CHAR_A);
        strRegExps.append("*\"{2})*)*");
        strRegExps.append(SPECIAL_CHAR_A);
        strRegExps.append("*\"[ 　]*" + spaceMark + "[ 　]*");
        strRegExps.append("|");
        strRegExps.append(SPECIAL_CHAR_B);
        strRegExps.append("*[ 　]*" + spaceMark + "[ 　]*");
        strRegExps.append("|\"((");
        strRegExps.append(SPECIAL_CHAR_A);
        strRegExps.append("*[" + spaceMark + "\\n 　])*(");
        strRegExps.append(SPECIAL_CHAR_A);
        strRegExps.append("*\"{2})*)*");
        strRegExps.append(SPECIAL_CHAR_A);
        strRegExps.append("*\"[ 　]*");
        strRegExps.append("|");
        strRegExps.append(SPECIAL_CHAR_B);
        strRegExps.append("*[ 　]*");
        return strRegExps.toString();
    }

    /**
     * If argChar is exist in argStr
     *
     * @param argChar
     * @param argStr
     * @return
     */
    private static boolean isExisted(String argChar, String argStr) {

        boolean blnReturnValue = false;
        if ((argStr.indexOf(argChar) >= 0)
                && (argStr.indexOf(argChar) <= argStr.length())) {
            blnReturnValue = true;
        }
        return blnReturnValue;
    }

    /**
     * 导入
     *
     * @param file csv文件(路径+文件)
     * @return
     */
    public static List<String> importCsv(File file){
        List<String> dataList=new ArrayList<String>();

        BufferedReader br=null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = "";
            while ((line = br.readLine()) != null) {
                dataList.add(line);
            }
        }catch (Exception e) {
        }finally{
            if(br!=null){
                try {
                    br.close();
                    br=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return dataList;
    }

    /**
     * 导出
     *
     * @param file csv文件(路径+文件名)，csv文件不存在会自动创建
     * @param dataList 数据
     * @return
     */
    public static boolean exportCsv(File file, List<String> dataList){
        boolean isSucess=false;

        FileOutputStream out=null;
        OutputStreamWriter osw=null;
        BufferedWriter bw=null;
        try {
            out = new FileOutputStream(file);
            osw = new OutputStreamWriter(out);
            bw =new BufferedWriter(osw);
            if(dataList!=null && !dataList.isEmpty()){
                for(String data : dataList){
                    bw.append(data).append("\r");
                }
            }
            isSucess=true;
        } catch (Exception e) {
            isSucess=false;
        }finally{
            if(bw!=null){
                try {
                    bw.close();
                    bw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(osw!=null){
                try {
                    osw.close();
                    osw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(out!=null){
                try {
                    out.close();
                    out=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return isSucess;
    }

}