package activitystreamer.data;

public class UserInfo {
	public String username;
	public String secret;
	public UserInfo(String un,String s) {
		username = un;
		secret = s;
	}
	
	public String getUsername() {return username;}
	public void setUsername(String un) {username = un;}
	
	public String getSecret() {return secret;}
	public void setSecret(String s) {secret = s;}
}
