package com.lsb.webshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;

@Configuration
public class RedisConfig {

    // Cấu hình cache (bạn đã có)
    @Bean
    public org.springframework.data.redis.cache.RedisCacheConfiguration redisCacheConfiguration() {
        return org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues()
                .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext
                        .SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    // 👇 Thêm mới bean RedisTemplate vào đây
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Đặt serializer để key/value dễ đọc trong Redis
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}
