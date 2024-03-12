package com.routing.engine.services.impl;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.routing.engine.external.apis.UserManagementAPI;
import com.routing.engine.request.AgentChannelRedis;
import com.routing.engine.request.AgentChatDataRedis;
import com.routing.engine.request.AgentDataRedis;
import com.routing.engine.request.AgentLanguageRedis;
import com.routing.engine.request.AgentSkillSetRedis;
import com.routing.engine.request.BestRouteInput;
import com.routing.engine.request.ChatRequestDetails;
import com.routing.engine.response.BestRouteOutput;
import com.routing.engine.response.GenericResponse;
import com.routing.engine.services.RoutingEngineService;
import com.routing.engine.internal.operations.ChatRoutingOperations;
import com.routing.engine.internal.operations.EmailRoutingOperations;

@Service
public class RoutingEngineServiceImpl implements RoutingEngineService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoutingEngineServiceImpl.class);

    @Autowired
    ChatRoutingOperations chatRoutingOperation;

    @Autowired
    EmailRoutingOperations emailRoutingOperations;

    @Value("${routing.chat.update-agent}")
    private String updateAgentUrl;
//
//    @Value("${dcc.client.id}")
//    private String clienttenantId;

    @Override
    public ResponseEntity<GenericResponse> getBestRoute(BestRouteInput request, String tenantId) {
        LOGGER.debug(request.getChat_session_id() + ": " + "getBestRoute:" + request.getCustomerLanguage());
        LOGGER.info(request.getChat_session_id() + ":getBestRoute:" + request.getCustomerLanguage());
        LOGGER.error(request.getChat_session_id() + ":getBestRoute: Exception: " + request.getCustomerLanguage());
        Map invalidAgentMap = null;
        String errorMessage = "";
        GenericResponse gResponse = new GenericResponse();
        gResponse = new GenericResponse();
        gResponse.setError("");
        gResponse.setStatus(1001);
        //String queueStatus = "Waiting for Agent";

        //push values in Queue
        //request.setChat_status(queueStatus);
        //chatRoutingOperation.pushToQueue(request);

        BestRouteOutput response = new BestRouteOutput();

        String agentJson = chatRoutingOperation.getAgentDetails(tenantId);

        ArrayList<Map> agentRedisList = getBodyObjectFromVarDataTypeJson(agentJson);
        List<AgentDataRedis> agentList = new ArrayList();
        for (Map agentmap : agentRedisList) {

            //agent Data validation from redis
            errorMessage = validateAgentData(agentmap);
            if (errorMessage != "") {
                invalidAgentMap = agentmap;
                //	gResponse.setValue(invalidAgentMap);
                gResponse.setStatus(1004);
                gResponse.setError(errorMessage);
                return new ResponseEntity<GenericResponse>(new GenericResponse(gResponse), HttpStatus.OK);
            }
            AgentDataRedis agent = null;
            try {
                agent = getAgentRedisData(agentmap, tenantId);
            } catch (Exception e) {
                LOGGER.error("GetBestRouteV1 : Agent details from getAgentRedisData is empty or invalid" + e.getMessage());
            }
            agentList.add(agent);
        }

        //	AgentDataRedis bestAgent = getBestAgent(agentList);
        HashMap<String, String> filterCriteria = new HashMap();
        filterCriteria.put("language", request.getCustomerLanguage());
        filterCriteria.put("skillSet", request.getSkillSet());
        filterCriteria.put("chatSessionId", request.getChat_session_id());
        AgentDataRedis bestAgent = getBestAgent(agentList, filterCriteria, tenantId);
        if (bestAgent == null) {
            gResponse.setError("Agent Not available Hence chat request has been pushed to Queue");
            gResponse.setStatus("1002");
            //	QueueChatRequest(request,false);
            chatRoutingOperation.pushToQueue(request, tenantId);
        } else if (bestAgent.getStatus().contains("Error:")) {
            gResponse.setError(bestAgent.getStatus() + "			" + bestAgent.getApiResponseStatus());
            gResponse.setStatus("1004");
            chatRoutingOperation.pushToQueue(request, tenantId);
        } else {
            response.setUserId(bestAgent.getUserId());
            response.setAgentName(bestAgent.getFirstName());
            gResponse.setValue(response);
            gResponse.setMessage("Agent Details");
            gResponse.setStatus("1001");
            //queueStatus = "Waiting for Accept Request";
        }
        //chatRoutingOperation.pushToQueue(request);


        return new ResponseEntity<GenericResponse>(new GenericResponse(gResponse), HttpStatus.OK);
    }

    private static ArrayList<Map> getBodyObjectFromVarDataTypeJson(String bodyJson) {

        ObjectMapper mapper = new ObjectMapper();
        ArrayList ap = null;
        try {
            ap = mapper.readValue(bodyJson, ArrayList.class);

        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            LOGGER.error("Error : " + e.getMessage());
//            e.printStackTrace();
        }

        return ap;

    }

    private AgentDataRedis getAgentRedisData(Map agentMap, String tenantId) {
        AgentDataRedis agent = null;
        try {
            agent = new AgentDataRedis();
            //	agent.setFirstName((String)agentMap.get("firstName"));
            //	agent.setLastName((String)agentMap.get("lastName"));
            //agent.setMobileNumber(Integer.toString((Integer)agentMap.get("mobileNumber")));
            agent.setEmail((String) agentMap.get("email"));
            agent.setUserId((String) agentMap.get("userId"));
            agent.setRoles((List<String>) agentMap.get("roles"));
            agent.setGroups((List<String>) agentMap.get("groups"));
            agent.setLanguage(getAgentLanguageData((List<Map>) agentMap.get("language")));
            agent.setSkillSet(getAgentSkillData((List<Map>) agentMap.get("skillSet")));
            agent.setChannel(getAgentChannelData((List<Map>) agentMap.get("channel")));
            agent.setStatus((String) agentMap.get("status"));
            Boolean emailFlag = agent.getChannel().stream().anyMatch(s -> s.getChannelName().equals("Email"));
            agent.setChat(getAgentChatData((Map) agentMap.get("chat"), agent.getUserId(), tenantId, emailFlag));
            //	agent.setIsSupvervisor((Boolean)agentMap.get("isSupvervisor"));
        } catch (ParseException e) {
            LOGGER.error("GetAgentRedisData agent is empty or invalid : " + e.getMessage());
//            e.printStackTrace();
        }
        return agent;
    }

    private List<AgentLanguageRedis> getAgentLanguageData(List<Map> languageList) {

        List<AgentLanguageRedis> result = new ArrayList<AgentLanguageRedis>();

        for (Map languageMap : languageList) {
            if (languageMap.containsKey("languageDesc") && languageMap.containsKey("proficiencyDesc")) {
                AgentLanguageRedis language = new AgentLanguageRedis();
                language.setLanguage((String) languageMap.get("languageDesc"));
                language.setProficiency((String) languageMap.get("proficiencyDesc"));
                result.add(language);
            } else if (languageMap.containsKey("language") && languageMap.containsKey("proficiency")) {
                AgentLanguageRedis language = new AgentLanguageRedis();
                language.setLanguage((String) languageMap.get("language"));
                language.setProficiency((String) languageMap.get("proficiency"));
                result.add(language);
            }
        }
        return result;
    }

    private List<AgentSkillSetRedis> getAgentSkillData(List<Map> skillList) {

        List<AgentSkillSetRedis> result = new ArrayList<AgentSkillSetRedis>();
        for (Map skillMap : skillList) {
            if (skillMap.containsKey("skillName") && skillMap.containsKey("proficiencyDesc")) {
                AgentSkillSetRedis skill = new AgentSkillSetRedis();
                skill.setSkill((String) skillMap.get("skillName"));
                skill.setProficiency((String) skillMap.get("proficiencyDesc"));
                result.add(skill);
            } else if (skillMap.containsKey("skill") && skillMap.containsKey("proficiency")) {
                AgentSkillSetRedis skill = new AgentSkillSetRedis();
                skill.setSkill((String) skillMap.get("skill"));
                skill.setProficiency((String) skillMap.get("proficiency"));
                result.add(skill);
            }
        }
        return result;
    }

    private AgentChatDataRedis getAgentChatData(Map chatData, String UserId, String tenantId, Boolean emailFlag) {
        AgentChatDataRedis result = new AgentChatDataRedis();
        LOGGER.info("UserId : " + UserId + ", tenantId : " + tenantId);
        try {
            if (chatData.get("active_chat_count") instanceof Integer) {

                result.setActive_chat_count(Integer.toString((Integer) chatData.get("active_chat_count")));
                result.setLast_chat_end_time((String) chatData.get("last_chat_end_time"));
                result.setLast_chat_time((String) chatData.get("last_chat_time"));
                result.setTotal_completed(Integer.toString((Integer) chatData.get("total_completed")));
                result.setTotal_ongoing(Integer.toString((Integer) chatData.get("total_ongoing")));
                result.setQueued_count(Integer.toString((Integer) chatData.get("queued_count")));
            }

            if (chatData.get("active_chat_count") instanceof String) {
                result.setActive_chat_count((String) chatData.get("active_chat_count"));
                result.setLast_chat_end_time((String) chatData.get("last_chat_end_time"));
                result.setLast_chat_time((String) chatData.get("last_chat_time"));
                result.setTotal_completed((String) chatData.get("total_completed"));
                result.setTotal_ongoing((String) chatData.get("total_ongoing"));
                result.setQueued_count((String) chatData.get("queued_count"));
            }
        } catch (Exception e) {
            LOGGER.error("getAgentChatData on get active_chat_count : UserId :" + UserId + "     " + e.getMessage());
        }
//            if (emailFlag) {
//                LOGGER.info("UserId : " + UserId);
//                result.setActive_chat_count(Integer.toString(emailRoutingOperations.getEmailCount("emailactivechatcount", UserId)));
//                result.setTotal_ongoing(Integer.toString(emailRoutingOperations.getEmailCount("emailactivechatcount", UserId)));
//                result.setQueued_count(Integer.toString(emailRoutingOperations.getEmailCount("emailqueuecount", UserId)));
//            } else {
        try {
            result.setActive_chat_count(chatRoutingOperation.getChatCount(tenantId + "_activechatcount", UserId));
            result.setTotal_ongoing(chatRoutingOperation.getChatCount(tenantId + "_activechatcount", UserId));
            result.setQueued_count(chatRoutingOperation.getChatCount(tenantId + "_queuecount", UserId));
        } catch (Exception e) {
            LOGGER.error("getAgentChatData set result counts : UserId :" + UserId + "     " + e.getMessage());
        }
        return result;
    }

    private List<AgentChannelRedis> getAgentChannelData(List<Map> channelList) {

        List<AgentChannelRedis> result = new ArrayList<AgentChannelRedis>();
        for (Map channelMap : channelList) {
            if (channelMap.containsKey("channelName") && channelMap.containsKey("channelId")) {
                AgentChannelRedis channel = new AgentChannelRedis();
                channel.setChannelName((String) channelMap.get("channelName"));
                channel.setChannelId((String) channelMap.get("channelId"));

                if (channelMap.containsKey("parentClientChannelId")) {
                    channel.setParentClientChannelId((String) channelMap.get("parentClientChannelId"));
                }
                result.add(channel);
            }

        }
        return result;
    }

    private AgentDataRedis getBestAgent(List<AgentDataRedis> agentRedisList, Map<String, String> filterCriteria, String tenantId) {

//	Map configMap =	chatRoutingOperation.getChannelConfigData("a3dc14bd-fe70-4120-8572-461b0dc866b5", "Chat");
        Map configMap = chatRoutingOperation.getChannelConfigData(tenantId, "Chat");

//	Double LanguageWeightage = ((Integer)configMap.get("languageWeightage")).doubleValue();
//	Double ProductWeightage = ((Integer)configMap.get("skillWeightage")).doubleValue();
        Double LanguageWeightage = null; // by Naresh
        Double ProductWeightage = null;
        Object languageWeightageObj = configMap.get("globalLanguageWeightage");
        if (languageWeightageObj != null) {
            LanguageWeightage = ((Integer) languageWeightageObj).doubleValue();
        } else {
            LanguageWeightage = (Double) languageWeightageObj;
        }
        // Retrieve "skillWeightage" from configMap
        Object skillWeightageObj = configMap.get("globalSkillWeightage");
        if (skillWeightageObj != null) {
            ProductWeightage = ((Integer) skillWeightageObj).doubleValue();
        } else {
            ProductWeightage = (Double) skillWeightageObj;
        }
        Integer concurrency = (Integer) configMap.get("globalConcurrency");
        String routingName = (String) configMap.get("globalRoutingName");
        System.out.println(" LanguageWeightage " + LanguageWeightage + " ProductWeightage " + ProductWeightage
                + " concurrency " + concurrency + " routingName " + routingName);

        LOGGER.info("getBestAgent: chatSessionId :" + filterCriteria.get("chatSessionId"));
        LOGGER.info("getBestAgent: LanguageWeightage :" + LanguageWeightage + " ProductWeightage :" + ProductWeightage);
        LOGGER.info("getBestAgent: concurrency :" + concurrency + " routingName :" + routingName);

        if (!"Precision Routing".equalsIgnoreCase(routingName)) {
            LOGGER.error("Error: Routing Config is not Present routingName:" + routingName);
            AgentDataRedis agent1 = new AgentDataRedis();
            agent1.setStatus("Error: Routing Config is not Present");
            agent1.setApiResponseStatus((String) configMap.get("response"));
            return agent1;
        }

        //{routingName=Precision Routing, skillWeightage=40, languageWeightage=60, concurrency=3}
        //Get the Rejected list of agents
        List<String> rejectAgents = chatRoutingOperation.getRejectedAgentdetails(filterCriteria.get("chatSessionId"));
        //***************************************Filter Agents Based on Skill and Language******************************************************
        List<AgentDataRedis> agentList = null;
        String errorMessage = "";
        AgentDataRedis bestAgent = null;
        try {
            List eg = new ArrayList();
            //isSupvervisor
            agentList = agentRedisList.stream()
                    .filter(p -> p.getStatus().equalsIgnoreCase("Available"))
                    .filter(p -> p.getLanguage().stream().anyMatch(s -> s.getLanguage().equals(filterCriteria.get("language"))))
                    .filter(p -> p.getSkillSet().stream().anyMatch(s -> s.getSkill().equals(filterCriteria.get("skillSet"))))
                    .filter(p -> ((Integer.parseInt(p.getChat().getTotal_ongoing()) + Integer.parseInt(p.getChat().getQueued_count())) < concurrency))
                    .filter(p -> (!rejectAgents.contains(p.getUserId())))
                    .collect(Collectors.toList());
            LOGGER.info("getBestAgent : Filter completed");
            SortedMap<Double, List<AgentDataRedis>> agentWeightageMap = new TreeMap<>();
            //***************************************Weightage calculation for Agents******************************************************
            for (AgentDataRedis agent : agentList) {
                Double languagewt = 0.0;
                Double skillwt = 0.0;
                LOGGER.info("getBestAgent :agentList" + "Email" + agent.getEmail() + "agent" + agent.getSkillSet() + "Language" + agent.getLanguage());
                for (AgentLanguageRedis language : agent.getLanguage()) {
                    if (filterCriteria.get("language").equalsIgnoreCase(language.getLanguage())) {
                        Integer val = getProficiencyValue(language.getProficiency());
                        languagewt = (double) (val * (LanguageWeightage / 100));
                    }
                }

                for (AgentSkillSetRedis skill : agent.getSkillSet()) {
                    if (filterCriteria.get("skillSet").equalsIgnoreCase(skill.getSkill())) {
                        Integer val = getProficiencyValue(skill.getProficiency());
                        skillwt = (double) (val * (ProductWeightage / 100));
                    }
                }

                Double agentWeightage = languagewt + skillwt;

                agentWeightage = (double) Math.round(agentWeightage * 100);
                agentWeightage = agentWeightage / 100;

                if (agentWeightageMap.containsKey(agentWeightage)) {
                    agentWeightageMap.get(agentWeightage).add(agent);
                } else {
                    List<AgentDataRedis> wtAgentList = new ArrayList();
                    wtAgentList.add(agent);
                    agentWeightageMap.put(agentWeightage, wtAgentList);
                }
            }

            Double lastKey = (Double) agentWeightageMap.lastKey();

            List<AgentDataRedis> highWeightageAgentList = agentWeightageMap.get(lastKey);

            //Start To be Removed After Testing
            for (Entry entry : agentWeightageMap.entrySet()) {
                List<AgentDataRedis> val = (ArrayList<AgentDataRedis>) entry.getValue();
                System.out.println("Weightage :" + entry.getKey());
                for (AgentDataRedis agentDataRedis : val) {
                    System.out.println("User Name :" + agentDataRedis.getFirstName() + "	User id :" + agentDataRedis.getUserId());
                }
                System.out.println("");
            }
            System.out.println("Agents with highest weightage: " + lastKey);
            for (AgentDataRedis agentDataRedis : highWeightageAgentList) {
                System.out.println("User Name :" + agentDataRedis.getFirstName() + "	User id :" + agentDataRedis.getUserId());
            }
            System.out.println("");

            //End  To be Removed After Testing

            //***************************************List of Least On Going Agents************************************************
            LOGGER.info("List of Least On Going Agents");
            Comparator<AgentDataRedis> comparator = Comparator.<AgentDataRedis, Integer>comparing(agent -> Integer.parseInt(agent.getChat().getTotal_ongoing()));
            highWeightageAgentList.sort(comparator);
            String totalOngoing = highWeightageAgentList.get(0).getChat().getTotal_ongoing();

            List<AgentDataRedis> leastOnGoingAgents = new ArrayList();
            for (AgentDataRedis agentDataRedis : highWeightageAgentList) {
                if (totalOngoing.equalsIgnoreCase(agentDataRedis.getChat().getTotal_ongoing())) {
                    leastOnGoingAgents.add(agentDataRedis);
                } else
                    break;
            }

            //Start To be Removed After Testing

            System.out.println("Agents with Least on going Chats ");
            for (AgentDataRedis agentDataRedis : highWeightageAgentList) {
                System.out.println("User Name :" + agentDataRedis.getFirstName());
                System.out.println("user id : " + agentDataRedis.getUserId() + "	On Going Chats :" + agentDataRedis.getChat().getTotal_ongoing() + "		Last chat End Time" + agentDataRedis.getChat().getLast_chat_end_time());
            }
            System.out.println("");

            //End  To be Removed After Testing


            /*
             * for (AgentDataRedis agentDataRedis : highWeightageAgentList) {
             * System.out.println(""+"User id :"+agentDataRedis.getUserId()); }
             */

            //***************************************Find The Most idle Agent****************************************************
            LOGGER.info("Find The Most idle Agent");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));   // This line converts the given date into UTC time zone
            java.util.Date oldDate = new Date();

            for (AgentDataRedis agentDataRedis : leastOnGoingAgents) {
                if (StringUtils.isEmpty(agentDataRedis.getChat().getLast_chat_end_time())) {
                    bestAgent = agentDataRedis;
                    break;
                }
                final java.util.Date dateObj = sdf.parse(agentDataRedis.getChat().getLast_chat_end_time());
                if (dateObj.before(oldDate)) {
                    oldDate = dateObj;
                    bestAgent = agentDataRedis;
                }
                System.out.println("Chat end time of user " + agentDataRedis.getUserId() + " " + dateObj.toString());
            }
        } catch (NoSuchElementException e) {
            errorMessage = "Agents are not available";
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            LOGGER.error("Error : " + e.getMessage());
//            e.printStackTrace();
        } catch (java.text.ParseException e) {
            LOGGER.error("Error : " + e.getMessage());
            // TODO Auto-generated catch block
//            e.printStackTrace();
        }

        if (bestAgent != null) { // by Naresh
            LOGGER.info("getBestAgent :Final bestAgent" + "Email" + bestAgent.getEmail() + "agent" + bestAgent.getSkillSet() + "Language" + bestAgent.getLanguage());
            return bestAgent;
        } else {
            LOGGER.error("It's seem best agent value is " + bestAgent); // by Naresh
            return bestAgent;
        }
    }


    private String validateAgentData(Map agentMap) {
        String errorMessage = "";


        /*
         * if(agentMap.get("firstName") == null || (
         * ((String)agentMap.get("firstName"))== "")) return "firstName id is invalid";
         * if(agentMap.get("lastName") == null || ( ((String)agentMap.get("lastName"))==
         * "")) return "lastName id is invalid";
         */
        /*
         * if(agentMap.get("mobileNumber") == null) return "mobileNumber id is invalid";
         */
        if (agentMap.get("email") == null || (((String) agentMap.get("email")) == ""))
            return "email id is invalid";
        if (agentMap.get("userId") == null || (((String) agentMap.get("userId")) == ""))
            return "User id is invalid";
        if (agentMap.get("userId") == null || (((String) agentMap.get("userId")) == ""))
            return "User id is invalid";
        /*
         * if(agentMap.get("roles") == null ) return "roles id is invalid";
         * if(agentMap.get("groups") == null ) return "groups id is invalid";
         */

        /*
         * if(agentMap.get("status") == null || ( ((String)agentMap.get("status"))==
         * "")) return "status id is invalid";
         */

        errorMessage = validateAgentChatData((Map) agentMap.get("chat"));
        if (errorMessage != "")
            return errorMessage + " Id: " + agentMap.get("id") + " Agent Name: " + agentMap.get("firstName");
        errorMessage = validateAgentLanguageData((List<Map>) agentMap.get("language"));
        if (errorMessage != "")
            return errorMessage + " Id: " + agentMap.get("id") + " Agent Name: " + agentMap.get("firstName");
        errorMessage = validateAgentSkillData((List<Map>) agentMap.get("skillSet"));
        if (errorMessage != "")
            return errorMessage + " Id: " + agentMap.get("id") + " Agent Name: " + agentMap.get("firstName");

        return "";
    }

    private String validateAgentChatData(Map chatData) {
        if (chatData == null)
            return "Chat details cannot be empty for an agent";
        if (chatData.get("active_chat_count") == null)
            return "active_chat_count is invalid";
        /*
         * if(chatData.get("last_chat_end_time") == null || (
         * ((String)chatData.get("last_chat_end_time"))== "")) return
         * "last_chat_end_time is invalid"; if(chatData.get("last_chat_time") == null ||
         * ( ((String)chatData.get("last_chat_time"))== "")) return
         * "last_chat_time is invalid";
         */
        if (chatData.get("total_completed") == null)
            return "total_completed is invalid";
        if (chatData.get("total_ongoing") == null)
            return "total_ongoing is invalid";
        return "";
    }

    private String validateAgentLanguageData(List<Map> languageList) {

        if (languageList == null)
            return "language details cannot be empty";

        for (Map languageMap : languageList) {

            //if(languageMap.get("languageDesc") == null || ( ((String)languageMap.get("languageDesc"))== ""))
            if (languageMap.get("languageDesc") == null)
                return "language is invalid";
            //		if(languageMap.get("proficiencyDesc") == null || ( ((String)languageMap.get("proficiencyDesc"))== ""))
            if (languageMap.get("proficiencyDesc") == null)
                return "language proficiency is invalid";
        }
        return "";
    }

    private String validateAgentSkillData(List<Map> skillList) {

        if (skillList == null)
            return "skill List cannot be empty for an agent";
        for (Map skillMap : skillList) {

            //if(skillMap.get("skillName") == null || ( ((String)skillMap.get("skillName"))== ""))
            if (skillMap.get("skillName") == null)
                return "skill is invalid";
            //if(skillMap.get("proficiencyDesc") == null || ( ((String)skillMap.get("proficiencyDesc"))== ""))
            if (skillMap.get("proficiencyDesc") == null)
                return "skill proficiency is invalid";
        }
        return "";
    }

    Integer getProficiencyValue(String proficiency) {
        Integer val = 0;
        switch (proficiency) {
            case ("Proficient"):
                val = 3;
                break;
            case ("Intermediate"):
                val = 2;
                break;
            case ("Basic"):
                val = 1;
                break;
        }
        return val;
    }


    Queue<ChatRequestDetails> convertJsontoQueue(String queueJson) {

        ObjectMapper mapper = new ObjectMapper();
        LinkedList<ChatRequestDetails> ap = null;
        try {
            ap = mapper.readValue(queueJson, LinkedList.class);

        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
            LOGGER.error("Error : " + e.getMessage());
        }

        List<ChatRequestDetails> pojos = mapper.convertValue(ap, new TypeReference<List<ChatRequestDetails>>() {
        });
        Queue<ChatRequestDetails> newQueue = new LinkedList<ChatRequestDetails>(pojos);
        System.out.println(pojos.toString());

        return newQueue;
    }

    String convertQueueToJson(Queue<ChatRequestDetails> routingQueue) {
        String json = new Gson().toJson(routingQueue);
        return json;
    }

    String convertChatToJson(ChatRequestDetails chatdata) {
        String json = new Gson().toJson(chatdata);
        return json;
    }


    private static Map<String, Object> convertAgentJsonToMap(String bodyJson) {

        ObjectMapper mapper = new ObjectMapper();
        Map ap = null;
        try {
            ap = mapper.readValue(bodyJson, LinkedHashMap.class);

        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
            LOGGER.error("Error : " + e.getMessage());
        }

        return ap;

    }


    @SuppressWarnings("unchecked")
    @Override
    public ResponseEntity<GenericResponse> availableAgent(String agentDetailsJson, String tenantId) {
        LOGGER.info("123" + "availableAgent - agent Fetch user config - agents details: " + "agentDetailsJson" + agentDetailsJson);
        List<Object> resultList = new ArrayList<Object>();
        AgentDataRedis agent = null;
//        LOGGER.info("availableAgent agentDetailsJson :" + agentDetailsJson);
        if (agentDetailsJson == null || agentDetailsJson.isEmpty()) {
            LOGGER.error("Agent details from agentDetails Json is null or Empty" + agentDetailsJson);
        }
        try {
            Map<String, Object> agentMap = null;
            try {
                agentMap = convertAgentJsonToMap(agentDetailsJson);
            } catch (Exception e) {
                LOGGER.error("Agent details from convertAgentJsonToMap is Empty");
            }
            agent = getAgentRedisData(agentMap, tenantId);
        } catch (Exception e) {
            LOGGER.error("Available agent details from getAgentRedisData is Empty");
        }
        Double LanguageWeightage = null;
        Double ProductWeightage = null;
        Integer concurrency = null;
        try {
            //Map configMap =	chatRoutingOperation.getChannelConfigData("a3dc14bd-fe70-4120-8572-461b0dc866b5", "Chat");
            Map configMap = chatRoutingOperation.getChannelConfigData(tenantId, "Chat");


            LanguageWeightage = ((Integer) configMap.get("globalLanguageWeightage")).doubleValue();
            ProductWeightage = ((Integer) configMap.get("globalSkillWeightage")).doubleValue();
            concurrency = (Integer) configMap.get("globalConcurrency");
        } catch (Exception e) {
            LOGGER.error("123" + "Available - agent Fetch user config - agents details: " + "LanguageWeightage"
                    + LanguageWeightage + "concurrency" + concurrency + "ProductWeightage" + ProductWeightage
                    + ":availableAgent: Exception: " + e.getMessage());
            GenericResponse gResponse = new GenericResponse();
            gResponse.setStatus(1004);
            gResponse.setError("Error occured in fetching user config data");
            return new ResponseEntity<GenericResponse>(new GenericResponse(gResponse), HttpStatus.OK);
        }

        try {
            if ("Ready".equalsIgnoreCase(agent.getStatus()) || "Available".equalsIgnoreCase(agent.getStatus())) {
                LOGGER.info("availableAgent :Status" + "Status()" + agent.getStatus());
                if ((Integer.parseInt(agent.getChat().getTotal_ongoing()) + Integer.parseInt(agent.getChat().getQueued_count())) >= concurrency) {
                    LOGGER.error("availableAgent Agent exceeded the concurrency Limit");
                    GenericResponse gResponse = new GenericResponse();
                    gResponse.setStatus(1005);
                    gResponse.setError("Agent exceeded the concurrency Limit");
                    return new ResponseEntity<GenericResponse>(new GenericResponse(gResponse), HttpStatus.OK);
                }
                HashMap<String, Object> sortedweightageMap = chatRoutingOperation.getWeightageForAnAgent(agent, LanguageWeightage, ProductWeightage);
                Iterator it = sortedweightageMap.entrySet().iterator();

                while (it.hasNext()) {
                    LOGGER.info("availableAgent inside sortedweightage agent loop ");
                    Map.Entry<String, Double> pair = (Entry<String, Double>) it.next();
                    System.out.println(pair.getKey() + " = " + pair.getValue());
                    String checked = chatRoutingOperation.checkTheQueueIsPresent(pair.getKey(), tenantId);
                    String chatStr = "";
                    if (checked.contains("true")) {
                        LOGGER.info("Queue is Present : " + checked + ", Queue name :" + pair.getKey());
                        chatStr = chatRoutingOperation.getChatDataFromQueue(pair.getKey(), tenantId);
                    } else {
                        LOGGER.error("Queue is not Present.");
                        continue;
                    }
                    if (chatStr == null || "".equals(chatStr.trim()) || chatStr.contains("Failed") || chatStr.contains("empty")) {
                        it.remove(); // avoids a ConcurrentModificationException
                        LOGGER.info("availableAgent avoids ConcurrentModificationException ");
                        continue;
                    } else {
                        LOGGER.info("availableAgent ChatRequestDetails: " + chatStr);
                        ChatRequestDetails head = convertJsontoChat(chatStr);
                        Map<String, String> result = new HashMap<String, String>();
                        result.put("agent_id", agent.getUserId());
                        result.put("chat_session_id", head.getChatSessionId());
                        try {
                            LOGGER.info("availableAgent UserId: " + agent.getUserId() + "ChatSessionId()" + head.getChatSessionId() + "updateAgentUrl :" + updateAgentUrl);
                            resultList.add(UserManagementAPI.updateAgent(agent.getUserId(), head.getChatSessionId(), updateAgentUrl, tenantId));
                            LOGGER.info(agent.getUserId() + ":availableAgent:" + resultList.get(0));
                        } catch (UnirestException e) {
                            LOGGER.error(agent.getUserId() + ":availableAgent:Exception:" + " UpdateAgent API error");
                            // TODO Auto-generated catch block
//                            e.printStackTrace();
                        }
                        resultList.add(result);
                        GenericResponse gResponse = new GenericResponse();
                        gResponse.setStatus(1001);
                        gResponse.setValue(resultList);
                        return new ResponseEntity<GenericResponse>(new GenericResponse(gResponse), HttpStatus.OK);
                    }
                }
                //		it.remove(); // avoids a ConcurrentModificationException
            } else {
                GenericResponse gResponse = new GenericResponse();
                gResponse.setStatus(1004);
                gResponse.setError("Agent status invalid for Queue");
                return new ResponseEntity<GenericResponse>(new GenericResponse(gResponse), HttpStatus.OK);
            }
        } catch (Exception e) {
            LOGGER.error("123" + "Available agent - routing - agents details: " + agentDetailsJson + ":availableAgent: Exception: " + e.getMessage());
            GenericResponse gResponse = new GenericResponse();
            gResponse.setStatus(1004);
            gResponse.setError("Error occured in Routing");
            return new ResponseEntity<GenericResponse>(new GenericResponse(gResponse), HttpStatus.OK);
        }
        GenericResponse gResponse = new GenericResponse();
        gResponse.setStatus(1004);
        gResponse.setError("Agent Does not match with any queue");
        return new ResponseEntity<GenericResponse>(new GenericResponse(gResponse), HttpStatus.OK);
    }


    ChatRequestDetails convertJsontoChat(String chatJson) {

        chatJson = chatJson.replace("\\", "");
        chatJson = chatJson.replace("\"{", "{");
        chatJson = chatJson.replace("}\"", "}");
        ObjectMapper mapper = new ObjectMapper();
        LinkedHashMap<String, String> ap = null;
        try {
            ap = mapper.readValue(chatJson, LinkedHashMap.class);

        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
        }

        ChatRequestDetails pojos = mapper.convertValue(ap, new TypeReference<ChatRequestDetails>() {
        });
        System.out.println(pojos.toString());

        return pojos;
    }

    //Chat is pushed to Priority Queue when Rejected
    @SuppressWarnings("unchecked")
    @Override
    public ResponseEntity<GenericResponse> acceptChat(BestRouteInput request, String tenantId) {
        try {
            chatRoutingOperation.removeFromQueue(request);
        } catch (Exception e) {

            GenericResponse gResponse = new GenericResponse();
            gResponse.setStatus(1004);
            gResponse.setError("request failed");
            return new ResponseEntity<GenericResponse>(new GenericResponse(gResponse), HttpStatus.OK);
        }

        GenericResponse gResponse = new GenericResponse();
        gResponse.setStatus(1001);
        gResponse.setValue("Chat:" + request.getChat_session_id() + " 	Chat removed from queue");
        return new ResponseEntity<GenericResponse>(new GenericResponse(gResponse), HttpStatus.OK);

    }

