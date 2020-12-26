package cn.ots.alarm.controller;

import cn.ots.alarm.service.AlarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * 告警信息controller
 *
 * @author
 * @since 2020/12/5 12:01
 */
@RestController
public class AlarmController {

    @Autowired
    private AlarmService alarmService;

}
