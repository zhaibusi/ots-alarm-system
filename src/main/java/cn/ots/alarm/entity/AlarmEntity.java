package cn.ots.alarm.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 告警实体类
 *
 * @author
 * @since 2020/12/7 15:50
 */
public class AlarmEntity implements Serializable {
    private static final long serialVersionUID = 2642276213723264084L;
    /**
     * 车站号
     */
    private int stationId;
    /**
     * 地铁站名称
     */
    private String stationName;
    /**
     * 告警类型
     */
    private String deviceType;

    /**
     * 告警代码
     */
    private String eventId;
    /**
     * 告警性质（返回的是中文名称。和编码对应起来）
     */
    private String eventDescription;
    /**
     * 发生时间。年月日时分秒
     */
    private Date recoverTime;

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public Date getRecoverTime() {
        return recoverTime;
    }

    public void setRecoverTime(Date recoverTime) {
        this.recoverTime = recoverTime;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
