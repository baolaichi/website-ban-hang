package com.lsb.webshop.config;

// ===== BẮT ĐẦU CÁC IMPORT MỚI =====
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// ===== KẾT THÚC CÁC IMPORT MỚI =====

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import java.time.Duration;

@Configuration
public class CacheConfig {

    // (Các hằng số định nghĩa tên Cache và thời gian sống - Giữ nguyên)
    public static final String CACHE_PRODUCTS = "products";
    public static final String CACHE_PRODUCT = "product";
    public static final String CACHE_CATEGORIES = "categories";
    public static final String CACHE_RECOMMEND = "recommend";

    private static final Duration TTL_30_MINUTES = Duration.ofMinutes(30);
    private static final Duration TTL_5_MINUTES = Duration.ofMinutes(5);
    private static final Duration TTL_1_HOUR = Duration.ofHours(1);

    /**
     * Cấu hình Redis Cache Manager
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {

        // ===== BẮT ĐẦU SỬA LỖI (Cấu hình ObjectMapper) =====

        // 1. Tạo một ObjectMapper (bộ chuyển đổi JSON) tùy chỉnh
        ObjectMapper objectMapper = new ObjectMapper()
                // 2. ĐĂNG KÝ "phiên dịch viên" JavaTimeModule (để xử lý LocalDateTime)
                .registerModule(new JavaTimeModule())
                // 3. (Tùy chọn) Cấu hình để nó không ghi ngày tháng dưới dạng số (timestamps)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 4. Tạo một Serializer (bộ tuần tự hóa) SỬ DỤNG ObjectMapper đã cấu hình
        GenericJackson2JsonRedisSerializer jsonRedisSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // 5. Tạo một "Cặp" (Pair) tuần tự hóa để dùng chung
        SerializationPair<Object> jsonSerializerPair =
                SerializationPair.fromSerializer(jsonRedisSerializer);

        // ===== KẾT THÚC SỬA LỖI =====


        return (builder) -> builder
                // Cấu hình chung: Dùng Serializer đã cấu hình
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(TTL_30_MINUTES)
                        .serializeValuesWith(jsonSerializerPair)) // <-- SỬA DÙNG CẶP NÀY

                // Cấu hình riêng cho từng "ngăn" cache
                .withCacheConfiguration(CACHE_PRODUCTS,
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(TTL_30_MINUTES)
                                .serializeValuesWith(jsonSerializerPair)) // <-- SỬA DÙNG CẶP NÀY

                .withCacheConfiguration(CACHE_PRODUCT,
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(TTL_1_HOUR)
                                .serializeValuesWith(jsonSerializerPair)) // <-- SỬA DÙNG CẶP NÀY

                .withCacheConfiguration(CACHE_CATEGORIES,
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofHours(6))
                                .serializeValuesWith(jsonSerializerPair)) // <-- SỬA DÙNG CẶP NÀY

                .withCacheConfiguration(CACHE_RECOMMEND,
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(TTL_5_MINUTES)
                                .serializeValuesWith(jsonSerializerPair)); // <-- SỬA DÙNG CẶP NÀY
    }
}