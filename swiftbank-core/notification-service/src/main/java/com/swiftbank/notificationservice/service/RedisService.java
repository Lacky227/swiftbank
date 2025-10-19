package com.swiftbank.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final StringRedisTemplate stringRedisTemplate;
    @Value("${ttl.minutes}")
    private byte TTL_MINUTES;

    public void saveToken(String email, String hashToken) {
        stringRedisTemplate.opsForValue().set(
                hashToken,
                email,
                TTL_MINUTES,
                TimeUnit.MINUTES
        );
    }
}
