package cn.ots.alarm.service.impl;

import cn.ots.alarm.entity.AlarmEntity;
import cn.ots.alarm.mapper.AlarmMapper;
import cn.ots.alarm.service.AlarmService;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 告警服务实现类
 *
 * @author
 * @since 2020/12/4 20:55
 */
@Service
public class AlarmServiceImpl implements AlarmService {

    @Autowired
    private AlarmMapper alarmMapper;

    private static final List<AlarmEntity> TEST_LIST;

    //初始化测试数据
    static {
        AlarmEntity entity1 = new AlarmEntity();
        entity1.setDeviceType("01");
        entity1.setEventDescription("一般告警");
        entity1.setRecoverTime(new Date());
        entity1.setStationId(4);
        entity1.setEventId("70");
        entity1.setStationName("test");
        AlarmEntity entity2 = new AlarmEntity();
        entity2.setDeviceType("02");
        entity2.setEventDescription("重要告警");
        entity2.setRecoverTime(new Date());
        entity2.setStationId(4);
        entity2.setEventId("80");
        entity2.setStationName("test");
        AlarmEntity entity3 = new AlarmEntity();
        entity3.setDeviceType("03");
        entity3.setEventDescription("一般告警");
        entity3.setRecoverTime(new Date());
        entity3.setStationId(4);
        entity3.setEventId("90");
        entity3.setStationName("test");
        AlarmEntity entity4 = new AlarmEntity();
        entity4.setDeviceType("04");
        entity4.setEventDescription("紧急告警");
        entity4.setRecoverTime(new Date());
        entity4.setStationId(4);
        entity4.setEventId("100");
        entity4.setStationName("test");
        AlarmEntity entity5 = new AlarmEntity();
        entity5.setDeviceType("05");
        entity5.setEventDescription("重要告警");
        entity5.setRecoverTime(new Date());
        entity5.setStationId(4);
        entity5.setEventId("110");
        entity5.setStationName("test");
        TEST_LIST = Lists.newArrayList(entity1, entity2, entity3, entity4, entity5);
    }

    @Override
    public List<byte[]> getAlarmInfos() {
        //返回告警信息

        List<byte[]> retList = Lists.newArrayList();
        for (int i = 0; i < CollectionUtils.size(TEST_LIST); i++) {
            retList.add(AlarmServiceHelper.buildAlarmStr(TEST_LIST.get(i), i + 1));
        }
        return retList;
    }

    @Override
    public byte[] getHandShakeAlarmInfo() {
        //返回握手时的第一条告警信息
        AlarmEntity entity = TEST_LIST.get(0);
        return entity == null ? ArrayUtils.EMPTY_BYTE_ARRAY : AlarmServiceHelper.buildAlarmStr(entity, 1);
    }

}
