package com.upgrad.demo.configuration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration.JedisClientConfigurationBuilder;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@PropertySource("classpath:application.properties")
public class RedisConfig {
	@Value("${spring.data.redis.host}")
	private String hostname;
	
	@Bean
	JedisConnectionFactory jedisConnectionFactory() {
	    RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration();
	    redisConfiguration.setHostName(hostname);

	    JedisClientConfigurationBuilder jedisClientConfiguration = JedisClientConfiguration.builder();
	    jedisClientConfiguration.connectTimeout(Duration.ofMillis(Long.parseLong("10000")));

	    return new JedisConnectionFactory(redisConfiguration, jedisClientConfiguration.build());
	}

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
	    RedisTemplate<String, Object> template = new RedisTemplate<>();
	    template.setConnectionFactory(jedisConnectionFactory());
	    template.setEnableTransactionSupport(true);
	    template.afterPropertiesSet();
    
	    return template;
	}
    
    @Bean
	public StringRedisTemplate stringRedisTemplate() {
		StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(jedisConnectionFactory());
		stringRedisTemplate.setEnableTransactionSupport(true);
		return stringRedisTemplate;
	}	
    
    public RedisConnection redisConnection() {
    	return redisTemplate().getConnectionFactory().getConnection();
    }
}
