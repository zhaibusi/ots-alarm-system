package cn.ots.alarm.utils;

import cn.ots.alarm.constants.Constants;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * 字节转换工具
 *
 * @author
 * @since 2020/12/4 20:13
 */
public class ByteUtils {

    /**
     * 字节转十六进制
     *
     * @param b 需要进行转换的byte字节
     * @return 转换后的Hex字符串
     */
    public static String byteToHex(byte b) {
        String hex = Integer.toHexString(b & 0xFF);
        if (hex.length() < 2) {
            hex = "0" + hex;
        }
        return StringUtils.upperCase(hex);
    }

    /**
     * 16进制表示的字符串转换为字节数组
     *
     * @param hexString 16进制表示的字符串
     * @return byte[] 字节数组
     */
    public static byte[] hexStringTobyteArray(String hexString) {
        hexString = hexString.replaceAll(" ", "");
        if (StringUtils.length(hexString) == 1) {
            hexString = "0" + hexString;
        }
        int len = hexString.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character
                    .digit(hexString.charAt(i + 1), 16));
        }
        return bytes;
    }

    /**
     * 16进制字符串转byte数组
     *
     * @param alarmArr
     * @return
     */
    public static byte[] hesStringArrTobyteArr(String[] alarmArr) {
        int length = ArrayUtils.getLength(alarmArr);
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            byte b = ByteUtils.hexStringTobyteArray(alarmArr[i])[0];
            bytes[i] = b;
        }
        return bytes;
    }

    /**
     * 数字转16进制字符串
     *
     * @param val
     * @return
     */
    public static String numToHex(Integer val) {
        return Integer.toHexString(val);
    }

    /**
     * 日期转16进制字节数组
     *
     * @param date
     * @return
     */
    public static byte[] dateTobyteArr(Date date) {
        if (date == null) {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
        String formate = DateUtils.formate(date, DateUtils.FORMATE_14);
        String[] split = StringUtils.split(formate, Constants.SEPARATOR);
        int length = ArrayUtils.getLength(split);
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = hexStringTobyteArray(split[i])[0];
        }
        return bytes;
    }


}
