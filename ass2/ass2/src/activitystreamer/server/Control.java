package activitystreamer.server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import activitystreamer.data.Legacy;
import activitystreamer.data.LegacyMessage;
import activitystreamer.data.LegacyUser;
import activitystreamer.data.MessageType;
import activitystreamer.data.OnlineUser;
import activitystreamer.data.ServerInfo;
import activitystreamer.data.SocketInfo;
import activitystreamer.data.UserInfo;
import activitystreamer.util.Settings;

public class Control extends Thread {
	private static final Logger log = LogManager.getLogger();
	private Gson gson = new Gson();
	private MessageType mt = new MessageType();
	
	//master or sub master
	private ArrayList<UserInfo> registerUsers;
	private ArrayList<OnlineUser> onlineUsers;
	private ArrayList<LegacyUser> legacyUsers;
	private ArrayList<LegacyMessage> legacyMessages;
	private ArrayList<Legacy> legacyCouple;
	
	private ArrayList<Connection> waitingRegisterConfirmUsers;
	private ArrayList<Connection> waitingLoginConfirmUsers;
	
	//sub master
	private ArrayList<SocketInfo> slaveServers;

	//slave server
	private boolean isConductingNewMaster = false;
	private long waitConductTime = 0;
	//for all kinds of server;		
	private static ArrayList<Connection> connections; // connection to server/client
	private static boolean term=false;
	private static Listener listener;
	
	private int clientLoad = 0;
	
	
	protected static Control control = null;
	
	public static Control getInstance() {
		if(control==null){
			control=new Control();
		} 
		return control;
	}
	
	public Control() {
		
		// initialize the connections array
		if(Settings.getServerType() == 1 || Settings.getServerType() == 2) {
			registerUsers = new ArrayList<>();
			legacyUsers = new ArrayList<>();
			legacyMessages = new ArrayList<>();
			legacyCouple = new ArrayList<>(); 
			waitingLoginConfirmUsers = new ArrayList<>();
			waitingRegisterConfirmUsers = new ArrayList<>();
			slaveServers = new ArrayList<>();
	
		}
		
		onlineUsers = new ArrayList<>();
		
				
		connections = new ArrayList<>();
		// start a listener
		try {
			listener = new Listener();
			start();
		} catch (IOException e1) {
			log.fatal("failed to startup a listening thread: "+e1);
			System.exit(-1);
		}	
	}
	
	public Connection initiateConnection(){
		
		Connection c = null;
		if(Settings.getRemoteHostname()!=null) {
			try {
				c = outgoingConnection(new Socket(Settings.getRemoteHostname(),Settings.getRemotePort()),1);
			} catch (Exception e) {
			
				log.error("failed to make connection to "+Settings.getRemoteHostname()+":"+Settings.getRemotePort()+" :"+e);
				System.exit(-1);
			}
		}
		return c;
	}
	

	
	
		/*
		 * A new incoming connection has been established, and a reference is returned to it
		 */
	public synchronized Connection incomingConnection(Socket s) throws IOException{
			
		log.debug("incomming connection: "+Settings.socketAddress(s));
		Connection c = new Connection(s);
		connections.add(c);
		return c;			
	}
		
