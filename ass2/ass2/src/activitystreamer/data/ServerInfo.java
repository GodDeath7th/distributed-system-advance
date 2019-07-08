package activitystreamer.data;

/**
 * Created by zhangxiaoming on 26/4/18.
 */
public class ServerInfo {
    private String id;
    private int load;
    private String hostname;
    private int port;
    private long currentWorkingTime;
    public ServerInfo(){
        id = "";
        load = 0;
        hostname = "";
        port = 0;
    }
    public ServerInfo(String i,int l,String hn, int p){
        id = i;
        load = l;
        hostname = hn;
        port = p;
    }

    public String getId(){return id;}
    public void setId(String i){id = i;}
    public int getLoad(){return load;}
    public void setLoad(int l){load = l;}
    public String getHostname(){return hostname;}
    public void setHostname(String hn){hostname = hn;}
    public int getPort(){return port;}
    public void setPort(int p){port = p;}
    public long getCurrentWorkingTime() {return currentWorkingTime;}
    public void setCurrentWorkingTime(long cwt) {currentWorkingTime = cwt;}

    public void updateServerInfo(ServerInfo s) {
    	id = s.getId();
    	load = s.getLoad();
    	hostname = s.getHostname();
    	port = s.getPort();
    	currentWorkingTime = s.getCurrentWorkingTime();
    }

}
