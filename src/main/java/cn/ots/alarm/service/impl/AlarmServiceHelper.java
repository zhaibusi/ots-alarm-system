package cn.ots.alarm.service.impl;

import cn.ots.alarm.constants.Constants;
import cn.ots.alarm.entity.AlarmEntity;
import cn.ots.alarm.netty.NettySocketGroup;
import cn.ots.alarm.service.AlarmService;
import cn.ots.alarm.utils.ByteUtils;
import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * helper类
 *
 * @author
 * @since 2020/12/4 22:09
 */
@Component
public class AlarmServiceHelper {

    public static final Logger LOGGER = LoggerFactory.getLogger(AlarmServiceHelper.class);

    @Autowired
    private AlarmService alarmService;

    /**
     * 获取第一条告警信息同时将其他告警信息放入缓存
     *
     * @param channelId
     * @return
     */
    public byte[] getFirstAndSaveOtherToQueue(String channelId) {

        //询问前先删除队列中的告警信息
        NettySocketGroup.WAITING_SEND_MSG_MAP.remove(channelId);
        //获取当前所有告警信息
        List<byte[]> alarms = alarmService.getAlarmInfos();
        if (CollectionUtils.isEmpty(alarms)) {
            //回复告警结束
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
        //设置放入队列的时间戳  用于判断是否在询问确认后超出2秒
        LinkedList<byte[]> linkedList = new LinkedList<>();
        //除了第一条其他放入队列
        alarms.stream().skip(1).forEach(a -> linkedList.push(a));
        NettySocketGroup.WAITING_SEND_MSG_MAP.put(channelId, linkedList);
        return alarms.get(0);
    }

    /**
     * 从缓存取出一条告警信息
     *
     * @param channelId
     * @return
     */
    public byte[] getAlarmFromWaitingQueue(String channelId) {
        LinkedList<byte[]> linkedList = NettySocketGroup.WAITING_SEND_MSG_MAP.get(channelId);
        return CollectionUtils.isNotEmpty(linkedList) ? linkedList.pollLast() : ArrayUtils.EMPTY_BYTE_ARRAY;
    }

    /**
     * 构建16进制字节数组消息体
     *
     * @param entity
     * @param code
     * @return
     */
    public static byte[] buildAlarmStr(AlarmEntity entity, int code) {
        if (entity == null) {
            LOGGER.error("[AlarmServiceHelper-buildAlarmStr-entity-is-null]");
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
        String deviceType = entity.getDeviceType();
        String eventDescription = entity.getEventDescription();
        Date recoverTime = entity.getRecoverTime();
        String eventId = entity.getEventId();
        int stationId = entity.getStationId();
        String alarmNature = Constants.ALARM_NATURE_MAP.get(eventDescription);
        byte codeByte = ByteUtils.hexStringTobyteArray(ByteUtils.numToHex(code))[0];
        byte[] bytes = {};
        //日期字节数组
        byte[] timeBytes = ByteUtils.dateTobyteArr(recoverTime);
        int alarmFlag = Integer.parseInt(eventId);
        if (StringUtils.isAnyBlank(deviceType, eventDescription, alarmNature, eventId) || ArrayUtils.isEmpty(timeBytes)) {
            LOGGER.error("[AlarmServiceHelper-buildAlarmStr-param-illegal]entity:{}", JSON.toJSONString(entity));
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
        //构建返回结果
        byte[] retBytes = ArrayUtils.addAll(bytes, Constants.ALARM_DATA_BEGIN
                //默认类型
                , Constants.DEFAULT_REQ_TYPE
                //告警类型
                , ByteUtils.hexStringTobyteArray(deviceType)[0]
                //车站号
                , ByteUtils.hexStringTobyteArray(ByteUtils.numToHex(stationId))[0]
                //机架号
                , Constants.ALARM_DATA_POSITION_DEFAULT
                //子架号
                , Constants.ALARM_DATA_POSITION_DEFAULT
                //槽位
                , Constants.ALARM_DATA_POSITION_DEFAULT
                //告警标志
                , (byte) (alarmFlag >> 8 & 0xff)
                //告警编码
                , (byte) (Integer.parseInt(eventId) & 0xff)
                //告警性质
                , ByteUtils.hexStringTobyteArray(alarmNature)[0]
                //告警编号
                , codeByte
                //年高位
                , timeBytes[0]
                //年低位
                , timeBytes[1]
                //月
                , timeBytes[2]
                //日
                , timeBytes[3]
                //时
                , timeBytes[4]
                //分
                , timeBytes[5]
                //秒
                , timeBytes[6]
                //结束位
                , Constants.ALARM_DATA_END);

        return retBytes;
    }
}