//    ""@Override
//    public ResponseEntity<GenericResponse> getBestRouteEmail(BestRouteEmailInput request) {
//
//        LOGGER.info(request.getCrmId() + ": " + "getBestRouteEmail:" + request.getEmailId());
//
//        BestRouteOutput response = new BestRouteOutput();
//        String agentJson = chatRoutingOperation.getAgentDetails();
//        Map invalidAgentMap = null;
//        String errorMessage = "";
//        GenericResponse gResponse = new GenericResponse();
//        gResponse = new GenericResponse();
//        gResponse.setError("");
//        gResponse.setStatus(1001);
//
//        ArrayList<Map> agentRedisList = getBodyObjectFromVarDataTypeJson(agentJson);
//        List<AgentDataRedis> agentList = new ArrayList();
//        for (Map agentmap : agentRedisList) {
//
//            //agent Data validation from redis
//            errorMessage = validateAgentData(agentmap);
//            if (errorMessage != "") {
//                invalidAgentMap = agentmap;
//                //	gResponse.setValue(invalidAgentMap);
//                gResponse.setStatus(1004);
//                gResponse.setError(errorMessage);
//                LOGGER.error(request.getCrmId() + ": " + "getBestRouteEmail:" + request.getEmailId() + " : " + errorMessage);
//                return new ResponseEntity<GenericResponse>(new GenericResponse(gResponse), HttpStatus.OK);
//            }
//            AgentDataRedis agent = null;
//            try {
//                agent = getAgentRedisData(agentmap);
//            } catch (Exception e) {
//                LOGGER.error("GetBestRouteEmail : Agent details from getAgentRedisData is empty or invalid");
//            }
//            agentList.add(agent);
//        }
//
//        HashMap<String, String> filterCriteria = new HashMap();
//        if (request.getCustomerLanguage() == null || request.getCustomerLanguage().isEmpty())
//            filterCriteria.put("language", "English");
//        else
//            filterCriteria.put("language", request.getCustomerLanguage());
//        filterCriteria.put("skillSet", request.getSkillSet());
//        AgentDataRedis bestAgent = getBestAgentEmailNew(agentList, filterCriteria,tenantId);
//
//        if (bestAgent == null) {
//            gResponse.setError("Agent Not available Hence chat request has been pushed to Queue");
//            gResponse.setStatus("1002");
//            //chatRoutingOperation.pushToQueue(request);
//        } else if (bestAgent.getStatus().contains("Error:")) {
//            gResponse.setError(bestAgent.getStatus() + "			" + bestAgent.getApiResponseStatus());
//            gResponse.setStatus("1004");
//            //	chatRoutingOperation.pushToQueue(request);
//        } else {
//            response.setUserId(bestAgent.getUserId());
//            response.setAgentName("");
//            gResponse.setValue(bestAgent);
//            gResponse.setStatus("1001");
//            //queueStatus = "Waiting for Accept Request";
//        }
//
//        return new ResponseEntity<GenericResponse>(new GenericResponse(gResponse), HttpStatus.OK);
//    }""

