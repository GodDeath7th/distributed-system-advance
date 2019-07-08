package activitystreamer.data;

public class OnlineUser {
	private String username;
	private String serverId;
	private long loginTime;
	
	public OnlineUser(String un,String si,long lt) {
		username = un;
		serverId = si;
		loginTime = lt;
	
	}
	
	public String getUsername() {return username;}
	public void setUsername(String un) {username = un;}
	
	public String getServerId() {return serverId;}
	public void setServerId(String si) {serverId = si;}
	
	public long getLoginTime() {return loginTime;}
	public void setLoginTime(long lt) {loginTime = lt;}
	

}
