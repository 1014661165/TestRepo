package com.fudan.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期时间工具类
 */
public class DateUtil {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 比较两个时间字符串代表时间的先后
     * @param time1 时间1
     * @param time2 时间2
     * @return
     */
    public static int compare(String time1, String time2){
        try {
            Date date1 = dateFormat.parse(time1);
            Date date2 = dateFormat.parse(time2);
            return date1.compareTo(date2);
        }catch (ParseException e){
            e.printStackTrace();
        }
        return 0;
    }
}
