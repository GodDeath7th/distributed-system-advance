package activitystreamer.data;

import java.util.ArrayList;

public class LegacyUser {
	private String username;
	private long startTime;
	private long endTime;
	
	public LegacyUser(String un,long st,long et) {
		username = un;
		startTime = st;
		endTime = et;
	}
	
	public String getUsername() {return username;}
	public void serUsername(String un) {username = un;}
	
	public void setStartTime(long st) {startTime = st;}
	public long getStartTime() {return startTime;}
	
	public void setEndTime(long et) {endTime = et;}
	public long getEndTime() {return endTime;}
	
	
}