		/*
		 * A new outgoing connection has been established, and a reference is returned to it
		 */
	public synchronized Connection outgoingConnection(Socket s, int type) throws IOException{

		log.debug("outgoing connection: "+Settings.socketAddress(s));
		Connection c = new Connection(s);
		c.setConnectorType(type);
		connections.add(c);
		try {
			c.writeMsg(new MessageType().buildMessage("type_declare",new MessageType().paramConstructor(String.valueOf(Settings.getServerType()))));
		}catch(Exception e) {
			dealMasterCrash(c);
		}
		if(Settings.getServerType() == 3) {
			try {
				c.writeMsg(new MessageType().buildMessage("authenticate",new MessageType().paramConstructor(Settings.getSecret())));
			}catch(Exception e) {
				dealMasterCrash(c);
			}
		}
		return c;			
	}
	/*
	 * Processing incoming messages from the connection.
	 * Return true if the connection should close.
	 */
	public synchronized boolean process(Connection con,String msg){
		// judge whether msg is a json file
		if(isConductingNewMaster && con.getConnectorType() == 4) {
			try {
				con.writeMsg(mt.buildMessage("try_new_master",mt.paramConstructor("")));
			}catch(Exception e) {
				log.error("Slave server "+con.getSocket().getInetAddress()+ " crash");
				con.closeCon();
				connections.remove(con);
			}
			return false;
		}
		try{
			gson.fromJson(msg,Object.class);
			JsonObject jobj = new JsonParser().parse(msg).getAsJsonObject();
		
			switch (jobj.get("command").getAsString()){
			
			//all
			case "TYPE_DECLARE":
				if(jobj.get("type").getAsString().equals("1")) {
					con.setConnectorType(1);
					isConductingNewMaster = false; 
					for(int i=connections.size()-1; i>0; i--) {
						if(connections.get(i).getConnectorType() == 4) {
							con.writeMsg(mt.buildMessage("live_user",mt.paramConstructor(connections.get(i).getOnlineUser().getUsername(),
									connections.get(i).getOnlineUser().getServerId(),String.valueOf(connections.get(i).getOnlineUser().getLoginTime()))));
						}
					}
				}
			    else if(jobj.get("type").getAsString().equals("2")) {
					con.setConnectorType(2);
				}else if(jobj.get("type").getAsString().equals("3")) {
					con.setConnectorType(3);
				}
				break;
			//master   // master client not use dealClientCrash()
			case "REGISTER":
				boolean isRegister = false;
				boolean isProcessRegister = false;
				for(int i=registerUsers.size()-1; i>=0; i--) {
					if(registerUsers.get(i).getUsername().equals(jobj.get("username").getAsString())){
						try {						
							con.writeMsg(mt.buildMessage("already_register",mt.paramConstructor("this username have been registered")));
						}catch(Exception e) {
							con.closeCon();
							connections.remove(con);
						}
						isRegister = true;
						break;
					}
				}
				for(int i=waitingRegisterConfirmUsers.size()-1; i>=0; i--) {
					if(waitingRegisterConfirmUsers.get(i).getWaitUser().getUsername().equals(jobj.get("username").getAsString())){
					
						// same request from same users forget
						if(!waitingRegisterConfirmUsers.get(i).getSocket().equals(con.getSocket())) {
						
						
							try {						
								con.writeMsg(mt.buildMessage("already_wait_register", mt.paramConstructor("this username is been processed to register")));
							}catch(Exception e) {
								con.closeCon();
								connections.remove(con);
							}					
							
							isProcessRegister = true;
							break;
						}
					}
				}
				if(!isRegister && !isProcessRegister) {
					con.createWaitUser(jobj.get("username").getAsString(), jobj.get("secret").getAsString(),System.currentTimeMillis());
					waitingRegisterConfirmUsers.add(con);
					for(int i=connections.size()-1; i>=0; i--) {
						if(connections.get(i).getConnectorType()==2) {
							try {
								connections.get(i).writeMsg(mt.buildMessage("register_copy",mt.paramConstructor(jobj.get("username").getAsString()
									,jobj.get("secret").getAsString())));
							}catch(Exception e) {
								dealSubMasterCrash(connections.get(i));
							}
							break;
						}
					}
				}
				break;
				
			// sub master	
			case "REGISTER_COPY":
				registerUsers.add(new UserInfo(jobj.get("username").getAsString(),jobj.get("secret").getAsString()));
				try {
					con.writeMsg(mt.buildMessage("register_copy_success",mt.paramConstructor(jobj.get("username").getAsString())));
				}catch(Exception e) {
					dealMasterCrash(con);
				}
				break;
			// master	
			case "REGISTER_COPY_SUCCESS":
				boolean isInRegisterWaitList = false;
				for(int i = waitingRegisterConfirmUsers.size()-1; i>=0; i--) {
					if(waitingRegisterConfirmUsers.get(i).getWaitUser().getUsername().equals(jobj.get("username").getAsString())) {
						try {
							registerUsers.add(new UserInfo(waitingRegisterConfirmUsers.get(i).getWaitUser().getUsername(),
									waitingRegisterConfirmUsers.get(i).getWaitUser().getPassword()));
							waitingRegisterConfirmUsers.get(i).writeMsg(mt.buildMessage("register_success",mt.paramConstructor(
								"")));
							waitingRegisterConfirmUsers.get(i).closeCon();
							waitingRegisterConfirmUsers.remove(i);
							isInRegisterWaitList = true;
							break;
						}catch(Exception e) {
							log.error("Client "+waitingRegisterConfirmUsers.get(i).getSocket().getInetAddress()+" crash");
							waitingRegisterConfirmUsers.get(i).closeCon();
							waitingRegisterConfirmUsers.remove(i);
						}
						break;
					}
				}
				if(!isInRegisterWaitList) {
					// write register excess time
					for(int i = waitingRegisterConfirmUsers.size()-1;i>=0;i--) {
						if(waitingRegisterConfirmUsers.get(i).getWaitUser().getUsername().equals(jobj.get("username").getAsString())) {
							try {
								waitingRegisterConfirmUsers.get(i).writeMsg(mt.buildMessage("register_fail",mt.paramConstructor(
									"register failed")));
								waitingRegisterConfirmUsers.get(i).closeCon();
								waitingRegisterConfirmUsers.remove(i);
							}catch(Exception e) {
								log.error("Client "+waitingRegisterConfirmUsers.get(i).getSocket().getInetAddress()+" crash");
								waitingRegisterConfirmUsers.get(i).closeCon();
								waitingRegisterConfirmUsers.remove(i);
							}
							break;
						}
					}
					try {
						con.writeMsg(mt.buildMessage("register_copy_rollback",mt.paramConstructor(jobj.get("username").getAsString())));
					}catch(Exception e) {
						dealSubMasterCrash(con);
					}
				}
				break;
			// sub master
			case "REGISTER_COPY_ROLLBACK":
				for(int i=registerUsers.size()-1;i>=0;i--) {
					if(registerUsers.get(i).getUsername().equals(jobj.get("username").getAsString())){
						registerUsers.remove(i);
						break;
					}	
				}
				break;
			
			case "REGISTER_USER":
				boolean isRegisterUserExist = false;
				for(int i=registerUsers.size()-1; i>=0; i--) {
					if(registerUsers.get(i).getUsername().equals(jobj.get("username").getAsString())) {
						isRegisterUserExist = true;
						break;
					}
				}
				if(!isRegisterUserExist) {
					registerUsers.add(new UserInfo(jobj.get("username").getAsString(),jobj.get("secret").getAsString()));
				}
				break;
				
			case "SLAVE_SERVER":
				boolean isSlaveServerExist = false;
				for(int i=slaveServers.size()-1; i>=0; i--) {
					if(slaveServers.get(i).getServerId().equals(jobj.get("serverId").getAsString())){
						isSlaveServerExist = true;
						break;
					}
				}
				if(!isSlaveServerExist) {
					slaveServers.add(new SocketInfo(jobj.get("serverId").getAsString(),jobj.get("hostname").getAsString(),
							jobj.get("port").getAsInt()));
				}
				break;
			// master
			case "LOGIN":
				
				boolean isInLoginWaitList = false;
				boolean isOnline = false;
				boolean isLoginInfoCorrect = false;
		
				for(int i=waitingLoginConfirmUsers.size()-1; i>=0; i--) {
					if(waitingLoginConfirmUsers.get(i).getWaitUser().getUsername().equals(jobj.get("username").getAsString())) {
						if(!waitingLoginConfirmUsers.get(i).getSocket().equals(con.getSocket())) {
							try {
								con.writeMsg(mt.buildMessage("already_wait_login",mt.paramConstructor("")));
							}catch(Exception e) {
								log.error("Client "+con.getSocket().getInetAddress()+" crash");
								con.closeCon();
								connections.remove(con);
							}
						}
						isInLoginWaitList = true;
						break;
					}
				}
				if(isInLoginWaitList) {break;}
				
				for(int i=onlineUsers.size()-1; i>=0; i--) {
					if(onlineUsers.get(i).getUsername().equals(jobj.get("username").getAsString())) {
						try {
							con.writeMsg(mt.buildMessage("already_login",mt.paramConstructor("")));
						}catch(Exception e) {
							log.error("Client "+con.getSocket().getInetAddress()+" crash");
							con.closeCon();
							connections.remove(con);
						}
						isOnline = true;
						break;
					}
				}
	
				if(!isOnline && !isInLoginWaitList) {
					for(int i=registerUsers.size()-1; i>=0; i--) {
						if(registerUsers.get(i).getUsername().equals(jobj.get("username").getAsString()) && 
								registerUsers.get(i).getSecret().equals(jobj.get("secret").getAsString())) {
		
							con.createWaitUser(jobj.get("username").getAsString(), jobj.get("secret").getAsString(),System.currentTimeMillis());
							waitingLoginConfirmUsers.add(con);
							System.out.println(waitingLoginConfirmUsers.size());
							String[] params = balanceLoad();
							try {
								con.writeMsg(mt.buildMessage("server_redirect",mt.paramConstructor(params[0],params[1],params[2])));
							}catch(Exception e) {
								log.error("Client "+con.getSocket().getInetAddress()+" crash");
								con.closeCon();
								waitingLoginConfirmUsers.remove(con);
								connections.remove(con);
							}
							isLoginInfoCorrect =true;
							break;
							
						}
					}
				}
				if(!isLoginInfoCorrect) {
					try {
						con.writeMsg(mt.buildMessage("login_info_wrong",mt.paramConstructor("")));
					}catch(Exception e) {
						log.error("Client "+con.getSocket().getInetAddress()+" crash");
						con.closeCon();
						connections.remove(con);
					}
				}

				break;
			
			// slave
			case "REDIRECT":
				boolean isRedirect = false;
				for(int i=onlineUsers.size()-1;i>=0;i--) {
					if(onlineUsers.get(i).getUsername().equals(jobj.get("username").getAsString())) {
						isRedirect = true;
						break;
					}
				}
				
				if(isRedirect) {break;}
				
				try {
					con.writeMsg(mt.buildMessage("login_success",mt.paramConstructor("login successfully")));
					con.setConnectorType(4);
					con.createOnlineUser(jobj.get("username").getAsString(), Settings.getServerId(), System.currentTimeMillis());
				}catch(Exception e) {
					log.error("Client "+con.getSocket().getInetAddress()+" crash");
					con.closeCon();
					connections.remove(con);
				}
				for(int i=connections.size()-1; i>=0; i--) {
					if(connections.get(i).getConnectorType()==1) {
						try {
							connections.get(i).writeMsg(mt.buildMessage("login_finish",mt.paramConstructor(jobj.get("username").getAsString(),Settings.getServerId(),
									String.valueOf(System.currentTimeMillis()))));
						}catch(Exception e) {
							dealMasterCrash(connections.get(i));
						}
						
						break;
					}
				}
				clientLoad ++;
				break;
				
			case "LOGIN_FINISH":
				
				for(int i=waitingLoginConfirmUsers.size()-1; i>=0; i--) {
					if(waitingLoginConfirmUsers.get(i).getWaitUser().getUsername().equals(jobj.get("username").getAsString())) {
						waitingLoginConfirmUsers.get(i).closeCon();
						waitingLoginConfirmUsers.remove(i);
						OnlineUser newOnlineUser = new OnlineUser(jobj.get("username").getAsString(),jobj.get("serverId").getAsString(),
								jobj.get("loginTime").getAsLong());
						onlineUsers.add(newOnlineUser);					
						break;
					}
				}
				
				break;
			
			case "LOGOUT":
				for(int i=connections.size()-1; i>=0; i--) {
					if(connections.get(i).getConnectorType()==1) {
						try {
							connections.get(i).writeMsg(mt.buildMessage("login_remove",mt.paramConstructor(con.getOnlineUser().getUsername())));
						}catch(Exception e) {
							dealMasterCrash(connections.get(i));
						}
						break;
					}
				}
				con.closeCon();
				connections.remove(con);
				clientLoad --;
				break;
				
			case "LOGIN_REMOVE":
				for(int i=onlineUsers.size()-1; i>=0; i--) {
					if(onlineUsers.get(i).getUsername().equals(jobj.get("username").getAsString())){
						onlineUsers.remove(i);
						break;
					}
				}
				break;
			
			case "SERVER_ANNOUCE":
				
				ServerInfo server = new ServerInfo();
				server.setId(jobj.get("serverId").getAsString());
				server.setLoad(jobj.get("load").getAsInt());
				server.setHostname(jobj.get("hostname").getAsString());
				server.setPort(jobj.get("port").getAsInt());
				server.setCurrentWorkingTime(jobj.get("currentWorkingTime").getAsLong());
				
                if(con.getServerInfo() == null) {
                	con.createServerInfo();
                	con.setServerInfo(server);
                	slaveServers.add(new SocketInfo(server.getId(),server.getHostname(),server.getPort()));
                }else {
                	con.setServerInfo(server);
                }
                break;
            // handle error
                
			case "CLIENT_CRASH":
				for(int i=onlineUsers.size()-1; i>=0; i--) {
					if(onlineUsers.get(i).getUsername().equals(jobj.get("username").getAsString())){				
						legacyUsers.add(new LegacyUser(onlineUsers.get(i).getUsername(),onlineUsers.get(i).getLoginTime(),jobj.get("crashTime").getAsLong()));
						onlineUsers.remove(i);
						break;
					}
				}
				break;
            
			case "ONLINE_USER":
				onlineUsers.add(new OnlineUser(jobj.get("username").getAsString(),jobj.get("serverId").getAsString(),jobj.get("loginTime").getAsLong()));
				break;
			// interaction between master and sub master
			case "HEART_BEAT":
				try {
					con.writeMsg(mt.buildMessage("still_work", mt.paramConstructor("")));
				}catch(Exception e) {
					dealSubMasterCrash(con);
				}
				break;
			
			case "STILL_WORK":
				con.setRecentActiveTime(System.currentTimeMillis());
				break;
			// -------------------------------------------- //
			case "AUTHENTICATE":
				if(!jobj.get("secret").getAsString().equals(Settings.getSecret())){
					try {
						con.writeMsg(mt.buildMessage("authenticate_fail",mt.paramConstructor("") ));
						con.closeCon();
						connections.remove(con);
					}catch(Exception e) {
						log.error("Slave server "+con.getSocket().getInetAddress()+" crash");
						con.closeCon();
						connections.remove(con);
					}
					con.closeCon();
					connections.remove(con);
				}else{
					try {
						con.writeMsg(mt.buildMessage("authenticate_success",mt.paramConstructor("") ));
					}catch(Exception e) {
						log.error("Slave server "+con.getSocket().getInetAddress()+" crash");
						con.closeCon();
						connections.remove(con);
					}
	
				}
				break;
				
			case "AUTHENTICATE_FAIL":
				System.exit(-1);
				break;
				
			case "AUTHENTICATE_SUCCESS":
				log.info("success connect to master");
				break;
			
			
			// slave
			case "ACTIVITY_BROADCAST":
				if(Settings.getServerType() == 1) {
					LegacyMessage legacyMessage = new LegacyMessage(msg, jobj.get("sendTime").getAsLong());
					legacyMessages.add(legacyMessage);
					
					for(int i=connections.size()-1;i>=0;i--) {
						if(connections.get(i).getConnectorType() == 3) {
							try {
								connections.get(i).writeMsg(msg);
							}catch(Exception e) {
								// store message for those suddenly crash user
								for(int j=onlineUsers.size()-1; j>=0; j--) {
									if(onlineUsers.get(j).getServerId().equals(connections.get(i).getServerInfo().getId())) {
										legacyCouple.add(new Legacy(onlineUsers.get(j).getUsername(), msg));
									}
								}
								dealSlaveServerCrash(connections.get(i));
							}
						}
					}
				}else if(Settings.getServerType() == 3) {
					for(int i=connections.size()-1; i>=0; i--) {
						if(connections.get(i).getConnectorType() == 4) {
							try {
								connections.get(i).writeMsg(msg);
							}catch(Exception e) {
								// deal server crash
								for(int j=connections.size()-1; j>=0; j--) {
									if(connections.get(j).getConnectorType() == 1) {
										try {
											connections.get(j).writeMsg(mt.buildMessage("client_crash",mt.paramConstructor(connections.get(i).getOnlineUser().getUsername(),String.valueOf(System.currentTimeMillis()))));
											break;
										}catch(Exception e1) {
											// deal server crash
											dealClientCrash(connections.get(j));
											i = connections.size()-1;
										}
									}
								}
							}
						}
					}
				}
				
					
				break;
			
			// slave	
			case "ACTIVITY_MESSAGE":
				con.setConnectorType(4);
				if(!jobj.get("username").getAsString().equals(con.getOnlineUser().getUsername()) &&
						!jobj.get("username").getAsString().equals("anonymous")){
					try {
						con.writeMsg(mt.buildMessage("activity_fail", mt.paramConstructor("username is not logged one or anonymous")));
					}catch(Exception e) {
						dealClientCrash(con);
					}
				}
				else {
					JsonObject activity = jobj.get("activity").getAsJsonObject();
					for(int i = connections.size()-1; i>=0; i--) {
						if(connections.get(i).getConnectorType() == 1) {
							try {
								connections.get(i).writeMsg(mt.buildMessage("activity_broadcast", mt.paramConstructor(jobj.get("sendTime").getAsString(),jobj.get("username").getAsString(),
									activity.get("time").getAsString(),activity.get("place").getAsString(),activity.get("description").getAsString())));
							}catch(Exception e) {
								dealMasterCrash(connections.get(i));
							}
						}else if(connections.get(i).getConnectorType() == 4) {
							try {
								connections.get(i).writeMsg(mt.buildMessage("activity_broadcast", mt.paramConstructor(jobj.get("sendTime").getAsString(),jobj.get("username").getAsString(),
									activity.get("time").getAsString(),activity.get("place").getAsString(),activity.get("description").getAsString())));
							}catch(Exception e) {
								dealClientCrash(connections.get(i));
							}
						}
					}
				}
				break;
			
			case "LEGACY_MESSAGE":
				if(Settings.getServerType() == 3) {
					boolean isWrite = false;
					for(int i=connections.size()-1;i>=0;i--) {
						if(connections.get(i).getConnectorType() == 4 && connections.get(i).getOnlineUser().getUsername().equals(jobj.get("username").getAsString())) {
							try{
								connections.get(i).writeMsg(jobj.get("message").getAsString());
								isWrite = true;
								break;
							}catch(Exception e) {
								dealClientCrash(connections.get(i));
							}
						}
						if(isWrite) {
							try {
								con.writeMsg(msg);
							}catch(Exception e) {
								dealMasterCrash(con);
							}
						}
					}
				}
				else if(Settings.getServerType() == 3) {
					for(int i=legacyCouple.size()-1;i>=0;i--) {
						if(legacyCouple.get(i).getUsername().equals(jobj.get("username").getAsString()) && 
								legacyCouple.get(i).getMessage().equals(jobj.get("message").getAsString())) {
							legacyCouple.remove(i);
							break;
						}
					}
				}
				
			default:break;

			}

		}catch (JsonSyntaxException e){

			// not json file
			try {
				con.writeMsg(mt.buildMessage("invalid_message",mt.paramConstructor(" ")));
			}catch(Exception e1) {
				for(int i=connections.size()-1;i>=0;i--) {
					if(connections.get(i).getConnectorType() == 1) {
						try {
							connections.get(i).writeMsg(mt.buildMessage("client_crash",mt.paramConstructor(" ")));
						}catch(Exception e2) {
							dealMasterCrash(connections.get(i));
						}
					}
				}
			}

		}
		return false;
	}
	

	
	public synchronized void connectionClosed(Connection con){
		if(!term) connections.remove(con);
	}
	
	
	@Override
	public void run(){
		log.info("using activity interval of "+Settings.getActivityInterval()+" milliseconds");
		while(!term){
			try {
				Thread.sleep(Settings.getActivityInterval());
				long currentTime = System.currentTimeMillis();
				if(Settings.getServerType() == 1) {
					try {
						// keep this way to remove element
						// remove those wait users more than 1 minutes
						
						for(int i= onlineUsers.size()-1; i>=0; i--) {
							for(int j=legacyCouple.size()-1; j>=0; j--) {
								if(onlineUsers.get(i).getUsername().equals(legacyCouple.get(j).getUsername())) {
									for(int k=connections.size()-1; k>=0; k--) {
										if(connections.get(k).getServerInfo().getId().equals(onlineUsers.get(i).getServerId()) && connections.get(k).getConnectorType() ==3) {
											try {
												connections.get(k).writeMsg(mt.buildMessage("legacy_message",mt.paramConstructor(legacyCouple.get(j).getUsername(),legacyCouple.get(j).getMessage())));
											}catch(Exception e) {
												dealSlaveServerCrash(connections.get(k));
											}
											break;
										}
									}
								}
								break;
							}
						}
						for(int i=connections.size()-1;i>=0;i--) {
							if(connections.get(i).getConnectorType() == 2) {
								for(int j=slaveServers.size()-1; j>=0; j--) {
									try {
										connections.get(i).writeMsg(mt.buildMessage("slave_server",mt.paramConstructor(slaveServers.get(j).getServerId(),
											slaveServers.get(j).getAddress(),String.valueOf(slaveServers.get(j).getPort()))));
									}catch(Exception e) {
										dealSubMasterCrash(connections.get(i));
									}
								}
								for(int j=registerUsers.size()-1;j>=0;j--) {
									try {
										connections.get(i).writeMsg(mt.buildMessage("register_user",mt.paramConstructor(registerUsers.get(j).getUsername(),
												registerUsers.get(j).getSecret())));
									}catch(Exception e) {
										dealSubMasterCrash(connections.get(i));
									}
								}
								if(connections.get(i).getConnectorType() == 3) {
									try {
										connections.get(i).writeMsg(mt.buildMessage("heart_beat",mt.paramConstructor("")));
									}catch(Exception e) {
										dealSlaveServerCrash(connections.get(i));
									}
									if((currentTime - connections.get(i).getRecentActiveTime()) >= 60000) {
										dealSlaveServerCrash(connections.get(i));
									}
								}
							}
							
						}
					}catch(Exception e) {}
				}
				else if(Settings.getServerType() == 2){
					try {
						for(int i=connections.size()-1;i>=0;i--) {
							if(connections.get(i).getConnectorType() == 1) {
								try {
									connections.get(i).writeMsg(mt.buildMessage("heart_beat",mt.paramConstructor("")));
								}catch(Exception e) {
									dealMasterCrash(connections.get(i));
									// become master and connect slave
								}
								
							}
						}
			
					}catch(Exception e) {}
				}
				else if(Settings.getServerType() == 3) {
					try {
						for(int i=connections.size()-1; i>=0; i--){
							if(connections.get(i).getConnectorType() == 1){
								try {
									connections.get(i).writeMsg(mt.buildMessage("server_annouce",mt.paramConstructor(
											Settings.getServerId(),String.valueOf(clientLoad),Settings.getLocalHostname(),
											String.valueOf(Settings.getLocalPort()),String.valueOf(System.currentTimeMillis()))));
								}catch(Exception e) {
									dealMasterCrash(connections.get(i));
								}
							}
						}
						for(int i=connections.size()-1; i>=0; i--){
							if(connections.get(i).getConnectorType() == 4){
								try {
									connections.get(i).writeMsg(mt.buildMessage("heart_beat",mt.paramConstructor("")));
								}catch(Exception e) {
									dealClientCrash(connections.get(i));
								}
							}
						}
					}catch(Exception e) {
					}
					if((currentTime - waitConductTime)>= 120000 && isConductingNewMaster) {
						for(int i=connections.size()-1; i>=0; i--) {
							if(connections.get(i).getConnectorType() == 4) {
								try {
									connections.get(i).writeMsg(mt.buildMessage("server_close", mt.paramConstructor("please re-login")));
								}catch(Exception e) {}
							}
							connections.get(i).closeCon();
							connections.remove(i);
						}
						System.exit(-1);
					}
				}
			
			}

			catch (Exception/*InterruptedException */e) {
				log.info("received an interrupt, system is shutting down");
				break;
			}
		}
		if(!term){
			//log.debug("doing activity");
			term=doActivity();
		}
		log.info("closing "+connections.size()+" connections");
		// clean up
		for(Connection connection : connections){
			connection.closeCon();
		}
		listener.setTerm(true);
	}
	
