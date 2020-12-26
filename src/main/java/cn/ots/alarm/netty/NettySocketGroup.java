package cn.ots.alarm.netty;

import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * netty 管理连接的socket
 *
 * @author
 * @since 2020/12/4 20:19
 */
@Component
public class NettySocketGroup {
    //连接到netty的所有用户
    public static final ConcurrentHashMap<String, Channel> ALL_CHANNEL_MAP = new ConcurrentHashMap<>();

    //发送握手的池
    public static final ConcurrentHashMap<String, Channel> HAND_CONNECT_CHANNEL_MAP = new ConcurrentHashMap<>();

    //发送握手断开的池
    public static final ConcurrentHashMap<String, Channel> HAND_BROKEN_CHANNEL_MAP = new ConcurrentHashMap<>();

    //等待ots应答的时间戳（key:channelId     value:上次ots询问的时间戳（用于判断是否超时没有ots应答））
    public static final ConcurrentHashMap<String, Long> ALARM_ACK_TIME_MAP = new ConcurrentHashMap<>();

    //询问等待发送告警信息的消费队列（key:channelId     value:待ots确认后发送的告警信息）
    public static final ConcurrentHashMap<String, LinkedList<byte[]>> WAITING_SEND_MSG_MAP = new ConcurrentHashMap<>();

}
