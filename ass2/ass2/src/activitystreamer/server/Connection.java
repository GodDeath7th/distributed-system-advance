package activitystreamer.server;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import activitystreamer.data.OnlineUser;
import activitystreamer.data.ServerInfo;
import activitystreamer.data.WaitUser;
import activitystreamer.util.Settings;


public class Connection extends Thread {
	private static final Logger log = LogManager.getLogger();
	private DataInputStream in;
	private DataOutputStream out;
	private BufferedReader inreader;
	private PrintWriter outwriter;
	private boolean open = false;
	private Socket socket;
	private boolean term=false;
	
	//server info
	
	private ServerInfo thisServer = null;
	// all kinds of user
	private WaitUser thisWaitUser = null;
	private OnlineUser thisOnlineUser = null;
	

	
	private int connectorType;   // 1 master 2 sub 3 slave 4 client
	private long recentActiveTime;
	
	Connection(Socket socket) throws IOException{
		in = new DataInputStream(socket.getInputStream());
	    out = new DataOutputStream(socket.getOutputStream());
	    inreader = new BufferedReader( new InputStreamReader(in));
	    outwriter = new PrintWriter(out, true);
	    this.socket = socket;
	    open = true;
	    
	    start();
	}
	
	/*
	 * returns true if the message was written, otherwise false
	 */
	public boolean writeMsg(String msg) {
		if(open){
			outwriter.println(msg);
			outwriter.flush();
			return true;	
		}
		return false;
	}
	
	public void closeCon(){
		if(open){
			log.info("closing connection "+Settings.socketAddress(socket));
			try {
				term=true;
				inreader.close();
				out.close();
			} catch (IOException e) {
				// already closed?
				log.error("received exception closing the connection "+Settings.socketAddress(socket)+": "+e);
			}
		}
	}
	
	
	public void run(){
		try {
			String data;
			while(!term && (data = inreader.readLine())!=null){
				term=Control.getInstance().process(this,data);
			}
			log.debug("connection closed to "+Settings.socketAddress(socket));
			Control.getInstance().connectionClosed(this);
			in.close();
		} catch (IOException e) {
			if(connectorType == 1) {
				Control.getInstance().dealMasterCrash(this);
			}else if(connectorType == 2) {
				Control.getInstance().dealSubMasterCrash(this);
			}else if(connectorType == 3) {
				Control.getInstance().dealSlaveServerCrash(this);
			}else if(connectorType == 4) {
				Control.getInstance().dealClientCrash(this);
			}
			log.error("connection "+Settings.socketAddress(socket)+" closed with exception: "+e);
			Control.getInstance().connectionClosed(this);
		}
		open=false;
	}
	
	public Socket getSocket() {return socket;}
	
	public boolean isOpen() {return open;}
	
	public int getConnectorType(){return connectorType;}
	public void setConnectorType(int ct){connectorType = ct;}
	
	
	public void createWaitUser(String un, String pwd, long wt) {thisWaitUser = new WaitUser(un,pwd, wt);}
	public void setWaitUser() {;}
	public WaitUser getWaitUser() {return thisWaitUser;}
	
	public void createOnlineUser(String un,String si,long lt) {thisOnlineUser = new OnlineUser(un, si, lt);}
	public void setOnlineUser() {;}
	public OnlineUser getOnlineUser() {return thisOnlineUser;}
	
	public void setRecentActiveTime(long rat) {recentActiveTime = rat;}
	public long getRecentActiveTime() {return recentActiveTime;}
	
	public void createServerInfo() {thisServer = new ServerInfo();}
	public void setServerInfo(ServerInfo newInfo) {thisServer.updateServerInfo(newInfo);}
	public ServerInfo getServerInfo() {return thisServer;}
	

}
