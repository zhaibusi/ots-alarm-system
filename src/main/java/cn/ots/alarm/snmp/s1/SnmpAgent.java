package cn.ots.alarm.snmp.s1;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.MessageException;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.mp.StateReference;
import org.snmp4j.mp.StatusInformation;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

/**
 * Snmp代理类
 *
 * @author Fan
 */
public class SnmpAgent {

    private final short targetPort = 162;// 发送端口号
    private final short listenPort = 161;// 本地监听端口号
    private Snmp snmp;

    public SnmpAgent() {
        init();
    }

    /**
     * 初始化snmp
     */
    private void init() {
        UdpAddress udpAddress = null;
        try {
            // 使用本地IP和161端口号
            udpAddress = new UdpAddress(InetAddress.getLocalHost(), listenPort);
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }
        try {
            // 选择UDP发送方式
            this.snmp = new Snmp(new DefaultUdpTransportMapping(udpAddress));
            // 设置snmp的消息处理模型 MPv2c代表SNMPv2c（基于社区的SNMPv2）
            snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
            // 添加自定义消息处理
            snmp.addCommandResponder(new ReceiveResponder());
            snmp.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 多线程接收消息
     */
    private void init2() {
        ThreadPool pool = ThreadPool.create("snmpReceiver", 1);
        MultiThreadedMessageDispatcher dispatcher = new MultiThreadedMessageDispatcher(pool,
                new MessageDispatcherImpl());
        try {
            DefaultUdpTransportMapping transportMapping = new DefaultUdpTransportMapping(
                    new UdpAddress(InetAddress.getLocalHost(), listenPort));
            this.snmp = new Snmp(dispatcher, transportMapping);
            snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
            this.snmp.listen();
            this.snmp.addCommandResponder(new ReceiveResponder());

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 自定义消息处理类
     *
     * @author Fan
     */
    private class ReceiveResponder implements CommandResponder {

        @Override
        public void processPdu(CommandResponderEvent event) {
            System.out.println("收到对方消息：" + event.toString());
            PDU pdu = event.getPDU();
            if (pdu == null) {
                return;
            }
            if (pdu.getType() == PDU.GET) {
                doGetResponse((Vector<? extends VariableBinding>) pdu.getVariableBindings());
            }

            pdu.setType(PDU.RESPONSE);
            StatusInformation statusInfo = new StatusInformation();
            StateReference reference = event.getStateReference();
            try {
                // 响应snmp消息
                event.getMessageDispatcher().returnResponsePdu(event.getMessageProcessingModel(),
                        event.getSecurityModel(), event.getSecurityName(), event.getSecurityLevel(), pdu,
                        event.getMaxSizeResponsePDU(), reference, statusInfo);
            } catch (MessageException e) {
                e.printStackTrace();
            }
        }

        /**
         * 对接收的消息设置返回值
         *
         * @param bindings
         */
        private void doGetResponse(Vector<? extends VariableBinding> bindings) {
            for (VariableBinding varBin : bindings) {
                System.out.println("接收到的OID是：" + varBin.getOid().toString());
                varBin.setVariable(new Integer32(1));
                System.out.println("给客户端回复的值是：" + varBin.getVariable().toInt());
            }
        }

    }

    public ResponseEvent send(PDU pdu) throws IOException {
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setVersion(SnmpConstants.version2c);
        target.setTimeout(TimeUnit.SECONDS.toMillis(2));
        try {
            target.setAddress(new UdpAddress(InetAddress.getLocalHost(), targetPort));
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }
        return this.snmp.send(pdu, target);
    }

    private static PDU createTrip() {
        PDU pdu = new PDU();
        pdu.setType(PDU.TRAP);
        VariableBinding vb = new VariableBinding(new OID("1.3.6.1.2.1.3.4.1.1"));
        vb.setVariable(new Integer32(34));
        pdu.add(vb);
        return pdu;
    }

    public static void main(String[] args) throws InterruptedException {
        SnmpAgent agent = new SnmpAgent();
        System.out.println("snmp-agent started...");

        Thread.sleep(5000);

        try {
            // trip测试
            agent.send(createTrip());

            System.out.println("send trip success-------");
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * 让主线程一直处理等待中，因为DefaultUdpTransportMapping中监听本地(161端口)的线程为守护线程，
         * 一旦主线程运行完毕，守护线程也随即销毁 如果采用多线程方式启动(如调用 init2方法)，则不需要主线程等待，但会额外开启新的线程。所以，视情况而定
         */
        synchronized (agent) {
            agent.wait();
        }

    }

}