	public boolean doActivity(){
		
		return false;
	}
	
	public final void setTerm(boolean t){
		term=t;
	}
	
	public final ArrayList<Connection> getConnections() {
		return connections;
	}
	public void setThisMaster() {
		Settings.setServerType(1);
		for(int i=slaveServers.size()-1;i>=0;i--) {
			try {
				outgoingConnection(new Socket(slaveServers.get(i).getAddress(),slaveServers.get(i).getPort()),3);
			}catch(Exception e) {
				log.error("failed to make connection to "+slaveServers.get(i).getAddress());
				slaveServers.remove(i);
			}
		}
		
	}
	public String[] balanceLoad() {
		String[] serverInfo = new String[3];
		
		int load = 100000;
		for(int i=connections.size()-1; i>=0; i--) {
        	if(connections.get(i).getConnectorType() == 3) {
        		if(connections.get(i).getServerInfo().getLoad() < load) {
        			serverInfo[0] = connections.get(i).getServerInfo().getId();
        			serverInfo[1] = connections.get(i).getServerInfo().getHostname();
        			serverInfo[2] = String.valueOf(connections.get(i).getServerInfo().getPort());
        			load = connections.get(i).getServerInfo().getLoad();
        		}
        	}
        }
		return serverInfo;
	}
	
