package cn.ots.alarm.snmp;

import cn.ots.alarm.netty.NettyServiceHelper;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.*;
import org.snmp4j.mp.*;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.transport.TransportListener;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
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
public class SNMPTrapListener implements CommandResponder, TransportListener
{
    public static final Logger LOGGER = LoggerFactory.getLogger(SNMPTrapListener.class);

    @Value("${uemIp}")
    private String uemIp;

    @Value("${trapPort}")
    private String trapPort;

    @Autowired
    private NettyServiceHelper nettyServiceHelper;

    @Value("${userName}")
    private String userName;

    private OID mAuthProtocol;

    private OID mPrivProtocol;

    private OctetString mPrivPassphrase;

    private OctetString mAuthPassphrase;

    private boolean mPacketDumpEnabled = true;

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final OctetString LOCAL_ENGINE_ID = new OctetString(MPv3.createLocalEngineID());

    @Override
    public <A extends Address> void processPdu(CommandResponderEvent<A> event)
    {
        int securityModel = event.getSecurityModel();
        LOGGER.info("SNMPTrapListener-processPdu:{}", securityModel);
        PDU pdu = event.getPDU();
        if (pdu != null)
        {
            LOGGER.info("SNMPTrapListener-processPdu-receive-data:{}", pdu.toString());
            pdu.getVariableBindings().forEach(varBind -> {
                //                    OID oid = varBind.getOid();
                String alarmInfo = getChinese(varBind.getVariable().toString());
                LOGGER.info("alarmInfo:{}", JSON.toJSONString(alarmInfo));
                nettyServiceHelper.alarmTrapHandler(alarmInfo);
            });
        }

    }

    /**
     * 开启trap监听
     * @throws Exception
     */
    public void listen()
        throws Exception
    {
        Address listenAddress = GenericAddress.parse("udp:" + uemIp + "/" + trapPort);
        TransportMapping<?> transport = new DefaultUdpTransportMapping((UdpAddress)listenAddress);
        SNMP4JSettings.setReportSecurityLevelStrategy(SNMP4JSettings.ReportSecurityLevelStrategy.noAuthNoPrivIfNeeded);
        CounterSupport.getInstance().addCounterListener(new DefaultCounterListener());

        ThreadPool threadPool = ThreadPool.create("OTS-SNMP-Trap", 1);

        MultiThreadedMessageDispatcher dispatcher =
            new MultiThreadedMessageDispatcher(threadPool, new SnmpCommandMessageDispatcher());
        dispatcher.addMessageProcessingModel(new MPv1());
        dispatcher.addMessageProcessingModel(new MPv2c());
        dispatcher.addMessageProcessingModel(new MPv3(LOCAL_ENGINE_ID.getValue()));
        // add all security protocols
        SecurityProtocols.getInstance().addDefaultProtocols();
        SecurityProtocols.getInstance().addPrivacyProtocol(new Priv3DES());

        Snmp snmp = new Snmp(dispatcher, transport);

        USM usm = new USM(SecurityProtocols.getInstance(), LOCAL_ENGINE_ID, 0);
        usm.setEngineDiscoveryEnabled(true);
        SecurityModels.getInstance().addSecurityModel(usm);

        snmp.setLocalEngine(LOCAL_ENGINE_ID.getValue(), 0, 0);
        snmp.getUSM()
            .addUser(new OctetString(userName),
                new UsmUser(new OctetString(userName), mAuthProtocol, mAuthPassphrase, mPrivProtocol, mPrivPassphrase));
        LOGGER.info("[listen-success]uemIp:{},trapPort:{},uemEngineId:{}", uemIp, trapPort, LOCAL_ENGINE_ID);
        snmp.addCommandResponder(this);
        snmp.listen();
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

    @Override
    public <A extends Address> void processMessage(TransportMapping<? super A> sourceTransport, A incomingAddress,
        ByteBuffer wholeMessage, TransportStateReference tmStateReference)
    {
        byte[] msg = new byte[wholeMessage.remaining()];
        wholeMessage.get(msg);
        wholeMessage.rewind();
        LOGGER.debug("Packet received from " + uemIp + " on " + sourceTransport.getListenAddress() + ":");
        LOGGER.debug(new OctetString(msg).toHexString());
    }

    public class SnmpCommandMessageDispatcher extends MessageDispatcherImpl
    {

        public SnmpCommandMessageDispatcher()
        {
            super();
        }

        @Override
        public <A extends Address> void processMessage(TransportMapping<? super A> sourceTransport, A incomingAddress,
            ByteBuffer wholeMessage, TransportStateReference tmStateReference)
        {
            if (mPacketDumpEnabled)
            {
                SNMPTrapListener.this.processMessage(sourceTransport, incomingAddress, wholeMessage, tmStateReference);
            }
            super.processMessage(sourceTransport, incomingAddress, wholeMessage, tmStateReference);
        }

        @Override
        protected <A extends Address> void sendMessage(TransportMapping<? super A> transport, A destAddress,
            byte[] message, TransportStateReference tmStateReference, long timeoutMillis, int maxRetries)
            throws IOException
        {
            super.sendMessage(transport, destAddress, message, tmStateReference, timeoutMillis, maxRetries);
            if (mPacketDumpEnabled)
            {
                SNMPTrapListener.this.processMessage(transport, destAddress, message);
            }
        }
    }

    public void processMessage(TransportMapping<?> sourceTransport, Address destAddress, byte[] message)
    {
        LOGGER.debug("Packet sent to " + destAddress + " on " + sourceTransport.getListenAddress() + ":");
        LOGGER.debug(new OctetString(message).toHexString());
    }
}