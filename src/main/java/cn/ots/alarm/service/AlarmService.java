package cn.ots.alarm.service;

import java.util.List;

/**
 * 告警服务接口
 *
 * @author
 * @since 2020/12/4 20:55
 */
public interface AlarmService {

    /**
     * 获取告警信息
     *
     * @return
     */
    List<byte[]> getAlarmInfos();

    /**
     * ots握手返回的一条数据
     *
     * @return
     */
    byte[] getHandShakeAlarmInfo();

}
