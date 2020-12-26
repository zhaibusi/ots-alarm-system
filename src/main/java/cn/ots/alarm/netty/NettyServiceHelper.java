package cn.ots.alarm.netty;

import cn.ots.alarm.constants.Constants;
import cn.ots.alarm.service.AlarmService;
import cn.ots.alarm.service.impl.AlarmServiceHelper;
import cn.ots.alarm.utils.CommonUtils;
import io.netty.channel.Channel;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * netty业务处理类
 *
 * @author
 * @since 2020/12/4 23:26
 */
@Component
public class NettyServiceHelper {

    public static final Logger LOGGER = LoggerFactory.getLogger(NettyServiceHelper.class);
    @Autowired
    private AlarmServiceHelper alarmServiceHelper;
    @Autowired
    private AlarmService alarmService;

    /**
     * 询问告警（OTS -> 子系统）
     *
     * @param incoming
     */
    public void inquiryAlarmBeginHandler(Channel incoming) {

        //查询所有告警数据的第一条告警数据，其余数据放入缓存
        byte[] alarm = alarmServiceHelper.getFirstAndSaveOtherToQueue(getChannelId(incoming));
        if (ArrayUtils.isEmpty(alarm)) {
            //没有结果返回则上报告警结束
            alarmInquiryEndHandler(incoming);
        } else {
            String cId = getChannelId(incoming);
            //更新时间
            refreshAckTimeMap(cId);
            respHandler(alarm, incoming);
        }
    }

    /**
     * 故障确认（OTS ->子系统）
     *
     * @param incoming
     */
    public void failureAckHandler(Channel incoming) {

        String cId = getChannelId(incoming);
        if (NettySocketGroup.WAITING_SEND_MSG_MAP.get(cId) == null) {
            //故障确认超时上报告警结束
            LOGGER.warn("[NettyServiceHelper-failureAck-timeOut]");
            alarmInquiryEndHandler(incoming);
            return;
        }
        byte[] alarm = alarmServiceHelper.getAlarmFromWaitingQueue(getChannelId(incoming));
        if (ArrayUtils.isEmpty(alarm)) {
            //没有结果返回则上报告警结束
            alarmInquiryEndHandler(incoming);
            //询问队列已经取空则删除询问等待池的队列
            NettySocketGroup.ALARM_ACK_TIME_MAP.remove(cId);
            NettySocketGroup.WAITING_SEND_MSG_MAP.remove(cId);
        } else {
            //更细时间
            refreshAckTimeMap(cId);
            respHandler(alarm, incoming);
        }
    }

    /**
     * 握手命令（OTS->子系统）
     *
     * @param incoming
     */
    public void handShakeBeginHandler(Channel incoming) {
        //子系统->ots 确认握手确认
        alarmHandShakeAckHandler(incoming);
        //返回握手的一条告警信息
        byte[] alarm = alarmService.getHandShakeAlarmInfo();
        if (ArrayUtils.isEmpty(alarm)) {
            //没有结果返回则上报告警结束
            alarmInquiryEndHandler(incoming);
        } else {
            respHandler(alarm, incoming);
        }
    }

    /**
     * 3.2	握手反馈（子系统 -> OTS）
     */
    public void alarmHandShakeAckHandler(Channel incoming) {
        respHandler(Constants.ALARM_ACK, incoming);
    }

    /**
     * 3.4	询问告警结束（子系统->OTS）
     */
    public void alarmInquiryEndHandler(Channel incoming) {
        LOGGER.warn("[NettyServiceHelper-alarmInquiryEndHandler-noalarm]");
        respHandler(Constants.ALARM_INQUIRY_OTS_END, incoming);
    }

    /**
     * 握手断开（OTS -> 子系统）
     *
     * @param incoming
     */
    public void handShakeEndHandler(Channel incoming) {
        LOGGER.info("[NettyServiceHelper-handShakeEndHandler]cid:{}", getChannelId(incoming));
    }

    /**
     * 结果推送回client端
     *
     * @param alarm
     * @param incoming
     */
    public static synchronized void respHandler(byte[] bytes, Channel incoming) {
        if (ArrayUtils.isNotEmpty(bytes)) {
            System.out.println(ArrayUtils.toString(bytes));
            Channel channel = NettySocketGroup.ALL_CHANNEL_MAP.get(incoming.id().asLongText());
            if (ObjectUtils.isNotEmpty(channel)) {
                channel.writeAndFlush(bytes);
            }
        }

    }

    /**
     * 获取channelid
     *
     * @param incoming
     * @return
     */
    public static String getChannelId(Channel incoming) {
        if (incoming == null) {
            return StringUtils.EMPTY;
        }
        return incoming.id().asLongText();
    }

    /**
     * 用户断开链接
     *
     * @param cId
     */
    public void socketExit(String cId) {
        NettySocketGroup.ALL_CHANNEL_MAP.remove(cId);
        NettySocketGroup.HAND_CONNECT_CHANNEL_MAP.remove(cId);
        NettySocketGroup.HAND_BROKEN_CHANNEL_MAP.remove(cId);
        NettySocketGroup.ALARM_ACK_TIME_MAP.remove(cId);
        NettySocketGroup.WAITING_SEND_MSG_MAP.remove(cId);

    }

    /**
     * 更新此次应答时间
     *
     * @param channelId
     */
    public void refreshAckTimeMap(String channelId) {
        NettySocketGroup.ALARM_ACK_TIME_MAP.put(channelId, CommonUtils.now());
    }
}

