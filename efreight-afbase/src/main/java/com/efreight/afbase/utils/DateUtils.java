package com.efreight.afbase.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @program:efreight-cloud
 * @description:
 * @author:shihongkai
 * @create:2021-01-28
 **/
public class DateUtils {
    /**
     * 根据当前时间，添加或减去指定的时间量。例如，要从当前日历时间减去 5 天，可以通过调用以下方法做到这一点：
     * add(Calendar.DAY_OF_MONTH, -5)。
     * @param date 指定时间
     * @param num  为时间添加或减去的时间天数
     * @return
     */
    public static Date getBeforeOrAfterDate(Date date, int num) {
        Calendar calendar = Calendar.getInstance();//获取日历
        calendar.setTime(date);//当date的值是当前时间，则可以不用写这段代码。
        calendar.add(Calendar.DATE, num);
        Date d = calendar.getTime();//把日历转换为Date
        return d;
    }

}
