package com.routing.engine.external.apis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.routing.engine.services.impl.RoutingEngineServiceImpl;

public class UserManagementAPI {
	
	@Value("${routing.chat.update-agent}")
	private  String updateAgentUrl;
	
	@Value("${routing.um.routing-config}")
	private  String routingConfigUrl;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementAPI.class);
	
     public static String updateAgent(String userId , String chatSessionId,String updateAgentUrl,String clientTenantId) throws UnirestException {
		Unirest.setTimeouts(0, 0);
		try {
			LOGGER.info( "UserManagementAPI updateAgent userId: " + userId + " ChatSessionId() :" + chatSessionId +  " updateAgentUrl :" +updateAgentUrl + " tenantId : " + clientTenantId);
			HttpResponse<String> response = Unirest.post(updateAgentUrl)
		//	HttpResponse<String> response = Unirest.post("https://dcc.inaipi.ae/v1/users/updateAgent")
			//HttpResponse<String> response = Unirest.post("https://dcc.demo.inaipi.ae/v1/users/updateAgent")
			  .header("Content-Type", "application/json")
			  //.header("TENANTID", "a3dc14bd-fe70-4120-8572-461b0dc866b5")
			  .header("TENANTID", clientTenantId)
			  .body("{\r\n    \"agentId\": \""+userId+"\",\r\n    \"chat_session_id\": \""+chatSessionId+"\"\r\n}")
			  .asString();
			
			LOGGER.info( "UserManagementAPI updateAgent userId: " + userId + "ChatSessionId()" + chatSessionId +  "updateAgentUrl :" +updateAgentUrl );
			LOGGER.info( "UserManagementAPI123 " +":updateAgent:" + response.getBody());
			return response.getBody();
		} catch (UnirestException e) {
//			e.printStackTrace();
			LOGGER.error("Error : "+e.getMessage());
			throw e;
		}
	}
     
     public static String getRoutingConfig(String tenantId,String routingConfigUrl ) throws UnirestException {
		Unirest.setTimeouts(0, 0);
		try {
			Unirest.setTimeouts(0, 0);
			 //.header("TenantId", "a3dc14bd-fe70-4120-8572-461b0dc866b5")
			// .header("TenantId", "0227e4cd-35cd-4c46-a077-a11981876c85") aws
			//
			LOGGER.info( "UserManagementAPI123 " +":routingConfigUrl:" +routingConfigUrl);
			HttpResponse<String> response = Unirest.post(routingConfigUrl)
			//HttpResponse<String> response = Unirest.post("https://usermgmt.inaipi.ae/usermodule/clientMaster/routingConfig/list")
	//		HttpResponse<String> response = Unirest.post("https://ui.release.inaipi.ae/usermodule/clientMaster/routingConfig/list")
			  //.header("TenantId", "a3dc14bd-fe70-4120-8572-461b0dc866b5")
			  .header("TenantId", tenantId)
			  .header("Content-Type", "application/json")
			  .body("{}")
			  .asString();				
			LOGGER.info( "UserManagementAPI123 " +":getRoutingConfig:" + response.getBody());
			return response.getBody();
		} catch (UnirestException e) {
			LOGGER.error( "UserManagementAPIException " +":getRoutingConfig:" + e.getMessage());
//			e.printStackTrace();
			throw e;
		}
	}

}
