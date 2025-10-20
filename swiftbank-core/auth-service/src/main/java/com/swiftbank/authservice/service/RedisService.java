package com.swiftbank.authservice.service;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RedisService {
    private final StringRedisTemplate stringRedisTemplate;

    public String getValue(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }
    public void deleteValue(String key) {
        stringRedisTemplate.delete(key);
    }
}
