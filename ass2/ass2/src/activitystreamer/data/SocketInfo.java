package activitystreamer.data;

public class SocketInfo {
	private String serverId;
	private String hostname;
	private int port;
	
	public SocketInfo(String si,String hn,int p) {
		serverId = si;
		hostname = hn;
		port = p;
	}
	
	public String getServerId() {return serverId;}
	public void setServerId(String si) {serverId = si;}
	
	public String getAddress() {return hostname;}
	public void setAddress(String hn) {hostname = hn;}
	
	public int getPort() {return port;}
	public void setPort(int p) {port = p;}
}
