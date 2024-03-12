package com.routing.engine.util;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.routing.engine.request.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class QueueEngineServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueEngineServices.class);


    public String pushMessage(QueueMessage msg, String chatSessionId, String phoneNumber, String pushMessageUrl,String tenantId) {
        boolean success = true;
        try {
            String test = "{\t\"queueName\":\"" + msg.getQueueName() + "\",\r\n\t\"routingKey\":\""
                    + msg.getRoutingKey() + "\",\r\n\t\"message\":\"{\\\"chatSessionId\\\":\\\""
                    + chatSessionId + "\\\",\\\"chatUserPhoneNumber\\\":\\\""
                    + phoneNumber + "\\\"}\",\r\n\t\"priority\":"
                    + msg.getPriority() + "\r\n\t}";

            Unirest.setTimeouts(0, 0);
            HttpResponse<String> response = Unirest.post(pushMessageUrl)
                    //HttpResponse<String> response = Unirest.post("http://localhost:9293/queueEngine/queue/pushMessage")
                    //HttpResponse<String> response = Unirest.post("http://gway.release.inaipi.ae/queueEngine/queue/pushMessage")
                    //HttpResponse<String> response = Unirest.post("http://172.31.8.122:9293/queueEngine/queue/pushMessage")
                    .header("Content-Type", "application/json")
                    .header("TENANTID", tenantId)
                    .body("{\t\"queueName\":\"" + msg.getQueueName() + "\",\r\n\t\"routingKey\":\""
                            + msg.getRoutingKey() + "\",\r\n\t\"message\":\"{\\\"chatSessionId\\\":\\\""
                            + chatSessionId + "\\\",\\\"chatUserPhoneNumber\\\":\\\""
                            + phoneNumber + "\\\"}\",\r\n\t\"priority\":"
                            + msg.getPriority() + "\r\n\t}")
                    .asString();
            System.out.println("test:" + test);

            System.out.println("queueName:" + msg.getQueueName());
            LOGGER.info("Push Message response : "+response.getBody());
            LOGGER.info("url : "+pushMessageUrl);
            return response.getBody();
        } catch (UnirestException e) {
            // TODO Auto-generated catch block
            LOGGER.error("" + ":pushMessage: Exception: " + e.getMessage());
//            e.printStackTrace();
            success = false;
        }

        if (success)
            return "Execution succeeded";
        else
            return "Execution Failed";
    }

    public String popMessage(String queueName, String popMessageUrl, String tenantId) {
        boolean success = true;
        Unirest.setTimeouts(0, 0);
        try {
            Unirest.setTimeouts(0, 0);
            HttpResponse<String> response = Unirest.post(popMessageUrl)
                    //HttpResponse<String> response = Unirest.post("http://localhost:9293/queueEngine/queue/popMessage")
                    //HttpResponse<String> response = Unirest.post("http://gway.release.inaipi.ae/queueEngine/queue/popMessage")
                    //	HttpResponse<String> response = Unirest.post("http://172.31.8.122:9293/queueEngine/queue/popMessage")
                    .header("Content-Type", "application/json")
//			  .header("TENANTID", "tenant_001")
                    .header("TENANTID", tenantId)
                    .body("{\r\n    \"queueName\": \"" + queueName + "\"\r\n}")
                    .asString();
            System.out.println("queueName:" + queueName);
            System.out.println("Pop Message response : "+response.getBody());
            return response.getBody();
        } catch (UnirestException e) {
            // TODO Auto-generated catch block
            LOGGER.error("" + ":popMessage: Exception: " + e.getMessage());
//            e.printStackTrace();
            success = false;
        }
        if (success)
            return "Execution succeeded";
        else
            return "Execution Failed";
    }

    public String getQueueName(String channel, String routingType, String queueSubName, String tenantId) {
//		return "tenantId_"+tenantId+"Test_"+channel+"_"+channel+"_"+routingType+"_"+queueSubName+"_durable";
        return "TenantId_" + tenantId + "_Test_" + channel + "_" + channel + "_" + routingType + "_" + queueSubName + "_durable";
    }

    public String checkQueueIsAvailable(String queueName, String checkApiUrl, String tenantId) {
        Unirest.setTimeouts(0, 0);
        try {
            Unirest.setTimeouts(0, 0);
            HttpResponse<String> response = Unirest.post(checkApiUrl)
                    .header("Content-Type", "application/json")
                    .header("TENANTID", tenantId)
                    .body("{\"queueName\": \"" + queueName + "\"}")
                    .asString();

            LOGGER.info("Checking the queueName is available or not : " + queueName);
            if (response.getBody().contains("true")) {
                return "true";
            } else {
                return "false";
            }
        } catch (UnirestException e) {
            LOGGER.error(queueName + ": Error checking the queue availability: " + e.getMessage());
            return "false";
        }
    }
}
