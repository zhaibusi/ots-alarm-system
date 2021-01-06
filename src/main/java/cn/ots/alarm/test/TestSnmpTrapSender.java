package cn.ots.alarm.test;

import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;

/**
 * 本类用于发送Trap信息
 *
 * @author gfw2306
 *
 */
public class TestSnmpTrapSender
{

    private Snmp snmp = null;

    private Address targetAddress = null;

    private TransportMapping<UdpAddress> transport = null;

    public static void main(String[] args)
    {

        TestSnmpTrapSender poc = new TestSnmpTrapSender();

        try
        {
            poc.init();
            poc.sendV3TrapNoAuthNoPriv();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public void init()
        throws IOException
    {
        //目标主机的ip地址 和 端口号
        targetAddress = GenericAddress.parse("udp:127.0.0.1/162");
        transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);
        transport.listen();
    }

    /**
     * SnmpV3 不带认证加密协议.
     * @return
     * @throws IOException
     */
    public ResponseEvent sendV3TrapNoAuthNoPriv()
        throws IOException
    {
        SNMP4JSettings.setExtensibilityEnabled(true);
        SecurityProtocols.getInstance().addDefaultProtocols();

        UserTarget target = new UserTarget();
        target.setVersion(SnmpConstants.version3);
        try
        {
            transport = new DefaultUdpTransportMapping();
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
        byte[] enginId =
            {(byte)0x80, (byte)0x00, (byte)0x00, (byte)0xA1, (byte)0x03, (byte)0x52, (byte)0x54, (byte)0x00, (byte)0x08,
                (byte)0xE9, (byte)0x0A};
        USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(enginId), 500);
        SecurityModels secModels = SecurityModels.getInstance();
        if (snmp.getUSM() == null)
        {
            secModels.addSecurityModel(usm);
        }
        target.setAddress(targetAddress);
        ScopedPDU pdu = new ScopedPDU();
        pdu.setType(PDU.NOTIFICATION);
        VariableBinding v = new VariableBinding();
        v.setOid(SnmpConstants.sysName);
        v.setVariable(new OctetString("Snmp Trap V3 Test"));
        pdu.add(v);
        snmp.setLocalEngine(enginId, 500, 1);
        return snmp.send(pdu, target);
    }

}
