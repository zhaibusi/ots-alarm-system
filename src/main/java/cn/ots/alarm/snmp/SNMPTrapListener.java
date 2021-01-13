package cn.ots.alarm.snmp;

import cn.ots.alarm.netty.NettyServiceHelper;
import cn.ots.alarm.utils.ByteUtils;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.*;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * <snmpTrap>
 * <功能详细描述>
 *
 * @author
 * @since 2021/1/7 0:04
 */
@Component
public class SNMPTrapListener implements CommandResponder
{
    public static final Logger LOGGER = LoggerFactory.getLogger(SNMPTrapListener.class);

/*    @Value("${uemEngineId}")
    private String uemEngineId;*/

    @Value("${uemIp}")
    private String uemIp;

    @Value("${trapPort}")
    private String trapPort;

    @Autowired
    private NettyServiceHelper nettyServiceHelper;

    @Value("${userName}")
    private String userName;

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    @Override
    public void processPdu(CommandResponderEvent event)
    {
        if (event.getSecurityModel() == 3)
        {
            PDU pdu = event.getPDU();
            if (pdu != null)
            {
                pdu.getVariableBindings().forEach(varBind -> {
                    //                    OID oid = varBind.getOid();
                    String alarmInfo = getChinese(varBind.getVariable().toString());
                    LOGGER.info("alarmInfo:{}", JSON.toJSONString(alarmInfo));
                    nettyServiceHelper.alarmTrapHandler(alarmInfo);
                });
            }
        }

    }

    /**
     * 开启trap监听
     * @throws Exception
     */
    public void listen()
    {
        byte[] localEngineID = MPv3.createLocalEngineID();
        try
        {
            ThreadPool threadPool = ThreadPool.create("OTS-SNMP-Trap", 1);
            MultiThreadedMessageDispatcher dispatcher =
                new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());
            Address listenAddress = GenericAddress.parse("udp:" + uemIp + "/" + trapPort);
            TransportMapping<?> transport = new DefaultUdpTransportMapping((UdpAddress)listenAddress);
            USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(localEngineID), 0);
            usm.setEngineDiscoveryEnabled(true);
            Snmp snmp = new Snmp(dispatcher, transport);
            snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
            snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
            snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3(usm));
            SecurityModels.getInstance().addSecurityModel(usm);
            snmp.getUSM()
                .addUser(new OctetString(userName),
                    new UsmUser(new OctetString(userName),
                        null,
                        new OctetString(StringUtils.EMPTY),
                        null,
                        new OctetString(StringUtils.EMPTY)));
            LOGGER.info("[listen-success]uemIp:{},trapPort:{},uemEngineId:{}", uemIp, trapPort, localEngineID);
            snmp.listen();
            snmp.addCommandResponder(this);
        }
        catch (IOException e)
        {
            LOGGER.error("listen-error!uemIp:{},trapPort:{},uemEngineId:{}", uemIp, trapPort, localEngineID, e);
        }
    }

    private String getChinese(String octetString)
    {
        try
        {
            if (octetString.contains(":"))
            {
                String[] temps = octetString.split(":");
                byte[] bs = new byte[temps.length];
                for (int i = 0; i < temps.length; i++)
                {
                    bs[i] = (byte)Integer.parseInt(temps[i], 16);
                }
                return new String(bs, CHARSET);
            }
            else
            {
                return octetString;
            }
        }
        catch (Exception e)
        {
            return octetString;
        }
    }
}