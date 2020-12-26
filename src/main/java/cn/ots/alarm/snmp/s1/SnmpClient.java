package cn.ots.alarm.snmp.s1;

import org.apache.commons.lang3.ArrayUtils;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * Snmp采集器
 *
 * @author Fan
 */
public class SnmpClient {

    private final short targetPort = 161;
    private final short listenPort = 162;
    private Snmp snmp;
    private String targetIp;

    public SnmpClient(String targetIp) {
        this.targetIp = targetIp;
        init();
    }

    private void init() {
      /*  USM usm = new USM();
        UsmUser usmUser = new UsmUser("");
        usm.setUsers(ArrayUtils.addAll(new UsmUser[1], usmUser));
        UserTarget userTarget = new UserTarget();
        try {
            // 使用本地IP和162端口号, 选择UDP发送方式
            this.snmp = new Snmp(new DefaultUdpTransportMapping(new UdpAddress(listenPort)));
            // 添加自定义消息处理
            snmp.addCommandResponder(new TripResponser());
            snmp.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    /**
     * 发送snmp消息
     *
     * @param pdu
     * @return
     * @throws IOException
     */
    public ResponseEvent send(PDU pdu) throws IOException {
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));//设置团体名
        target.setVersion(SnmpConstants.version2c);//设置SNMP的版本
        target.setTimeout(TimeUnit.SECONDS.toMillis(2));

        try {
            target.setAddress(new UdpAddress(InetAddress.getByName(targetIp), targetPort));
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }
        return this.snmp.send(pdu, target);
    }

    /**
     * SNMP trip处理类
     *
     * @author Fan
     */
    private class TripResponser implements CommandResponder {

        @Override
        public void processPdu(CommandResponderEvent event) {
            System.out.println("reveice the trip!!!");
        }

    }
}