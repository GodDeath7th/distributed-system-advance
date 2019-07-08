package activitystreamer.data;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Created by zhangxiaoming on 25/4/18.
 */


public class MessageType {
	public TryNewMaster try_new_master = null;
	public TypeDeclare type_declare = null;
	public Register register = null;
	public RegisterCopy register_copy = null;
	public RegisterSuccess register_success = null;
	public RegisterCopySuccess register_copy_success = null;
	public RegisterCopyRollback register_copy_rollback = null;
	public AlreadyRegister already_register = null;
	public AlreadyWaitRegister already_wait_register = null;
	public ServerRedirect server_redirect = null;
	public Login login = null;
	public Redirect redirect = null;
	public LoginFinish login_finish = null;
	public LoginRemove login_remove = null;
	public AlreadyLogin already_login = null;
	public AlreadyWaitLogin already_wait_login = null;
	public ServerAnnouce server_annouce = null;
	public ClientCrash client_crash = null;
	public HeartBeat heart_beat = null;
	public StillWork still_work = null;
	public Authenticate authenticate = null;
	public AuthenticateFail authenticate_fail = null;
	public AuthenticateSuccess authenticate_success = null;
	public ActivityBroadcast activity_broadcast = null;
    public SlaveServer slave_server = null;
    public RegisterUser register_user = null;
    public LiveUser live_user = null;
    public ServerClose server_close = null;
    
    public class Authenticate{
    	public String command;
    	public String secret;
    	public Authenticate(String s) {
    		command = "AUTHENTICATE";
    		secret = s;
    	}
    }
    public class Register{
    	public String command;
    	public String username;
    	public String secret;
    	public Register(String un,String s) {
    		command = "REGISTER";
    		username = un;
    		secret = s;
    	}
    }
    
	public class TryNewMaster{
		public String command;
		public TryNewMaster() {
			command = "TRY_NEW_MASTER";
		}
	}
	
	public class TypeDeclare {
	    public String command;
	    public String type;
	    public TypeDeclare(String t) {
	   		command = "TYPE_DECLARE";
	   		type = t;
	   	}
	}
	 
	public class RegisterCopy{
		public String command;
		public String username;
		public String secret;
		public RegisterCopy(String un,String s) {
			command = "REGISTER_COPY";
			username = un;
			secret = s;
		}
		
	}
	
	public class RegisterCopySuccess{
		public String command;
		public String username;
		public RegisterCopySuccess(String un) {
			command = "REGISTER_COPY_SUCCESS";
			username = un;
		}
	}
    
	public class RegisterSuccess{
		String command;
		public RegisterSuccess() {
			command = "REGISTER_SUCCESS";
		}
	}
	
	public class RegisterCopyRollback{
		public String command;
		public String username;
		public RegisterCopyRollback(String un) {
			command = "REGISTER_COPY_ROLLBACK";
			username = un;
		}
	}
	
	public class AlreadyRegister{
		public String command;
		public AlreadyRegister() {
			command = "ALREADY_REGISTER";
		}
	}
	
	public class AlreadyWaitRegister{
		public String command;
		public AlreadyWaitRegister() {
			command = "ALREADY_WAIT_REGISTER";
		}
	}
	public class Login{
		public String command;
		public String username;
		public String secret;
		public Login(String un,String s) {
			command = "LOGIN";
			username = un;
			secret = s;
		}
	}
	
	public class ServerRedirect{
		public String command;
		public String serverId;
		public String hostname;
		public String port;
		public ServerRedirect(String si,String hn,String p) {
			command = "SERVER_REDIRECT";
			serverId = si;
			hostname = hn;
			port = p;
		}
	}
	public class Redirect{
		public String command;
		public String username;
		public Redirect(String un) {
			command = "REDIRECT";
			username = un;
		}
		
	}
	
	public class LoginFinish{
		public String command;
		public String username;
		public String serverId;
		public String loginTime;
		public LoginFinish(String un,String si,String lt) {
			command = "LOGIN_FINISH";
			username = un;
			serverId = si;
			loginTime = lt;
		}
	}
	
