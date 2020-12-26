package cn.ots.alarm.snmp.s1;

import org.snmp4j.PDU;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

import java.io.IOException;
import java.util.Vector;

/**
 * snmp测试
 *
 * @author Fan
 */
public class SnmpTest {

    public static void main(String[] args) {

        SnmpClient collector = new SnmpClient("127.0.0.1");
        PDU pdu = new PDU();
        pdu.setType(PDU.GET);
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.3.4.1")));

        try {
            ResponseEvent event = collector.send(pdu);
            PDU response = event.getResponse();
            if (response == null) {
                System.out.println("未能采集到设备信息");
                return;
            }
            Vector<? extends VariableBinding> bindings = response.getVariableBindings();
            for (VariableBinding var : bindings) {
                System.out.println("发送的OID是：" + var.getOid().toString());
                System.out.println("采集的值是：" + var.getVariable().toInt());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        //		while(true) {
        //			try {
        //				Thread.sleep(TimeUnit.SECONDS.toMillis(3));
        //			} catch (InterruptedException e) {
        //				e.printStackTrace();
        //			}
        //			System.out.println("over!");
        //		}

    }

}
