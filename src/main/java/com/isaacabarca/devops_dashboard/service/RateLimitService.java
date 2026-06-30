package com.isaacabarca.devops_dashboard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final int LOGIN_MAX_ATTEMPTS = 5;
    private static final int GLOBAL_MAX_ATTEMPTS = 100;
    private static final int WINDOW_MINUTES = 1;

    public boolean tryConsume(String key) {
        try {
            String redisKey = "rate_limit:" + key;
            String attempts = redisTemplate.opsForValue().get(redisKey);

            int maxAttempts;
            if (key.startsWith("global:")) {
                maxAttempts = GLOBAL_MAX_ATTEMPTS;
            } else {
                maxAttempts = LOGIN_MAX_ATTEMPTS;
            }

            if (attempts == null) {
                redisTemplate.opsForValue().set(redisKey, "1", WINDOW_MINUTES, TimeUnit.MINUTES);
                return true;
            }

            int currentAttempts = Integer.parseInt(attempts);

            if (currentAttempts >= maxAttempts) {
                return false;
            }

            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.MILLISECONDS);
            redisTemplate.opsForValue().set(redisKey, String.valueOf(currentAttempts + 1),
                                            ttl, TimeUnit.MILLISECONDS);
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    public void clearBucket(String key) {
        String redisKey = "rate_limit:" + key;
        try {
            redisTemplate.delete(redisKey);
        } catch (Exception e) {
            // Redis no disponible, ignorar
        }
    }
}