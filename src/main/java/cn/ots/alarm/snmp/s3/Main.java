package cn.ots.alarm.snmp.s3;


public class Main {
	
	public static void main(String[] args) {
		TrapReceiver trapper = new TrapReceiver();
		
//		setOptions(trapper,args);
		
		trapper.listen();
	}
	
	private static void setOptions(TrapReceiver trapper, String[] args) {
	/*	CommandLineParser parser = new DefaultParser();

		Options options = new Options();
		options.addOption("i", "listen-ip", true, "The local IP that Trapper Keeper will listen on. Default [" + trapper.getIP() + "]");
		options.addOption("n", "listen-port", true, "The port that Trapper Keeper will listen on. Default [" + trapper.getPort() + "]");
		options.addOption("T", "tcp", false, "Use TCP. Default [" + trapper.isTcp() + "]");
		options.addOption("U", "udp", false, "Use UDP. Default [" + (!trapper.isTcp()) + "]");
		options.addOption("u", "v3-user", true, "The username for v3 snmp. Default [" + trapper.getIP() + "]");
		options.addOption("p", "v3-user", true, "The username for v3 snmp. Default [" + trapper.getIP() + "]");
		options.addOption("f", "full-bindings", false, "Print out the full set of bindings (not 'pretty printed')");
		options.addOption("h", "help", false, "Print the help message.");

		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			
			if (line.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("SnmpTrapperKeeper", options);
				System.exit(0);
			}
			
			trapper.setV3User(line.getOptionValue("v3-user"));
			trapper.setV3Pass(line.getOptionValue("v3-pass"));
			trapper.setIP(line.getOptionValue("listen-ip"));
			trapper.setPort(line.getOptionValue("listen-port"));
			if (line.hasOption("full-bindings")) {
				trapper.isPrettyPrint(false);
			}
			if (line.hasOption("tcp")) {
				trapper.isTcp(true);
			}
			if (line.hasOption("udp")) {
				trapper.isTcp(false);
			}
			
//			for (Option o : options.getOptions()) {
//				if (line.hasOption(o.getOpt())) {
//					System.out.println("Arg [" + o.getOpt() + "] => " + line.getOptionValue(o.getOpt()));
//				}
//			}
		} catch (ParseException exp) {
			System.out.println("Unexpected exception:" + exp.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("SnmpTrapperKeeper", options);
			System.exit(1);
		}*/
	}

}