	public class LoginRemove{
		public String command;
		public String username;
		public LoginRemove(String un) {
			command = "LOGIN_REMOVE";
			username = un;
		}
	}
	
	public class ServerAnnouce{
		public String command;
		public String serverId;
		public String load;
		public String hostname;
		public String port;
		public String currentWorkingTime;
		public ServerAnnouce(String si, String l,String hn,String p, String cwt ) {
			command = "SERVER_ANNOUCE";
			serverId = si;
			load = l;
			hostname = hn;
			port = p;
			currentWorkingTime = cwt;
		}
	}
	public class AlreadyLogin{
		public String command;
		public AlreadyLogin() {
			command = "ALREADY_LOGIN";
		}
	}
	
	public class AlreadyWaitLogin{
		public String command;
		public AlreadyWaitLogin() {
			command = "ALREADY_WAIT_LOGIN";
		}
	}
	
	public class ClientCrash{
		String command;
		String crashTime;
		public ClientCrash(String ct) {
			command = "CLIENT_CRASH";
			crashTime = ct;
		}
	}
	
	public class HeartBeat{
		public String command;
		public HeartBeat() {
			command = "HEART_BEAT";
		}
	}
   
    public class StillWork{
    	public String command;
    	public StillWork() {
    		command = "STILL_WORK";
    	}
    }
    
    public class ActivityBroadcast{
    	public String command;
    	public String sendTime;
    	public String username;
    	public Activity activity = null;
    	public class Activity{
    		public String time;
    		public String place;
    		public String description;
    		public Activity(String t,String p,String d) {
    			time = t;
    			place = p;
    			description = d;
    		}
    	}
    	public ActivityBroadcast(String st,String un,String t,String p,String d) {
    		command = "ACTIVITY_BROADCAST";
    		sendTime = st;
    		username = un;
    		activity = new Activity(t, p, d);
    	}
    
    }
    
    public class AuthenticateFail{
    	public String command;
    	public AuthenticateFail() {
    		command = "AUTHENTICATE_FAIL";
    	}
    }
    
    public class AuthenticateSuccess{
    	public String command;
    	public AuthenticateSuccess() {
    		command = "AUTHENTICATE_SUCCESS";
    	}
    }
    
    public class SlaveServer{
    	String command;
    	String serverId;
    	String hostname;
    	String port;
    	public SlaveServer(String si,String hn,String p) {
    		command = "SLAVE_SERVER";
    		serverId = si;
    		hostname = hn;
    		port = p;
    	}
    }
    
    public class RegisterUser{
    	public String command;
    	public String username;
    	public String secret;
    	public RegisterUser(String un,String s) {
    		command = "REGISTER_USER";
    		username = un;
    		secret = s;
    	}
    }
    
    public  class LiveUser{
    	public String command;
    	public String serverId;
    	public String username;
    	public String loginTime;
    	public LiveUser(String si,String un,String lt) {
    		command = "LIVE_USER";
    		serverId = si;
    		username = un;
    		loginTime = lt;
    	}
    }
    
    public class ServerClose{
    	public String command;
    	public String info;
    	public ServerClose(String i) {
    		command = "SERVRE_CLOSE";
    		info = i;
    	}
    }
    
