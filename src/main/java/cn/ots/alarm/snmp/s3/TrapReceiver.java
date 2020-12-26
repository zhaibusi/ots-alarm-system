package cn.ots.alarm.snmp.s3;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.TransportIpAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

public class TrapReceiver implements CommandResponder {
	private String username = "username";
	private String password = "password";
	private String ip = "0.0.0.0";
	private String port = "161";
	private boolean isTcp = false;
	private boolean isPP = true;
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

	public TrapReceiver() {
	}

	public void listen() {
		TransportMapping<?> transport = null;
		try {
			if (isTcp) {
				TransportIpAddress address = new TcpAddress(ip + "/" + port);
				transport = new DefaultTcpTransportMapping((TcpAddress) address);
			} else {
				TransportIpAddress address = new UdpAddress(ip + "/" + port);
				transport = new DefaultUdpTransportMapping((UdpAddress) address);
			}
		} catch (IOException e) {
			System.out.println("Caught an exception (may need to be run as sudo/root)");
		    e.printStackTrace();
			System.exit(1);
		}

		init(transport);
		System.out.println("Listening on " + ip + ":" + port + " " + (isTcp ? "TCP" : "UDP") + "\n");
	}

	public void init(TransportMapping<?> transport) {
		ThreadPool threadPool = ThreadPool.create("Trap", 10);
		MultiThreadedMessageDispatcher dispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());

		Snmp snmp = new Snmp(dispatcher, transport);

		USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
		usm.setEngineDiscoveryEnabled(true);

		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3(usm));
		SecurityModels.getInstance().addSecurityModel(usm);
		snmp.getUSM().addUser(new OctetString("MD5DES"), new UsmUser(new OctetString("MD5DES"), AuthMD5.ID,
				new OctetString(username), PrivDES.ID, new OctetString(password)));
//		snmp.getUSM().addUser(new OctetString("MD5DES"),
//				new UsmUser(new OctetString("MD5DES"), null, null, null, null));

		snmp.addCommandResponder(this);

		try {
			snmp.listen();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public synchronized void processPdu(CommandResponderEvent cmdRespEvent) {
		Date date = new Date();
		System.out.println(dateFormat.format(date) + " ... Trap Received ...");
		PDU pdu = cmdRespEvent.getPDU();
		if (pdu != null) {
			printBindings(pdu.getVariableBindings());
		}
	}
	
	private void printBindings(Vector<?> variableBindings) {
		for (Object o : variableBindings) {
			String line = o.toString();
			if (!isPP) {
				System.out.println(line);
			} else {
				int idx = line.indexOf('=');
				if (line.length() - idx > 2) {
					String val = line.substring(idx+2);
					String oid = line.substring(0, idx-1);
					System.out.println("   " + val + " ::: [" + oid + "]");
				} else if (idx > 0){
					System.out.println("   [" + line.substring(0,idx-1) + "]");
				}
			}
		}
	}
	
	public String getV3User() {
		return username;
	}

	public void setV3User(String user) {
		if (user != null) {
			username = user;
		}
	}
	
	public String getV3Pass() {
		return password;
	}

	public void setV3Pass(String pass) {
		if (pass != null) {
			password = pass;
		}
	}
	
	public String getIP() {
		return ip;
	}
	
	public void setIP(String ip) {
		if (ip != null) {
			this.ip = ip;
		}
	}
	
	public String getPort() {
		return port;
	}
	
	public void setPort(String port) {
		if (port != null) {
			this.port = port;
		}
	}
	
	public boolean isTcp() {
		return isTcp;
	}

	public void isTcp(boolean useTcp) {
		this.isTcp = useTcp;
	}
	
	public boolean isPrettyPrint() {
		return isPP;
	}
	
	public void isPrettyPrint(boolean usePrettyPrint) {
		isPP = usePrettyPrint;
	}
}
