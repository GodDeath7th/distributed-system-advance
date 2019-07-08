package activitystreamer.data;

public class Legacy {
	public String username;
	public String message;
	
	public Legacy(String un, String msg) {
		username = un;
		message = msg;
	}
	
	public String getUsername() {return username;}
	public void setUsername(String un) {username = un;}
	
	public String getMessage() {return message;}
	public void setMessage(String msg) {message = msg;}
	
}
