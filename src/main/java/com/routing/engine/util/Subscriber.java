package com.routing.engine.util;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.routing.engine.services.RoutingEngineService;
import com.routing.engine.services.impl.RoutingEngineServiceImpl;

@Component
public class Subscriber implements MessageListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Subscriber.class);
	
	@Autowired
	RoutingEngineService routingEngineService;
	
	 @Override
	    public void onMessage(Message message, byte[] pattern) {
		 
	        System.out.println("Subscriber >>"+message);
	        String channelName = new String(message.getChannel(), StandardCharsets.UTF_8);
	        if("agent-activity".equalsIgnoreCase(channelName)) {
//	        	routingEngineService.availableAgent(message.toString());
	        	LOGGER.info( "12345" +":Subscriber:" + message );
	        }
	        System.out.println(channelName);
	    }

}
