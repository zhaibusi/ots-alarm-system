package cn.ots.alarm.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author
 * @since 2020/12/7 12:11
 */
public class DateUtils {

    public static final String FORMATE_14 = "yy,yy,MM,dd,HH,mm,ss";

    public static String formate(Date date, String formate) {
        return new SimpleDateFormat(formate).format(date);
    }
}
