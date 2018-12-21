package com.example.DistLockdemo;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import com.example.redis.RedisJedisPool;
/**
 * DistributedLock
 * 
 * */
@Component
public final class DistributedLock {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private final static String splitflag = ":";
	@Autowired
	private RedisJedisPool redisJedisPool;
	
	/**
     * 加锁
     * @param lockName       锁的key
     * @param lockIdentifier       锁拥有者身份 ip+port+thread
     * @param acquireTimeout 获取超时时间
     * @param timeout        锁的超时时间
     * @return 锁标识
     */
	public String lockWithTimeout(String lockName,String lockIdentifier, long acquireTimeout, long timeout) {
		Jedis conn = null;
		String retIdentifier = null;
		try {
			conn = redisJedisPool.getJedisPool().getResource();
			long currentTime = System.currentTimeMillis();//该时间需要从统一的时间服务器取值

			// 超时时间，上锁后超过此时间则自动释放锁
			int lockExpire = (int) (timeout/1000);
			long lockExpireAt = currentTime + timeout;
			// 获取锁的超时时间，超过这个时间则放弃获取锁
			long end = currentTime + acquireTimeout;
			//锁拥有者身份 ip+port+method
//			String identifier = UUID.randomUUID().toString();
			// 锁名，即key值
			String lockKey = "lock-"+lockName;
			//锁value  ：ip+port+method+"-"+ lockExpireAt;
			String lockValue = lockIdentifier +splitflag+ lockExpireAt;
			while (System.currentTimeMillis() < end) {
				if (conn.setnx(lockKey, lockValue) == 1) {//获取到锁
					conn.expire(lockKey, lockExpire);
//					retIdentifier = identifier;
					return lockValue;
				}else {//锁已存在，判断是否失效,是否可以重入
					String oLockValue = conn.get(lockKey);
					if (oLockValue != null) {
						long oLockExpireAt = Long.parseLong(StringUtils.split(oLockValue, splitflag)[1]);
						if (oLockExpireAt < System.currentTimeMillis()) {//锁失效 开启redis事务
							conn.watch(lockKey);
							if (oLockValue.equals(conn.get(lockKey))) {
								Transaction transaction = conn.multi();
								transaction.set(lockKey, lockValue);
								if (transaction.exec() != null) {//抢锁成功
									return lockValue;
								}
							}
							conn.unwatch();
						}else {//锁有效 判断是否可重入
							
						}
					}
				}
				
				// 返回-1代表key没有设置超时时间，为key设置一个超时时间
                if (conn.ttl(lockKey) == -1) {
                    conn.expire(lockKey, lockExpire);
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }


			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if (conn != null) {
				conn.close();
			}
		}
		
		return retIdentifier;
	}
	
	/**
     * 释放锁
     * @param lockName   锁的key
     * @param identifier 释放锁的标识
     * @return
     */
	public boolean releaseLock(String lockName, String identifier) {
		Jedis conn = null;
		boolean retVal = false;
		try {
			conn = redisJedisPool.getJedisPool().getResource();
			// 锁名，即key值
			String lockKey = "lock-"+lockName;
			conn.watch(lockKey);
			if (!identifier.equals(conn.get(lockKey))) {//目前锁已被其他处理
				conn.unwatch();
				return retVal;
			}
			Transaction transaction = conn.multi();
			transaction.del(lockKey);
			if (transaction.exec() != null) {//释放锁成功
				retVal = true;
			}
			conn.unwatch();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if (conn != null) {
				conn.close();
			}
		}
		
		return retVal;
	}
}
