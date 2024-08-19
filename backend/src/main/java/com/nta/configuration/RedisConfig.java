package com.nta.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    String REDIS_HOST;

    @Value("${spring.data.redis.port}")
    String REDIS_PORT;

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {

        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setHostName(REDIS_HOST);
        jedisConnectionFactory.setPort(Integer.parseInt(REDIS_PORT));
        return jedisConnectionFactory;
    }

    @Bean
    @Scope("prototype")
    GeoOperations<String, String> geoOperations(RedisTemplate<String,String> template) {
        return template.opsForGeo();
    }

    @Bean
    RedisTemplate<String,Object> redisTemplate() {
        RedisTemplate<String,Object> template  = new RedisTemplate<>();
        template .setConnectionFactory(jedisConnectionFactory());

        JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
        template.setDefaultSerializer(jdkSerializationRedisSerializer);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jdkSerializationRedisSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}
