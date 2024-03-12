package com.routing.engine.request;


import java.util.HashMap;
import java.util.List;

public class AgentDataRedis {
	
	private String firstName;
	private String lastName;
	private String mobileNumber;
	private String email;
	private String userId;
	private List<String> roles;
	private List<String> groups;
	private List<AgentLanguageRedis> language;
	private List<AgentSkillSetRedis> skillSet;
	private List<AgentChannelRedis> channel;
	private String status;	
	private AgentChatDataRedis chat;
	private String apiResponseStatus;
	private Boolean isSupvervisor;
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	public List<String> getGroups() {
		return groups;
	}
	public void setGroups(List<String> groups) {
		this.groups = groups;
	}
	public List<AgentLanguageRedis> getLanguage() {
		return language;
	}
	public void setLanguage(List<AgentLanguageRedis> language) {
		this.language = language;
	}
	public List<AgentSkillSetRedis> getSkillSet() {
		return skillSet;
	}
	public void setSkillSet(List<AgentSkillSetRedis> skillSet) {
		this.skillSet = skillSet;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public AgentChatDataRedis getChat() {
		return chat;
	}
	public void setChat(AgentChatDataRedis chat) {
		this.chat = chat;
	}
	public String getApiResponseStatus() {
		return apiResponseStatus;
	}
	public void setApiResponseStatus(String apiResponseStatus) {
		this.apiResponseStatus = apiResponseStatus;
	}
	public List<AgentChannelRedis> getChannel() {
		return channel;
	}
	public void setChannel(List<AgentChannelRedis> channel) {
		this.channel = channel;
	}
	public Boolean getIsSupvervisor() {
		return isSupvervisor;
	}
	public void setIsSupvervisor(Boolean isSupvervisor) {
		this.isSupvervisor = isSupvervisor;
	}
	
}