//    AgentDataRedis getBestAgentEmail(List<AgentDataRedis> agentList, Map<String, String> filterCriteria) {
//
//        AgentDataRedis result = null;
//
//        agentList = agentList.stream()
//                .filter(p -> p.getSkillSet().stream().anyMatch(s -> s.getSkill().equals(filterCriteria.get("skillSet"))))
//                .collect(Collectors.toList());
//
//        if (emailRoutingOperations.checkforOverFlow()) {
//            emailRoutingOperations.deleteAllValues();
//        }
//
//        for (AgentDataRedis agentDataRedis : agentList) {
//
//            if (!checkChannelExist(agentDataRedis, "Email"))
//                continue;
//
//            Boolean isAgentRouted = emailRoutingOperations.checkIfAgentisRouted(agentDataRedis);
//
//            if (isAgentRouted) {
//                continue;
//            } else {
//                emailRoutingOperations.addAgentToRoutingList(agentDataRedis);
//                result = agentDataRedis;
//                break;
//            }
//        }
//        return result;
//    }

    Boolean checkChannelExist(AgentDataRedis agentDataRedis, String channel) {

        List<AgentChannelRedis> channelList = agentDataRedis.getChannel();

        for (AgentChannelRedis agentChannelRedis : channelList) {
            if (channel.equalsIgnoreCase(agentChannelRedis.getChannelName()))
                return true;
        }
        return false;
    }


    AgentDataRedis getBestAgentEmailNew(List<AgentDataRedis> agentRedisList, Map<String, String> filterCriteria) {

        //Map configMap =	chatRoutingOperation.getChannelConfigData("a3dc14bd-fe70-4120-8572-461b0dc866b5", "Email");
        Map configMap = chatRoutingOperation.getChannelConfigData("", "Email");

//        Double LanguageWeightage = ((Integer) configMap.get("languageWeightage")).doubleValue();
//        Double ProductWeightage = ((Integer) configMap.get("skillWeightage")).doubleValue();
        Double LanguageWeightage = null; // by Naresh
        Double ProductWeightage = null;
        Object languageWeightageObj = configMap.get("globalLanguageWeightage");
        if (languageWeightageObj != null) {
            LanguageWeightage = ((Integer) languageWeightageObj).doubleValue();
        } else {
            LanguageWeightage = (Double) languageWeightageObj;
        }
        // Retrieve "skillWeightage" from configMap
        Object skillWeightageObj = configMap.get("globalSkillWeightage");

        if (skillWeightageObj != null) {
            ProductWeightage = ((Integer) skillWeightageObj).doubleValue();
        } else {
            ProductWeightage = (Double) skillWeightageObj;
        }

        Integer concurrency = (Integer) configMap.get("globalConcurrency");
        String routingName = (String) configMap.get("globalRoutingName");
        System.out.println(" LanguageWeightage " + LanguageWeightage + " ProductWeightage " + ProductWeightage
                + " concurrency " + concurrency + " routingName " + routingName);


        if ("Precision Routing".equalsIgnoreCase(routingName)) {
            AgentDataRedis agent1 = new AgentDataRedis();
            agent1.setStatus("Error: Routing Config is not Present");
            agent1.setApiResponseStatus((String) configMap.get("response"));
            return agent1;
        }


        //{routingName=Precision Routing, skillWeightage=40, languageWeightage=60, concurrency=3}
        //Get the Rejected list of agents
//	List<String> rejectAgents = chatRoutingOperation.getRejectedAgentdetails(filterCriteria.get("chatSessionId"));	
        //***************************************Filter Agents Based on Skill and Language******************************************************
        List<AgentDataRedis> agentList = null;
        String errorMessage = "";
        AgentDataRedis bestAgent = null;
        try {
            List eg = new ArrayList();
            //isSupvervisor
            agentList = agentRedisList.stream()
                    .filter(p -> p.getStatus().equalsIgnoreCase("Available"))
                    .filter(p -> p.getChannel().stream().anyMatch(s -> s.getChannelName().equalsIgnoreCase("Email")))
                    .filter(p -> p.getLanguage().stream().anyMatch(s -> s.getLanguage().equals(filterCriteria.get("language"))))
                    .filter(p -> p.getSkillSet().stream().anyMatch(s -> s.getSkill().equals(filterCriteria.get("skillSet"))))
                    .filter(p -> ((Integer.parseInt(p.getChat().getTotal_ongoing()) + Integer.parseInt(p.getChat().getQueued_count())) < concurrency))
                    //	.filter(p -> (!rejectAgents.contains(p.getUserId())))
                    .collect(Collectors.toList());

            //*** Agent not available validation
            if (agentList == null)
                return bestAgent;

            SortedMap<Double, List<AgentDataRedis>> agentWeightageMap = new TreeMap<>();

            //***************************************Weightage calculation for Agents******************************************************
            for (AgentDataRedis agent : agentList) {
                LOGGER.info("****Weightage calculation for Agents*****");
                Double languagewt = 0.0;
                Double skillwt = 0.0;

                for (AgentLanguageRedis language : agent.getLanguage()) {
                    if (filterCriteria.get("language").equalsIgnoreCase(language.getLanguage())) {
                        Integer val = getProficiencyValue(language.getProficiency());
                        languagewt = (double) (val * (LanguageWeightage / 100));
                    }
                }

                for (AgentSkillSetRedis skill : agent.getSkillSet()) {
                    if (filterCriteria.get("skillSet").equalsIgnoreCase(skill.getSkill())) {
                        Integer val = getProficiencyValue(skill.getProficiency());
                        skillwt = (double) (val * (ProductWeightage / 100));
                    }
                }

                Double agentWeightage = languagewt + skillwt;

                agentWeightage = (double) Math.round(agentWeightage * 100);
                agentWeightage = agentWeightage / 100;

                if (agentWeightageMap.containsKey(agentWeightage)) {
                    agentWeightageMap.get(agentWeightage).add(agent);
                } else {
                    List<AgentDataRedis> wtAgentList = new ArrayList();
                    wtAgentList.add(agent);
                    agentWeightageMap.put(agentWeightage, wtAgentList);
                }
            }

            Double lastKey = (Double) agentWeightageMap.lastKey();

            List<AgentDataRedis> highWeightageAgentList = agentWeightageMap.get(lastKey);

            //Start To be Removed After Testing
            for (Entry entry : agentWeightageMap.entrySet()) {
                List<AgentDataRedis> val = (ArrayList<AgentDataRedis>) entry.getValue();
                System.out.println("Weightage :" + entry.getKey());
                for (AgentDataRedis agentDataRedis : val) {
                    System.out.println("User Name :" + agentDataRedis.getFirstName() + "	User id :" + agentDataRedis.getUserId());
                }
                System.out.println("");
            }
            System.out.println("Agents with highest weightage: " + lastKey);
            for (AgentDataRedis agentDataRedis : highWeightageAgentList) {
                System.out.println("User Name :" + agentDataRedis.getFirstName() + "	User id :" + agentDataRedis.getUserId());
            }
            System.out.println("");

            //End  To be Removed After Testing

            //***************************************List of Least On Going Agents************************************************

            Comparator<AgentDataRedis> comparator = Comparator.<AgentDataRedis, Integer>comparing(agent -> Integer.parseInt(agent.getChat().getTotal_ongoing()));
            highWeightageAgentList.sort(comparator);
            String totalOngoing = highWeightageAgentList.get(0).getChat().getTotal_ongoing();

            List<AgentDataRedis> leastOnGoingAgents = new ArrayList();
            for (AgentDataRedis agentDataRedis : highWeightageAgentList) {
                if (totalOngoing.equalsIgnoreCase(agentDataRedis.getChat().getTotal_ongoing())) {
                    leastOnGoingAgents.add(agentDataRedis);
                } else
                    break;
            }

            //Start To be Removed After Testing

            System.out.println("Agents with Least on going Chats ");
            for (AgentDataRedis agentDataRedis : highWeightageAgentList) {
                System.out.println("User Name :" + agentDataRedis.getFirstName());
                System.out.println("user id : " + agentDataRedis.getUserId() + "	On Going Chats :" + agentDataRedis.getChat().getTotal_ongoing() + "		Last chat End Time" + agentDataRedis.getChat().getLast_chat_end_time());
            }
            System.out.println("");

            //End  To be Removed After Testing


            /*
             * for (AgentDataRedis agentDataRedis : highWeightageAgentList) {
             * System.out.println(""+"User id :"+agentDataRedis.getUserId()); }
             */

            //***************************************Find The Most idle Agent****************************************************

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));   // This line converts the given date into UTC time zone
            java.util.Date oldDate = new Date();

            for (AgentDataRedis agentDataRedis : leastOnGoingAgents) {
                if (StringUtils.isEmpty(agentDataRedis.getChat().getLast_chat_end_time())) {
                    bestAgent = agentDataRedis;
                    break;
                }
                final java.util.Date dateObj = sdf.parse(agentDataRedis.getChat().getLast_chat_end_time());
                if (dateObj.before(oldDate)) {
                    oldDate = dateObj;
                    bestAgent = agentDataRedis;
                }
                System.out.println("Chat end time of user " + agentDataRedis.getUserId() + " " + dateObj.toString());
            }
        } catch (NoSuchElementException e) {
            errorMessage = "Agents are not available";
        } catch (ParseException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
            LOGGER.error("Error : " + e.getMessage());
        } catch (java.text.ParseException e) {
            // TODO Auto-generated catch block
            LOGGER.error("Error : " + e.getMessage());
//            e.printStackTrace();
        }
        return bestAgent;
    }

}
