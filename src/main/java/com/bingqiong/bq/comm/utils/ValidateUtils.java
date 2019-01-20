package com.bingqiong.bq.comm.utils;

import com.bingqiong.bq.comm.constants.ErrorCode;
import com.bingqiong.bq.comm.exception.BizException;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by hunsy on 2017/6/21.
 */
public class ValidateUtils {


    /**
     * 验证字符长度
     * true符合长度要求
     * false不符合长度要求
     *
     * @return
     */
    public static boolean validateStrLen(String str, int min, int max) {

        int len = str.length();

        if (len > max || len < min) {
            return false;
        }
        return true;
    }


    /**
     * 验证身份证正则
     *
     * @param card
     * @return
     */
    public static void validateCardInner(String card) throws BizException {

        if (StringUtils.isEmpty(card)) {

            throw new BizException(ErrorCode.MISSING_PARM);
        }

        String reg15 = "^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$";
        String reg19 = "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X)$";
        if (card.length() != 15 && card.length() != 18) {
            throw new BizException(ErrorCode.IDCARD_ILLEGLE);
        }
        boolean flag = false;
        if (card.length() == 15) {
            flag = card.matches(reg15);
        }

        if (card.length() == 18) {
            flag = card.toUpperCase().matches(reg19);
        }
        if (!flag) {
            throw new BizException(ErrorCode.IDCARD_ILLEGLE);
        }
    }

    /**
     * 验证国内姓名
     */
    public static void validateNameInner(String name) throws BizException {

        if (StringUtils.isEmpty(name)) {

            throw new BizException(ErrorCode.MISSING_PARM);
        }

        String nameReg = "^[\u4E00-\u9FFF]+$";

        if (name != null && name.length() > 32) {
            throw new BizException(ErrorCode.NAME_LIMIT);
        }

        if (!name.matches(nameReg)) {
            throw new BizException(ErrorCode.NAME_ILLEGLE);
        }
    }

}
