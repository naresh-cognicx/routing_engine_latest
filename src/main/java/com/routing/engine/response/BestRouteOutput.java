package com.routing.engine.response;

/**
 * @author
 *
 */
public class BestRouteOutput {
	private String userId; // by naresh
	private String agentName; //optional
	/*
	 * Expected wait time.
	 */
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
}
