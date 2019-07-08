package activitystreamer.data;

import java.util.ArrayList;

public class LegacyMessage {
	private String content;
	private long sendTime;
	
	
	public LegacyMessage(String c,long st) {
		content = c;
		sendTime = st;
	
	}
	
	public String getContent() {return content;}
	public void setContent(String c) {content = c;}
	
	public long getSendTime() {return sendTime;}
	public void setSendTime(long st) {sendTime = st;}
	
	
	
}
