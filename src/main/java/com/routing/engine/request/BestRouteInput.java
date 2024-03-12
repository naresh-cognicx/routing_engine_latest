/**
 * 
 */
package com.routing.engine.request;

/**
 * @author
 *
 */
public class BestRouteInput {
	/*
	 * Context - channel, previous service type, language ,routingRuleId
	 * customer info - phone number, lasthandledagentid
	 */

	private String crmId;
	private String phoneNumber;
	private String serviceRequestType;
	private String customerLanguage;
	private String customerLanguageCode;
	private String LastHandledAgentID;
	private String routingRuleId;
	private String skillSet;
	private String chat_session_id;
	private String chat_status;
	private String chat_inititaed_at;
	public String getChat_session_id() {
		return chat_session_id;
	}

	public void setChat_session_id(String chat_session_id) {
		this.chat_session_id = chat_session_id;
	}

	public String getCrmId() {
		return crmId;
	}

	public void setCrmId(String crmId) {
		this.crmId = crmId;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getServiceRequestType() {
		return serviceRequestType;
	}

	public void setServiceRequestType(String serviceRequestType) {
		this.serviceRequestType = serviceRequestType;
	}

	public String getCustomerLanguage() {
		return customerLanguage;
	}

	public void setCustomerLanguage(String customerLanguage) {
		this.customerLanguage = customerLanguage;
	}

	public String getCustomerLanguageCode() {
		return customerLanguageCode;
	}

	public void setCustomerLanguageCode(String customerLanguageCode) {
		this.customerLanguageCode = customerLanguageCode;
	}

	public String getLastHandledAgentID() {
		return LastHandledAgentID;
	}

	public void setLastHandledAgentID(String lastHandledAgentID) {
		LastHandledAgentID = lastHandledAgentID;
	}

	public String getRoutingRuleId() {
		return routingRuleId;
	}

	public void setRoutingRuleId(String routingRuleId) {
		this.routingRuleId = routingRuleId;
	}

	public String getSkillSet() {
		return skillSet;
	}

	public void setSkillSet(String skillSet) {
		this.skillSet = skillSet;
	}

	public String getChat_status() {
		return chat_status;
	}

	public void setChat_status(String chat_status) {
		this.chat_status = chat_status;
	}

	public String getChat_inititaed_at() {
		return chat_inititaed_at;
	}

	public void setChat_inititaed_at(String chat_inititaed_at) {
		this.chat_inititaed_at = chat_inititaed_at;
	}
}