	public void dealSlaveServerCrash(Connection con) {
		log.error("Slave server "+con.getSocket().getInetAddress()+ " crash");
		for(int i=onlineUsers.size()-1; i>=0; i--) {
			if(onlineUsers.get(i).getServerId().equals(con.getServerInfo().getId())) {
				legacyUsers.add(new LegacyUser(onlineUsers.get(i).getUsername(),onlineUsers.get(i).getLoginTime(),
						System.currentTimeMillis()));
				onlineUsers.remove(i);
			}
		}
		con.closeCon();
		connections.remove(con);
	}
	
	public void dealMasterCrash(Connection con) {
		log.error("Master "+con.getSocket().getInetAddress()+ " crash");
		con.closeCon();
		connections.remove(con);
		if(Settings.getServerType() == 2) {
			setThisMaster();
		}
		if(Settings.getServerType() == 3) {
			isConductingNewMaster = true;
			waitConductTime = System.currentTimeMillis();
		}
	}
	
	public void dealSubMasterCrash(Connection con) {
		log.error("Sub master "+con.getSocket().getInetAddress()+ " crash");
		con.closeCon();
		connections.remove(con);		
	}
	
	public void dealClientCrash(Connection con) {
		log.error("Sub master "+con.getSocket().getInetAddress()+ " crash");
		for(int i=connections.size()-1; i>=0; i--) {
			if(connections.get(i).getConnectorType() == 1) {
				try {
					connections.get(i).writeMsg(mt.buildMessage("client_crash",mt.paramConstructor(con.getOnlineUser().getUsername(),
						String.valueOf(System.currentTimeMillis()))));
				}catch(Exception e) {
					dealMasterCrash(connections.get(i));
				}
			}
			break;
		}
		con.closeCon();
		connections.remove(con);
		clientLoad --;
	}
	public void showServerSocket() {
		for(int i= slaveServers.size()-1;i>=0;i--) {
			System.out.println(slaveServers.get(i).getServerId()+"/"+slaveServers.get(i).getAddress()+"/"+slaveServers.get(i).getPort());
		}
	}
	
