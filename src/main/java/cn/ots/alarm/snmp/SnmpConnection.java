package cn.ots.alarm.snmp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.*;
import org.snmp4j.mp.*;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.AbstractTransportMapping;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.transport.TransportListener;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;

@Component
public class SnmpConnection implements CommandResponder, TransportListener
{
    private static SnmpConnection sInstance = new SnmpConnection();

    private static final int SNMP_DATE_FORMAT_BYTES_LEN = 11;

    private static final int NUM_DISPATCHER_THREADS = 2;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Snmp mSnmp;

    private OctetString mLocalEngineID = new OctetString(MPv3.createLocalEngineID());

    private OID mAuthProtocol;

    private OID mPrivProtocol;

    private OctetString mPrivPassphrase;

    private OctetString mAuthPassphrase;

    OctetString mSecurityName = new OctetString("MotoNorth");

    private boolean mPacketDumpEnabled = true;

    private SnmpConnection()
    {
    }

    public static SnmpConnection getInstance()
    {
        return sInstance;
    }

    public void listen()
        throws IOException
    {
        AbstractTransportMapping<? extends Address> transport;
        Address address = GenericAddress.parse("udp:0.0.0.0/162");
        if (address instanceof TcpAddress)
        {
            transport = new DefaultTcpTransportMapping((TcpAddress)address);
        }
        else
        {
            transport = new DefaultUdpTransportMapping((UdpAddress)address);
        }

        SNMP4JSettings.setReportSecurityLevelStrategy(SNMP4JSettings.ReportSecurityLevelStrategy.noAuthNoPrivIfNeeded);
        // Set the default counter listener to return proper USM and MP error
        // counters.
        CounterSupport.getInstance().addCounterListener(new DefaultCounterListener());

        ThreadPool threadPool = ThreadPool.create("DispatcherPool", NUM_DISPATCHER_THREADS);
        MessageDispatcher mtDispatcher =
            new MultiThreadedMessageDispatcher(threadPool, new SnmpCommandMessageDispatcher());

        // add message processing models
        mtDispatcher.addMessageProcessingModel(new MPv1());
        mtDispatcher.addMessageProcessingModel(new MPv2c());
        mtDispatcher.addMessageProcessingModel(new MPv3(mLocalEngineID.getValue()));

        // add all security protocols
        SecurityProtocols.getInstance().addDefaultProtocols();
        SecurityProtocols.getInstance().addPrivacyProtocol(new Priv3DES());

        mSnmp = new Snmp(mtDispatcher, transport);

        USM usm = new USM(SecurityProtocols.getInstance(), mLocalEngineID, 0);
        SecurityModels.getInstance().addSecurityModel(usm);

        mSnmp.setLocalEngine(mLocalEngineID.getValue(), 0, 0);
        mSnmp.getUSM()
            .addUser(mSecurityName,
                new UsmUser(mSecurityName, mAuthProtocol, mAuthPassphrase, mPrivProtocol, mPrivPassphrase));

        mSnmp.addCommandResponder(this);

        transport.listen();
        logger.debug("Listening on " + address);
    }

    public void close()
    {
        if (mSnmp != null)
        {
            try
            {
                mSnmp.close();
            }
            catch (IOException e)
            {
                logger.error(e.getMessage(), e);
            }
            mSnmp = null;
        }
    }

    @Override
    public <A extends Address> void processMessage(TransportMapping<? super A> sourceTransport, A incomingAddress,
        ByteBuffer wholeMessage, TransportStateReference tmStateReference)
    {
        byte[] msg = new byte[wholeMessage.remaining()];
        wholeMessage.get(msg);
        wholeMessage.rewind();
        logger.debug("Packet received from " + incomingAddress + " on " + sourceTransport.getListenAddress() + ":");
        logger.debug(new OctetString(msg).toHexString());
    }

    public void processMessage(TransportMapping<?> sourceTransport, Address destAddress, byte[] message)
    {
        logger.debug("Packet sent to " + destAddress + " on " + sourceTransport.getListenAddress() + ":");
        logger.debug(new OctetString(message).toHexString());
    }

    @Override
    public <A extends Address> void processPdu(CommandResponderEvent<A> event)
    {
        System.out.println("receive msg!!!!!!!!!!!!!");
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
                SnmpConnection.this.processMessage(sourceTransport, incomingAddress, wholeMessage, tmStateReference);
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
                SnmpConnection.this.processMessage(transport, destAddress, message);
            }
        }

    }

    public static LocalDateTime fromSnmpDateFormat(byte[] snmpDateAndTime)
    {
        if (snmpDateAndTime.length >= SNMP_DATE_FORMAT_BYTES_LEN)
        {
            int i = 0;
            int year = ((snmpDateAndTime[i] & 0xFF) << 8) | (snmpDateAndTime[i + 1] & 0xFF);
            i += 2;
            int month = snmpDateAndTime[i++] & 0xFF;
            int day = snmpDateAndTime[i++] & 0xFF;
            int hour = snmpDateAndTime[i++] & 0xFF;
            int minute = snmpDateAndTime[i++] & 0xFF;
            int second = snmpDateAndTime[i++] & 0xFF;
            int deciSecond = snmpDateAndTime[i++] & 0xFF;
            char directionfromUTC = (char)(snmpDateAndTime[i++] & 0xFF);
            int hoursfromUTC = snmpDateAndTime[i++] & 0xFF;
            int minutesfromUTC = snmpDateAndTime[i++] & 0xFF;

            return LocalDateTime.of(year, month, day, hour, minute, second);
        }

        return null;
    }
}