    public String buildMessage(String cmd, String[] params) {
    	Gson gson = new Gson();
    	switch(cmd) {
    	case "type_declare":
    		type_declare = new TypeDeclare(params[0]);
    		return gson.toJson(type_declare);
    	case "activity_broadcast":
    		activity_broadcast = new ActivityBroadcast(params[0],params[1],params[2],params[3],params[4]);
    		return gson.toJson(activity_broadcast);
    	case "try_new_master":
    		try_new_master = new TryNewMaster();
    		return gson.toJson(try_new_master);
    	case "register_copy":
    		register_copy = new RegisterCopy(params[0],params[1]);
    		return gson.toJson(register_copy);
    	case "register_copy_success":
    		register_copy_success = new RegisterCopySuccess(params[0]);
    		return gson.toJson(register_copy_success);
    	case "register_copy_rollback":
    		register_copy_rollback = new RegisterCopyRollback(params[0]);
    		return gson.toJson(register_copy_rollback);
    	case "register_success":
    		register_success = new RegisterSuccess();
    		return gson.toJson(register_success);
    	case "already_register":
    		already_register = new AlreadyRegister();
    		return gson.toJson(already_register);
    	case "already_wait_register":
    		already_wait_register = new AlreadyWaitRegister();
    		return gson.toJson(already_wait_register);
    	case "server_redirect":	
    		server_redirect = new ServerRedirect(params[0],params[1],params[2]);
    		return gson.toJson(server_redirect);
    	case "redirect":
    		redirect = new Redirect(params[0]);
    		return gson.toJson(redirect);
    	case "login_finish":
    		login_finish = new LoginFinish(params[0],params[1],params[2]);
    		return gson.toJson(login_finish);
    	case "login_remove":
    		login_remove = new LoginRemove(params[0]);
    		return gson.toJson(login_remove);
    	case "already_login":
    		already_login = new AlreadyLogin();
    		return gson.toJson(already_login);
    	case "already_wait_login":
    		already_wait_login = new AlreadyWaitLogin();
    		return gson.toJson(already_wait_login);
    	case "server_annouce":
    		server_annouce = new ServerAnnouce(params[0],params[1],params[2],params[3],params[4]);
    		return gson.toJson(server_annouce);
    	case "client_crash":
    		client_crash = new ClientCrash(params[0]);
    		return gson.toJson(client_crash);
    	case "heart_beat":
    		heart_beat = new HeartBeat();
    		return gson.toJson(heart_beat);
    	case "still_work":
    		still_work = new StillWork();
    		return gson.toJson(still_work);
    	case "authenticate_fail":
    		authenticate_fail = new AuthenticateFail();
    		return gson.toJson(authenticate_fail);
    	case "authenticate_success":
    		authenticate_success = new AuthenticateSuccess();
    		return gson.toJson(authenticate_success);
    	case "slave_server":
    		slave_server = new SlaveServer(params[0],params[1],params[2]);
    		return gson.toJson(slave_server);
    	case "register_user":
    		register_user = new RegisterUser(params[0],params[1]);
    		return gson.toJson(register_user);
    	case "live_user":
    		live_user = new LiveUser(params[0],params[1],params[2]);
    		return gson.toJson(live_user);
    	case "register":
    		register = new Register(params[0],params[1]);
    		return gson.toJson(register);
    	case "login":
    		login = new Login(params[0],params[1]);
    		return gson.toJson(login);
    	case "serevr_close":
    		server_close = new ServerClose(params[0]);
    		return gson.toJson(server_close);
    	case "authenticate":
    		authenticate = new Authenticate(params[0]);
    		return gson.toJson(authenticate);
    	default:return null;		
    	}
    }
 
    public String[] paramConstructor(String para1){
        String[] newParams = new String[1];
        newParams[0] = para1;
        return newParams;
    }
    public String[] paramConstructor(String para1,String para2){
        String[] newParams = new String[2];
        newParams[0] = para1;
        newParams[1] = para2;
        return newParams;
    }
    public String[] paramConstructor(String para1,String para2,String para3){
        String[] newParams = new String[3];
        newParams[0] = para1;
        newParams[1] = para2;
        newParams[2] = para3;
        return newParams;
    }
    public String[] paramConstructor(String para1,String para2,String para3,String para4){
        String[] newParams = new String[4];
        newParams[0] = para1;
        newParams[1] = para2;
        newParams[2] = para3;
        newParams[3] = para4;
        return newParams;
    }
    public String[] paramConstructor(String para1,String para2,String para3,String para4,String para5){
        String[] newParams = new String[5];
        newParams[0] = para1;
        newParams[1] = para2;
        newParams[2] = para3;
        newParams[3] = para4;
        newParams[4] = para5;
        return newParams;
    }

}
