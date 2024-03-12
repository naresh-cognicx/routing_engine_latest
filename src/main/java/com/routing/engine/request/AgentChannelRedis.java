package com.routing.engine.request;

public class AgentChannelRedis {

	private String channelName;
	private String channelId;
	private String parentClientChannelId;
	
	public String getChannelName() {
		return channelName;
	}
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}
	public String getChannelId() {
		return channelId;
	}
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	public String getParentClientChannelId() {
		return parentClientChannelId;
	}
	public void setParentClientChannelId(String parentClientChannelId) {
		this.parentClientChannelId = parentClientChannelId;
	}
	
	
}
