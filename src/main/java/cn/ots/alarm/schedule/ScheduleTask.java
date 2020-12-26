package cn.ots.alarm.schedule;

import cn.ots.alarm.constants.Constants;
import cn.ots.alarm.netty.NettyServiceHelper;
import cn.ots.alarm.netty.NettySocketGroup;
import cn.ots.alarm.utils.CommonUtils;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map.Entry;
import java.util.Set;

/**
 * 定时任务
 *
 * @author
 * @since 2020/12/5 12:19
 */
@Configuration
@EnableScheduling   //开启定时任务
public class ScheduleTask {

    @Autowired
    private NettyServiceHelper nettyServiceHelper;

    @Autowired
    private CommonUtils commonUtils;
    /**
     * 默认不开启
     */
    @Value("${heartbeat.detection.entable:false}")
    private Boolean heartbeatEntable;

    //3.8	心跳包（OTS -> 子系统，子系统->OTS） 3分钟一次
    @Scheduled(cron = "0 0/3 * * * ?")
    private void sendHeatPackect() {
        if (heartbeatEntable) {
            NettySocketGroup.ALL_CHANNEL_MAP.forEach((k, v) -> NettyServiceHelper.respHandler(Constants.HEARTBEAT_PACKET, v));
        }
    }

    //监控ots应答过期
    @Scheduled(cron = "*/1 * * * * ?")
    private void monitor() {
        Set<Entry<String, Long>> entries = NettySocketGroup.ALARM_ACK_TIME_MAP.entrySet();
        for (Entry<String, Long> entry : entries) {
            String key = entry.getKey();
            //上次发送消息时的时间戳
            Long value = entry.getValue();
            if (commonUtils.compareNow(value)) {
                //移除等待应答队列
                NettySocketGroup.ALARM_ACK_TIME_MAP.remove(key);
                //等待发送告警队列清除
                NettySocketGroup.WAITING_SEND_MSG_MAP.remove(key);
                Channel channel = NettySocketGroup.ALL_CHANNEL_MAP.get(key);
                //超过2秒没收到应答的子系统发送询问告警结束
                nettyServiceHelper.alarmInquiryEndHandler(channel);
            }
        }
    }
}
