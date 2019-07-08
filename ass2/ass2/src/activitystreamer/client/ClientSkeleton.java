package activitystreamer.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import activitystreamer.data.MessageType;
import activitystreamer.util.Settings;

public class ClientSkeleton extends Thread{
	private Socket s = null; 
	private DataInputStream in = null;
    private DataOutputStream out = null;
    private BufferedReader inreader = null;
    private PrintWriter outwriter = null;
	public void startClient() throws Exception{
		start();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		 String line = br.readLine();
		 if(line!=null) {
	        String[] params = line.split("/");
	        Settings.setLocalHostname(params[0]);
	        Settings.setRemoteHostname(params[1]);
	        Settings.setLocalPort(Integer.parseInt(params[2]));
	        Settings.setRemotePort(Integer.parseInt(params[3]));
		 }
	    
		MessageType mt = new MessageType();
	    s = new Socket(Settings.getRemoteHostname(),Settings.getRemotePort());
	    
	   
		in = new DataInputStream(s.getInputStream());
	    out = new DataOutputStream(s.getOutputStream());
	    inreader = new BufferedReader( new InputStreamReader(in));
	    outwriter = new PrintWriter(out, true);
	    System.out.println("client start on "+Settings.getLocalHostname()+":"+Settings.getLocalPort());
	    System.out.println("listening "+Settings.getRemoteHostname()+":"+Settings.getRemotePort());
	    while(true) {
	    	line = br.readLine();
	    	if(line!=null) {
	    		String[] param = line.split("/");
	    		switch(param[0]){
	    		case "register":
	    			outwriter.println(mt.buildMessage("register", mt.paramConstructor(param[0],param[1])));
	    			outwriter.flush();
	    			
	    			break;
	    		case "login":
	    			outwriter.println(mt.buildMessage("login", mt.paramConstructor(param[0],param[1])));
	    			outwriter.flush();
	    			break;
	    			
	    		case "redirect":
	    			s = new Socket(param[0],Integer.parseInt(param[1]));
	    			in = new DataInputStream(s.getInputStream());
	    		    out = new DataOutputStream(s.getOutputStream());
	    		    inreader = new BufferedReader( new InputStreamReader(in));
	    		    outwriter = new PrintWriter(out, true);
	    		    
	    		    outwriter.println(mt.buildMessage("redirect", mt.paramConstructor(param[0])));
	    		    outwriter.flush();
	    		 
	    		    break;
	    		case "activity_message":
	    			outwriter.println(mt.buildMessage("activity_message", mt.paramConstructor(String.valueOf(System.currentTimeMillis()),param[0]
	    					,param[1],param[2],param[3])));
	    			outwriter.flush();		
	    			break;
	    		}
	    	}
	    }
	}
	@Override
	public void run() {
		String msg;
		try {
			while(true) {
				if(inreader!=null) {
					while((msg = inreader.readLine())!=null) {
						System.out.println(msg);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
