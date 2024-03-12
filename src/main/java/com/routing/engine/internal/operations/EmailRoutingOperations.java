package com.routing.engine.internal.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.routing.engine.redis.RedisMap;
import com.routing.engine.request.AgentDataRedis;
import com.routing.engine.request.BestRouteEmailInput;
import com.routing.engine.request.EmailRequestDetails;
import com.routing.engine.request.QueueMessage;
import com.routing.engine.util.CommonUtil;
import com.routing.engine.util.QueueEngineServices;

@Component
public class EmailRoutingOperations {

	@Autowired
	RedisMap redisMap;
	
	@Autowired
	QueueEngineServices queueEngineService;
	
	@Value("${routing.um.routing-config}")
	private  String routingConfigUrl;
	
	@Value("${routing.queue-engine.push-message}")
	private String pushMessageUrl;
	
	@Value("${routing.queue-engine.pop-message}")
	private String popMessageUrl;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EmailRoutingOperations.class);
	
	String  routedAgentMapName = "agentMap";
	
	public Boolean checkIfAgentisRouted(AgentDataRedis agent) {
		return redisMap.hasKey(routedAgentMapName,agent.getUserId());
	}
	
	public Boolean checkforOverFlow() {
		Long size = redisMap.getsize(routedAgentMapName);
		if(size >= 3) 
			return true;
		else
			return false;
	}
	
	public Boolean deleteAllValues(){
		return redisMap.delete(routedAgentMapName);
	}
	
	public void addAgentToRoutingList(AgentDataRedis agent) {
		redisMap.put(routedAgentMapName, agent.getUserId(), agent.getUserId());
	}
	
	public Integer getEmailCount(String queueName, String agentid) {
		return (Integer) redisMap.getValue(queueName, agentid);
	}
	
	
	public void pushToQueue(BestRouteEmailInput request,String tenantId) {
		try {
			String queueName = request.getCustomerLanguage()+request.getSkillSet();
			EmailRequestDetails emailData = new EmailRequestDetails();
			emailData.setEmailCrmId(CommonUtil.nullRemove(request.getCrmId()));
			emailData.setEmailId(CommonUtil.nullRemove(request.getEmailId()));
			//String chatDataStr = "{\"chatSessionId\":\""+chatdata.getChatSessionId()+"\",\"chatUserPhoneNumber\":\""+chatdata.getChatUserPhoneNumber()+"\"}" ;// convertChatToJson(chatdata);
		//	String chatDataStr = convertChatToJson(chatdata);
			//chatDataStr = StringEscapeUtils.escapeJava(chatDataStr);
			String queueFullName = queueEngineService.getQueueName("Email", "PrecisionRouting", queueName,tenantId);
			QueueMessage msg = new QueueMessage();
			msg.setQueueName(queueFullName);
			msg.setRoutingKey(queueFullName);
			//msg.setMessage(chatDataStr);
			//Integer priority = getMessagePriority(request.getChat_inititaed_at());
			Integer priority = 1;
			if((priority!=999) && (priority!=104) && (priority!=105))
			msg.setPriority(priority);
			String chatStr = queueEngineService.pushMessage(msg,emailData.getEmailCrmId(),emailData.getEmailId(),pushMessageUrl,tenantId);
//			redisQueue.pushRight(queueName, chatDataStr);
		}
		catch(Exception e) {
			LOGGER.error(request.getCrmId() +":pushToQueue: Exception: " + request.getSkillSet() + "	" + e.getMessage());
		}
		}
}


