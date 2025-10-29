package com.gj.dev_note.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gj.dev_note.note.common.PageEnvelope;
import com.gj.dev_note.note.response.NoteResponse;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory cf, ObjectMapper springOm) {
        var om = springOm.copy()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        var keySer = RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer());
        var valSer = RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer(om));

        var defaultCfg = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(keySer)
                .serializeValuesWith(valSer)
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> configMap = getConfigMap(om, defaultCfg);
        return RedisCacheManager.builder(cf)
                .cacheDefaults(defaultCfg)
                .withInitialCacheConfigurations(configMap)
                .build();
    }

    private static Map<String, RedisCacheConfiguration> getConfigMap(ObjectMapper om, RedisCacheConfiguration defaultCfg) {
        var noteSer = new Jackson2JsonRedisSerializer<>(om, NoteResponse.class);
        var noteCfg = defaultCfg.serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(noteSer)
        );

        var pageType = om.getTypeFactory()
                .constructParametricType(PageEnvelope.class, NoteResponse.class);
        var pageSer = new Jackson2JsonRedisSerializer<>(om, pageType);
        var pageCfg = defaultCfg.serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(pageSer)
        );

        return Map.of(
                "noteById", noteCfg,
                "allNote", pageCfg
        );
    }

//    플리모픽 타입 정보 활성화 (전역 제네릭 직렬화기)
//    @Bean
//    public CacheManager cacheManager(RedisConnectionFactory cf, ObjectMapper springOm) {
//        var ptv = BasicPolymorphicTypeValidator.builder()
//                // 우리 도메인/스프링 데이터 도메인만 허용 범위로
//                .allowIfSubType("com.gj")
//                .allowIfSubType("org.springframework.data.domain")
//                .build();
//
//        var om = springOm.copy()
//                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
//
//        var keySer = RedisSerializationContext.SerializationPair
//                .fromSerializer(new StringRedisSerializer());
//        var valSer = RedisSerializationContext.SerializationPair
//                .fromSerializer(new GenericJackson2JsonRedisSerializer(om));
//
//        var defaultCfg = RedisCacheConfiguration.defaultCacheConfig()
//                .serializeKeysWith(keySer)
//                .serializeValuesWith(valSer)
//                .entryTtl(Duration.ofMinutes(10))
//                .disableCachingNullValues();
//
//        return RedisCacheManager.builder(cf)
//                .cacheDefaults(defaultCfg)
//                .build();
//    }
//
}
