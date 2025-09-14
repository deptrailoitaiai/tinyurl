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

    @Value("${cache.analytics.daily-stats.ttl}")
    private long dailyStatsTtl;

    @Value("${cache.analytics.summary.ttl}")
    private long summaryTtl;

    @Value("${cache.analytics.url-ownership.ttl}")
    private long urlOwnershipTtl;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(3600)) // Default 1 hour
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Daily stats cache - 2 hours (refreshed after batch processing)
        cacheConfigurations.put("dailyStats", defaultCacheConfig.entryTtl(Duration.ofSeconds(dailyStatsTtl)));
        
        // Analytics summary cache - 1 hour (frequently accessed)
        cacheConfigurations.put("analyticsSummary", defaultCacheConfig.entryTtl(Duration.ofSeconds(summaryTtl)));
        
        // URL ownership verification cache - 30 minutes (external service call)
        cacheConfigurations.put("urlOwnership", defaultCacheConfig.entryTtl(Duration.ofSeconds(urlOwnershipTtl)));
        
        // External service responses cache - 10 minutes
        cacheConfigurations.put("externalService", defaultCacheConfig.entryTtl(Duration.ofSeconds(600)));
        
        // Latest analytics cache - 15 minutes (frequently requested)
        cacheConfigurations.put("latestAnalytics", defaultCacheConfig.entryTtl(Duration.ofSeconds(900)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}