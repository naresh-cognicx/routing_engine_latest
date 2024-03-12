package com.routing.engine.request;

public class BestRouteEmailInput {

	private String crmId;
	private String phoneNumber;
	private String emailId;
	private String serviceRequestType;
	private String customerLanguage;
	private String skillSet;
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
	public String getEmailId() {
		return emailId;
	}
	public void setEmailId(String emailId) {
		this.emailId = emailId;
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
	public String getSkillSet() {
		return skillSet;
	}
	public void setSkillSet(String skillSet) {
		this.skillSet = skillSet;
	}
	
}
