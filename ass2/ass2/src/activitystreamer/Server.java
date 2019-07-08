package activitystreamer;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import activitystreamer.server.Control;
import activitystreamer.util.Settings;

public class Server {
	private static final Logger log = LogManager.getLogger();
	
	private static void help(Options options){
		String header = "An ActivityStream Server for Unimelb COMP90015\n\n";
		String footer = "\ncontact aharwood@unimelb.edu.au for issues.";
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("ActivityStreamer.Server", header, options, footer, true);
		System.exit(-1);
	}
	public static void main(String[] args) throws Exception{
		
		log.info("reading command line options");
		
		Options options = new Options();
		options.addOption("lp",true,"local port number");
		options.addOption("rp",true,"remote port number");
		options.addOption("rh",true,"remote hostname");
		options.addOption("lh",true,"local hostname");
		options.addOption("a",true,"activity interval in milliseconds");
		options.addOption("s",true,"secret for the server to use");
		options.addOption("t",true,"server typr");
		options.addOption("n",true,"name of server");
		
		
		// build the parser
		CommandLineParser parser = new DefaultParser();
		
		CommandLine cmd = null;
		try {
			cmd = parser.parse( options, args);
		} catch (ParseException e1) {
			help(options);
		}
		
		if(cmd.hasOption("lp")){
			try{
				int port = Integer.parseInt(cmd.getOptionValue("lp"));
				Settings.setLocalPort(port);
			} catch (NumberFormatException e){
				log.info("-lp requires a port number, parsed: "+cmd.getOptionValue("lp"));
				help(options);
			}
		}
		
		if(cmd.hasOption("rh")){
			Settings.setRemoteHostname(cmd.getOptionValue("rh"));
		}
		
		if(cmd.hasOption("rp")){
			try{
				int port = Integer.parseInt(cmd.getOptionValue("rp"));
				Settings.setRemotePort(port);
			} catch (NumberFormatException e){
				log.error("-rp requires a port number, parsed: "+cmd.getOptionValue("rp"));
				help(options);
			}
		}
		
		if(cmd.hasOption("a")){
			try{
				int a = Integer.parseInt(cmd.getOptionValue("a"));
				Settings.setActivityInterval(a);
			} catch (NumberFormatException e){
				log.error("-a requires a number in milliseconds, parsed: "+cmd.getOptionValue("a"));
				help(options);
			}
		}
		
        if(cmd.hasOption("n")) {
        	try{
				String n = String.valueOf(cmd.getOptionValue("n"));
				Settings.setServerId(n);
			} catch (Exception e){
			
			}
        }
        
        if(cmd.hasOption("t")) {
        	try{
				int t = Integer.parseInt(cmd.getOptionValue("t"));
				Settings.setServerType(t);
			} catch (Exception e){
			
			}
        }
        
        System.out.println("please input parameters to start server:");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = br.readLine();
        if(line!=null) {
        	String[] params = line.split("/");
        	Settings.setLocalHostname(params[0]);
        	Settings.setRemoteHostname(params[1]);
        	Settings.setLocalPort(Integer.parseInt(params[2]));
        	Settings.setRemotePort(Integer.parseInt(params[3]));
        	Settings.setServerId(params[4]);
        	Settings.setServerType(Integer.parseInt(params[5]));
        }
        
        if(Settings.getServerType() == 1) {
        	Settings.setRemoteHostname(null);
        }
        System.out.println("server start on "+Settings.getLocalHostname()+":"+Settings.getLocalPort()+" ,its type is "+
        Settings.getServerType());
        
		final Control c = Control.getInstance(); 
		c.initiateConnection();
		if(Settings.getServerType() == 1 || Settings.getServerType() == 2) {
			while(true) {
				line = br.readLine();
				if(line.equals("check online user")) {
					c.showOnlineUser();					
				}
				if(line.equals("check register user")) {
					c.showRegisterUser();
				}
				if(line.equals("check legacy user")) {
					c.showLegacyUser();
				}
				if(line.equals("check server info")) {
					c.showServerInfo();
				}
				if(line.equals("check client load")) {
					c.showClientLoad();
				}
				if(line.equals("check server type")) {
					c.showThisServerType();
				}
				if(line.equals("check server socket")) {
					c.showServerSocket();
				}
			}
		}
		
		// the following shutdown hook doesn't really work, it doesn't give us enough time to
		// cleanup all of our connections before the jvm is terminated.
		/*Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {  
				c.setTerm(true);
				c.interrupt();
		    }
		 });*/
	}

}
