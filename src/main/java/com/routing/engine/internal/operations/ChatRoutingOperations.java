package com.routing.engine.internal.operations;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.routing.engine.external.apis.UserManagementAPI;
import com.routing.engine.redis.RedisMap;
import com.routing.engine.redis.RedisQueue;
import com.routing.engine.redis.RedisString;
import com.routing.engine.request.AgentDataRedis;
import com.routing.engine.request.AgentLanguageRedis;
import com.routing.engine.request.AgentSkillSetRedis;
import com.routing.engine.request.BestRouteInput;
import com.routing.engine.request.ChatRequestDetails;
import com.routing.engine.request.QueueMessage;
import com.routing.engine.util.CommonUtil;
import com.routing.engine.util.QueueEngineServices;

@Component
public class ChatRoutingOperations {

	@Autowired
	RedisQueue redisQueue;
	
	@Autowired
	RedisString  redisString;
	
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

	@Value("${routing.queue-engine.check-api}")
	private String checkApiUrl;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ChatRoutingOperations.class);
	
	/*
	 * public String getChatDataFromQueue(String queueName){
	 * 
	 * Long queueSize = redisQueue.getQueueSize(queueName+"Priority"); String
	 * chatStr = redisQueue.popLeft(queueName+"Priority"); if(queueSize == 0) {
	 * queueSize = redisQueue.getQueueSize(queueName); if (queueSize== 0) { return
	 * ""; } else chatStr = redisQueue.popLeft(queueName); } return chatStr; }
	 */
	
	public String getChatDataFromQueue(String queueName,String tenantId){
		
		String queueFullName = queueEngineService.getQueueName("Chat", "PrecisionRouting", queueName,tenantId);
		String chatStr = queueEngineService.popMessage(queueFullName,popMessageUrl,tenantId);
		return chatStr;
	}

	public String checkTheQueueIsPresent(String queueName, String tenantId){
		String queueFullName = queueEngineService.getQueueName("Chat", "PrecisionRouting", queueName,tenantId);
		return queueEngineService.checkQueueIsAvailable(queueFullName, checkApiUrl,tenantId);
	}
	
	public HashMap<String, Object> getWeightageForAnAgent(AgentDataRedis agent,Double languageWeightage,Double productWeightage) {
	
		/*
		 * Double languageWeightage = 40.00; Double productWeightage = 60.00;
		 */
		HashMap<String, Double> weigtageMap = new HashMap<String, Double>();
		
		for (AgentLanguageRedis language : agent.getLanguage()) {

			Integer langProfval = getProficiencyValue(language.getProficiency());
			Double langwt = (double) (langProfval * (languageWeightage / 100));

			for (AgentSkillSetRedis skill : agent.getSkillSet()) {
				
				Integer skillProfval = getProficiencyValue(skill.getProficiency());
				Double skillwt = (double) (skillProfval * (productWeightage / 100));

				Double totalWeightage = langwt + skillwt;
				totalWeightage = (double) Math.round(totalWeightage * 100);
				totalWeightage = totalWeightage / 100;
				weigtageMap.put(language.getLanguage() + skill.getSkill(), totalWeightage);
			}
		}

		HashMap<String, Object> sortedweightageMap = weigtageMap.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
		
		return sortedweightageMap;
	}
	
	public void pushToQueue(BestRouteInput request,String tenantId) {
	try {
		String queueName = request.getCustomerLanguage()+request.getSkillSet();
		ChatRequestDetails chatdata = new ChatRequestDetails();
		chatdata.setChatSessionId(CommonUtil.nullRemove(request.getChat_session_id()));
		chatdata.setChatUserPhoneNumber(CommonUtil.nullRemove(request.getPhoneNumber()));
		//String chatDataStr = "{\"chatSessionId\":\""+chatdata.getChatSessionId()+"\",\"chatUserPhoneNumber\":\""+chatdata.getChatUserPhoneNumber()+"\"}" ;// convertChatToJson(chatdata);
	//	String chatDataStr = convertChatToJson(chatdata);
		//chatDataStr = StringEscapeUtils.escapeJava(chatDataStr);
		String queueFullName = queueEngineService.getQueueName("Chat", "PrecisionRouting", queueName,tenantId);
		QueueMessage msg = new QueueMessage();
		msg.setQueueName(queueFullName);
		msg.setRoutingKey(queueFullName);
		//msg.setMessage(chatDataStr);
		//Integer priority = getMessagePriority(request.getChat_inititaed_at());
		Integer priority = 1;
		if((priority!=999) && (priority!=104) && (priority!=105))
		msg.setPriority(priority);
		String chatStr = queueEngineService.pushMessage(msg,chatdata.getChatSessionId(),chatdata.getChatUserPhoneNumber(),pushMessageUrl,tenantId);
		//redisQueue.pushRight(queueName, chatDataStr);
	}
	catch(Exception e) {
		LOGGER.error(request.getChat_session_id() +":pushToQueue: Exception: " + request.getSkillSet() + "	" + e.getMessage());
	}
	}
	
	private Integer getMessagePriority(String chat_inititaed_at) {

	boolean success = true;
		try {
			Instant instant1 = Instant.parse(chat_inititaed_at);
	    	Instant instant2 = Instant.now();
	    	Long timeDiff =instant1.until(instant2, ChronoUnit.MINUTES);
	    	
	    	if(timeDiff==0 || timeDiff==1)
	    		return 1;
	    	if(timeDiff==2)
	    		return 2;
	    	if(timeDiff==3)
	    		return 3;
	    	if(timeDiff==4)
	    		return 4;
	      	if(timeDiff==5)
	    		return 999;  	
	    	
		}
		catch(Exception e) {
			LOGGER.error(chat_inititaed_at +":getMessagePriority: Exception: " + chat_inititaed_at + "	" + e.getMessage());
			throw e;
		}
		
		if (!success) 
			return 104;
		else
			return 105;
		
		
	}

	public void removeFromQueue(BestRouteInput request) {
		
		String queueName = request.getCustomerLanguage()+request.getSkillSet();
		ChatRequestDetails chatdata = new ChatRequestDetails();
		chatdata.setChatSessionId(CommonUtil.nullRemove(request.getChat_session_id()));
		chatdata.setChatUserPhoneNumber(CommonUtil.nullRemove(request.getPhoneNumber()));
		String chatDataStr = convertChatToJson(chatdata);
		redisQueue.remove(queueName, chatDataStr);
	}
	
	Integer getProficiencyValue(String proficiency){
		proficiency = proficiency.trim();
		Integer val = 0;
		switch (proficiency) {
		case("Proficient"):
			val = 3;
			break;
		case("Intermediate"):
			val = 2;
			break;
		case("Basic"):
			val = 1;
			break;
		}
		return val;
	}
	
	private String convertChatToJson(ChatRequestDetails chatdata) {
		String json = new Gson().toJson(chatdata);
		return json;
	}
	
	public String getAgentDetails(String tenantId) {
		return redisString.findById(tenantId+"_agentsDetails");
	}
	
	public List<String> getRejectedAgentdetails(String chatSessionId){
		String rejectAgentList = redisString.findById("reject_chat"+chatSessionId);
		LOGGER.info( "getRejectedAgentdetails :" + rejectAgentList);
		if(rejectAgentList!= null)
			return CommonUtil.convertStringIntoList(rejectAgentList);
		else
			return new LinkedList();
	}
	
	public Map<String,Object> getChannelConfigData(String tenantId, String channelName){
	Map<String,Object> result = new HashMap();
		String routingConfigStr = "";
		try {
		routingConfigStr = UserManagementAPI.getRoutingConfig(tenantId,routingConfigUrl);
		} catch (UnirestException e) {
			System.out.println(e.getMessage());
			LOGGER.error("Error : "+e.getMessage());
//			e.printStackTrace();
		}
		result.put("response", routingConfigStr);
	Map configData = CommonUtil.convertStringIntMap(routingConfigStr);
	
	Map data = (LinkedHashMap)configData.get("data");
//	List<LinkedHashMap>concurrencyConfig = (List)data.get("channelConcurrency");
	
	// By Naresh
		Integer globalConcurrency = (Integer)data.get("globalConcurrency");
		Integer globalSkillWeightage = (Integer) data.get("globalSkillWeightage");
		Integer globalLanguageWeightage = (Integer) data.get("globalLanguageWeightage");
		String globalRoutingName = (String)data.get("globalRoutingName");

		result.put("globalConcurrency",globalConcurrency);
		result.put("globalSkillWeightage",globalSkillWeightage);
		result.put("globalLanguageWeightage",globalLanguageWeightage);
		result.put("globalRoutingName",globalRoutingName);


//	for (Map configMap : concurrencyConfig) {
//
//		if(channelName.equalsIgnoreCase((String)configMap.get("channelName"))){
//			
//			Integer concurrency = (Integer)configMap.get("concurrency");
//			/*
//			Integer skillWeightage = (Integer)configMap.get("skillWeightage");
//
//			Integer languageWeightage = (Integer)configMap.get("languageWeightage");
//			*/
//			result.put("concurrency", concurrency);
//
//			/*
//			 * result.add(skillWeightage); result.add(languageWeightage);
//			 */
//			
//		}
//	}
	
//	List<LinkedHashMap>channelRouting = (List)data.get("channelRouting");
//
//	for (Map configMap : channelRouting) {
//
//		if(channelName.equalsIgnoreCase((String)configMap.get("channelName"))){
//
//			String routingName = (String)configMap.get("routingName");
//			Integer skillWeightage = (Integer)configMap.get("skillWeightage");
//			Integer languageWeightage = (Integer)configMap.get("languageWeightage");
//
//			result.put("routingName", routingName);
//			result.put("skillWeightage", skillWeightage);
//			result.put("languageWeightage", languageWeightage);
//		}
//	}
	
		return result;
	}
	
	public String getChatCount(String queueName, String agentid) {
//		LOGGER.info("ChatRoutingOperations queueName : "+queueName +"   agentid : " + agentid);
		try {
		System.out.println(redisMap.getValue(queueName,agentid));
		}
		catch(Exception e) {
			LOGGER.error("ChatRoutingOperations :Exception  queueName : "+queueName +"   agentid : " + agentid + e.getMessage());	
		}
		return (String) redisMap.getValue(queueName,agentid);
	}
}
