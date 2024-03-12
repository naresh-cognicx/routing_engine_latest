package com.routing.engine.request;


public class AgentChatDataRedis {
	
	private String active_chat_count;
	private String last_chat_time;
	private String last_chat_end_time;
	private String total_ongoing;
	private String total_completed;
	private String queued_count;
	public String getActive_chat_count() {
		return active_chat_count;
	}
	public void setActive_chat_count(String active_chat_count) {
		this.active_chat_count = active_chat_count;
	}
	public String getLast_chat_time() {
		return last_chat_time;
	}
	public void setLast_chat_time(String last_chat_time) {
		this.last_chat_time = last_chat_time;
	}
	public String getLast_chat_end_time() {
		return last_chat_end_time;
	}
	public void setLast_chat_end_time(String last_chat_end_time) {
		this.last_chat_end_time = last_chat_end_time;
	}
	public String getTotal_ongoing() {
		return total_ongoing;
	}
	public void setTotal_ongoing(String total_ongoing) {
		this.total_ongoing = total_ongoing;
	}
	public String getTotal_completed() {
		return total_completed;
	}
	public void setTotal_completed(String total_completed) {
		this.total_completed = total_completed;
	}
	public String getQueued_count() {
		return queued_count;
	}
	public void setQueued_count(String queued_count) {
		this.queued_count = queued_count;
	}
	
}
