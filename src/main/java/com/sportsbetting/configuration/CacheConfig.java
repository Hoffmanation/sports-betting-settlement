package com.sportsbetting.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.time.Duration;
import java.util.Map;

@Configuration
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(
        RedisConnectionFactory connectionFactory,
        @Value("${app.cache.ttl-seconds:300}") long ttlSeconds
    ) {
        GenericJacksonJsonRedisSerializer jsonSerializer = GenericJacksonJsonRedisSerializer.builder()
            .enableDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                    .allowIfSubType("com.sportsbetting.")
                    .allowIfSubType("java.util.")
                    .allowIfSubType("java.math.")
                    .allowIfSubType("java.time.")
                    .build())
            .build();

        RedisCacheConfiguration baseConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
            .disableCachingNullValues()
            .entryTtl(Duration.ofSeconds(ttlSeconds));

        Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
            "betsById", baseConfig,
            "betsSearch", baseConfig
        );

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(baseConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
}
