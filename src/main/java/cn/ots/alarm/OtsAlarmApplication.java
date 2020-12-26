package cn.ots.alarm;

import cn.ots.alarm.netty.NettySever;
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
public class OtsAlarmApplication implements CommandLineRunner {

    @Autowired
    private NettySever nettySever;

    public static void main(String[] args) {
        SpringApplication.run(OtsAlarmApplication.class, args);
    }

    @Override
    public void run(String... args) {
        //启动netty服务
        nettySever.init();
    }
}
