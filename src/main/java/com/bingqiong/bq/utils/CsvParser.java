package com.bingqiong.bq.utils;

/**
 * Created by hunsy on 2017/5/18.
 */

import com.google.common.base.Charsets;
import com.jfinal.kit.JsonKit;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        List<List<String>> retval = new ArrayList<>();

        String regExp = getRegExp();
        BufferedReader in = new BufferedReader(new FileReader(this.inputCsvFile));
        String strLine;
        String str;
        List<String> lines = FileUtils.readLines(new File(this.inputCsvFile));
        System.out.println(JsonKit.toJson(lines));
        for (String line : lines) {
            System.out.println(new String(line.getBytes("GBK"),"UTF-8"));
        }


//        while ((strLine = in.readLine()) != null) {
//            Pattern pattern = Pattern.compile(regExp);
//            Matcher matcher = pattern.matcher(strLine);
//            List<String> listTemp = new ArrayList<>();
//            while (matcher.find()) {
//                str = matcher.group();
//                System.out.println(str);
//                str = str.trim();
//                System.out.println(str);
//                if (str.endsWith(spaceMark)) {
//                    str = str.substring(0, str.length() - 1);
//                    str = str.trim();
//                }
//
//                if (str.startsWith("\"") && str.endsWith("\"")) {
//                    str = str.substring(1, str.length() - 1);
//                    if (CsvParser.isExisted("\"\"", str)) {
//                        str = str.replaceAll("\"\"", "\"");
//                    }
//                }
//
//                if (!"".equals(str)) {
//                    listTemp.add(str);
//                }
//            }
//
//            // Add to retval
//            retval.add(listTemp);
//        }
//        in.close();

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

    /**
     * Test
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        File file = new File("C:\\Users\\hunsy\\Desktop\\xx.csv");

//        List<String> dataList=new ArrayList<String>();
//        dataList.add("1,张三,男");
//        dataList.add("2,李四,男");
//        dataList.add("3,小红,女");
//        boolean isSuccess=CsvParser.exportCsv(file, dataList);
//        System.out.println(isSuccess);

        System.out.println(MyFileUtils.getFilecharset(file));
//        CsvParser parser = new CsvParser();
        //CsvParser parser=new CsvParser("C:\\Users\\IBM_ADMIN\\Desktop\\Test CSV Files\\dummydata_not quoted_2.csv");
        //CsvParser parser=new CsvParser("C:\\Users\\IBM_ADMIN\\Desktop\\Test CSV Files\\dummydata_quoted.csv");
        //CsvParser parser=new CsvParser("C:\\Users\\IBM_ADMIN\\Desktop\\Test CSV Files\\dummydata_quoted_2.csv");

        //CsvParser parser=new CsvParser("C:\\Users\\IBM_ADMIN\\Desktop\\Test CSV Files\\dummydata_1.csv",";");
        //CsvParser parser=new CsvParser("C:\\Users\\IBM_ADMIN\\Desktop\\Test CSV Files\\dummydata_2.csv",":");

       List<String> arr = CsvParser.importCsv(file);

//
        for (String ls : arr) {
            System.out.println(ls);
        }

//            byte b[] = "中伟".getBytes();
//            System.out.print(new String (b,"UTF-8"));


//        System.out.println(new String(JsonKit.toJson(arr).getBytes("GBK"), "UTF-8"));


//        for (Object obj : arr) {
//            System.out.print("[");
//
//            List<String> ls = (List<String>) obj;
//
//            for (String item : ls) {
//                System.out.println(item + ",");
//            }
//
//            System.out.println("],");
//        }
    }
}