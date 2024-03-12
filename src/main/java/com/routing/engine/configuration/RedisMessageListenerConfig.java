package com.routing.engine.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

import com.routing.engine.util.Subscriber;

@Configuration
public class RedisMessageListenerConfig {
	
	@Value("${routing.topic.agent-activity}")
	private String agentActivityTopic;
	
	@Autowired
	RedisConfigurationBean redisConfigurationBean;
	
	@Autowired
	private Subscriber subscriber;
	
    @Bean
    MessageListenerAdapter messageListener() {
        return new MessageListenerAdapter( subscriber );
    }

    @Bean
    RedisMessageListenerContainer redisContainer() {
        final RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory( redisConfigurationBean.lettuceConnectionFactory() );
  //      container.addMessageListener( messageListener(), topic() );
        container.addMessageListener(messageListener(), topicList());
        return container;
    }

    @Bean
    ChannelTopic topic() {
        return new ChannelTopic( "agent-activity-demo" );
    }
    
    @Bean
    List<ChannelTopic> topicList() {
    	List<ChannelTopic> topicList = new ArrayList();
    	topicList.add(new ChannelTopic( agentActivityTopic ));
    	topicList.add(new ChannelTopic( "testdemo" ));
        return topicList;
    }

}
