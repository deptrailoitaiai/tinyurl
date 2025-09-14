package org.example.config.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CacheConfig {

    @Value("${cache.token.verification.ttl}")
    private long verificationTokenTtl;

    @Value("${cache.token.reset-password.ttl}")
    private long resetPasswordTokenTtl;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(3600)) // Default 1 hour
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Verification token cache - 30 minutes
        cacheConfigurations.put("verificationTokens", defaultCacheConfig.entryTtl(Duration.ofSeconds(verificationTokenTtl)));
        
        // Reset password token cache - 15 minutes
        cacheConfigurations.put("resetPasswordTokens", defaultCacheConfig.entryTtl(Duration.ofSeconds(resetPasswordTokenTtl)));
        
        // User sessions cache - 2 hours
        cacheConfigurations.put("userSessions", defaultCacheConfig.entryTtl(Duration.ofSeconds(7200)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}