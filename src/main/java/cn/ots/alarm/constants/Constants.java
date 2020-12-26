package cn.ots.alarm.constants;

import com.google.common.base.Splitter;

import java.util.Map;

/**
 * @author
 * @since 2020/12/4 21:24
 */
public interface Constants {

    /**
     * 分隔符
     */
    String SEPARATOR = ",";

    /**
     * 默认请求类型
     */
    byte DEFAULT_REQ_TYPE = (byte) 0x02;

    /**
     * ots 发送子系统最少字节数组
     */
    Integer MIN_OTS_REQ_ARR_LENGTH = 2;

    /**
     * 故障确认（OTS ->子系统）
     */
    byte OTS_FAILURE_ACK = (byte) 0x7E;

    /**
     * 握手命令（OTS->子系统）
     */
    byte OTS_HANDSHAKE_BEGIN = (byte) 0xAA;
    /**
     * 握手断开（OTS -> 子系统）
     */
    byte OTS_HANDSHAKE_END = (byte) 0x7A;
    /**
     * 询问告警（OTS -> 子系统）
     */
    byte OTS_INQUIRY_ALARM_BEGIN = (byte) 0xFA;

    /**
     * 握手反馈（子系统 -> OTS）
     */
    byte[] ALARM_ACK = {(byte) 0xFF, DEFAULT_REQ_TYPE};
    /**
     * 询问告警结束（子系统->OTS）
     */
    byte[] ALARM_INQUIRY_OTS_END = {(byte) 0xFA, DEFAULT_REQ_TYPE};
    /**
     * 心跳包
     */
    byte[] HEARTBEAT_PACKET = {(byte) 0x7B, DEFAULT_REQ_TYPE};

    /**
     * 告警响应首位  固定7E
     */
    byte ALARM_DATA_BEGIN = (byte) 0x7E;
    /**
     * 告警响应末位  固定10
     */
    byte ALARM_DATA_END = (byte) 0x10;

    /**
     * 告警响应告警位置默认值
     */
    byte ALARM_DATA_POSITION_DEFAULT = (byte) 0xFF;
    /**
     * 告警响应告警标志位默认值
     */
    byte ALARM_DATA_FLAG_DEFAULT = (byte) 0x80;

    /**
     * 告警性质映射
     */
    Map<String, String> ALARM_NATURE_MAP = Splitter.on(Constants.SEPARATOR).withKeyValueSeparator("=").split("紧急告警=1,重要告警=2,一般告警=3");

}
