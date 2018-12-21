package com.example.redis;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * redis 连接
 * */
@Component
public final class RedisJedisPool {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private static JedisPool jedisPool;
	@Autowired
	private RedisProperties redisProperties;
	
	@PostConstruct
	public void init() {
		logger.error("{} init host:{} port:{} MaxTotal:{} MaxIdle:{} MaxWaitMillis:{} begin",this,redisProperties.getHost(), redisProperties.getPort(),
				redisProperties.getMaxTotal(),redisProperties.getMaxIdle(),redisProperties.getMaxWaitMillis());

		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(redisProperties.getMaxTotal());
		config.setMaxIdle(redisProperties.getMaxIdle());
		config.setMaxWaitMillis(redisProperties.getMaxWaitMillis());
		config.setTestOnBorrow(true);
		
		jedisPool = new JedisPool(config, redisProperties.getHost(), redisProperties.getPort(), redisProperties.getTimeout(),
				redisProperties.getPassword(),redisProperties.getDatabase());
		logger.error("{} init host:{} port:{} asking:{}end",this,redisProperties.getHost(), redisProperties.getPort());
	}

	public static JedisPool getJedisPool() {
		return jedisPool;
	}
	
}
