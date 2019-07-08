package activitystreamer.data;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class WaitUser {
	private String username;
	private String password;
	private long waitingTime;
	
	public WaitUser(String un, String pwd, long wt) {
		username = un;
		password = pwd;
		waitingTime = wt;

	}
	
	public String getUsername() {return username;}
	public void setUsername(String un) {username = un;}
	
	public String getPassword() {return password;}
	public void setPassword(String pwd) {password = pwd;}
	
	public long getWaitingTime() {return waitingTime;}
	public void setWaitingTime(long wt) {waitingTime = wt;} 
	
}
