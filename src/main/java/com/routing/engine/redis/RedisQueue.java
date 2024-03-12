package com.routing.engine.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisQueue {

	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	
		public Long getQueueSize(String queueName) {	
			return redisTemplate.opsForList().size(queueName);
		}
		
		public String popLeft(String queueName){
			return redisTemplate.opsForList().leftPop(queueName);
		}
		
		public void pushRight(String queueName,String chatData) {
			redisTemplate.opsForList().rightPush(queueName, chatData);
		}
		
		 public void remove(String queueName,String chatData) {
			redisTemplate.opsForList().remove(queueName, 1, chatData);
			
		}		
}
