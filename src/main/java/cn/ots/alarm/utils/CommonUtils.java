package cn.ots.alarm.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 常用工具
 *
 * @author
 * @since 2020/12/5 13:31
 */
@Component
public class CommonUtils {

    @Value("${alarm.ack.expire:2}")
    public Long expire;

    /**
     * 获取当前时间
     *
     * @return
     */
    public static Long now() {
        return System.currentTimeMillis();
    }

    /**
     * 判断是否过期 默认两秒
     *
     * @param value
     * @return
     */
    public boolean compareNow(Long value) {
        return now() - value > expire * 1000;
    }


}
