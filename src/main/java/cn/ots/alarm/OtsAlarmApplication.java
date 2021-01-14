package cn.ots.alarm;

import cn.ots.alarm.netty.NettySever;
import cn.ots.alarm.snmp.SNMPTrapListener;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ots子系统告警服务
 *
 * @author
 * @since 2020/12/4 20:14
 */
@MapperScan(basePackages = {"cn.ots.alarm.mapper"}) //扫描的mapper
@SpringBootApplication
public class OtsAlarmApplication implements CommandLineRunner
{

    @Autowired
    private NettySever nettySever;

    @Autowired
    private SNMPTrapListener snmpTrapListener;

    public static void main(String[] args)
    {
        SpringApplication.run(OtsAlarmApplication.class, args);
    }

    @Override
    public void run(String... args)
        throws Exception
    {
        //启动trap监听
        snmpTrapListener.listen();
        //启动netty服务
        nettySever.init();

    }
}
