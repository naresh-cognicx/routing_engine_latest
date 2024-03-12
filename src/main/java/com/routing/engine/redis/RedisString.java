package com.routing.engine.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisString {

	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	
	public String findById(String id) {
	    return redisTemplate.opsForValue().get(id);
	}
	
	public void findById(String id,String value) {
	     redisTemplate.opsForValue().set(id, value);
	}
}
