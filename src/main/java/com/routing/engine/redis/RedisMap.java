package com.routing.engine.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RedisMap {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisMap.class);

    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public RedisMap(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /*
     * private ConfigurableApplicationContext ctx = new
     * AnnotationConfigApplicationContext(RedisMessageTemplate.class);
     *
     * @SuppressWarnings("unchecked") private RedisTemplate<String, Object>
     * redisTemplate = (RedisTemplate<String, Object>) ctx.getBean("redisTemplate");
     */

    public void put(String hashName, String key, String value) {
        redisTemplate.opsForHash().put(hashName, key, value);
    }
    public Object getValue(String hash, String field) {
        String hashName = "\"" + hash + "\"";
        String key = "\"" + field + "\"";
        try {
            if (redisTemplate.opsForHash().hasKey(hashName, key)) {
                Object value = redisTemplate.opsForHash().get(hashName, key);
//                LOGGER.info("Value for hashName '{}' and key '{}' is: {}", hashName, key, value);
                return value;
            } else {
                LOGGER.info("Key '{}' and field '{}' not present in Redis. Returning null.", hashName, key);
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error retrieving value from Redis. HashName: {}, Key: {}", hashName, key, e);
            return null;
        }
    }


    public Boolean hasKey(String hashName, String key) {
        String hash = "\"" + hashName + "\"";
        String field = "\"" + key + "\"";
        return redisTemplate.opsForHash().hasKey(hash, field);
    }

    public Boolean delete(String hashName) {
        String hash = "\"" + hashName + "\"";
        return redisTemplate.opsForHash().getOperations().delete(hash);
    }

    public List<Object> getAllKeys(String hashName) {
        return redisTemplate.opsForHash().values(hashName);
    }

    public Long getsize(String hashName) {
        String hash = "\"" + hashName + "\"";
        return redisTemplate.opsForHash().size(hash);
    }

    public String getValueFromHash(String hashKey, String field) {
        return redisTemplate.opsForHash().get(hashKey, field).toString();
    }
}