	public void showOnlineUser() {
		for(int i= onlineUsers.size()-1;i>=0;i--) {
			System.out.println(onlineUsers.get(i).getUsername()+"/"+onlineUsers.get(i).getServerId()+"/"+onlineUsers.get(i).getLoginTime());
		}
	}
	
	public void showRegisterUser() {
		for(int i= registerUsers.size()-1;i>=0;i--) {
			System.out.println(registerUsers.get(i).getUsername()+"/"+registerUsers.get(i).getSecret());
		}
	}
	
	public void showLegacyUser() {
		for(int i= legacyUsers.size()-1;i>=0;i--) {
			System.out.println(legacyUsers.get(i).getUsername()+"/"+legacyUsers.get(i).getStartTime()+"/"+legacyUsers.get(i).getEndTime());
		}
	}
	
	public void showServerInfo() {
		for(int i=connections.size()-1;i>=0;i--) {
			if(connections.get(i).getConnectorType() == 3) {
				System.out.println(connections.get(i).getServerInfo().getId()+"/"+connections.get(i).getServerInfo().getLoad()+"/"+
						connections.get(i).getServerInfo().getHostname()+"/"+connections.get(i).getServerInfo().getPort()+"/"+
						connections.get(i).getServerInfo().getCurrentWorkingTime());
			}
		}
	}
	public void showClientLoad() {
		System.out.println("there are "+clientLoad+" on this server");
	}
	public void showThisServerType() {
		System.out.println(Settings.getServerType());
	}
}
	
		