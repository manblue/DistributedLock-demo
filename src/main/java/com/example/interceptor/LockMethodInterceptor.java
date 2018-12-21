package com.example.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.DistLockdemo.DistributedLock;
import com.example.annotation.DistLock;
/**
 * 注释了 @DistLock 的方法拦截器
 * */
public class LockMethodInterceptor implements MethodInterceptor {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private DistributedLock distributedLock;
	
	@Override
	public Object invoke(MethodInvocation arg0) throws Throwable {
		DistLock lock = arg0.getMethod().getAnnotation(DistLock.class);
		if (lock == null) {
			logger.error("class:{} method:{} not distlock ", arg0.getThis(), arg0.getMethod().getName());
			return arg0.proceed();
		}
		logger.error("class:{} method:{} get distlock begin", arg0.getThis(), arg0.getMethod().getName());
		//获取超时时间
		long acquireTimeout = lock.acquireTimeout();
		//锁的有效时间
		long lockExpire = lock.lockExpire();
		//锁拥有者身份 ip+port+thread
		String lockIdentifier = Thread.currentThread().getName();
		//锁名称
		String lockName = arg0.getMethod().getName();
		//获取锁逻辑
		String retIdentifier = ContextUtil.getContext().getBean(DistributedLock.class).lockWithTimeout(lockName, lockIdentifier, acquireTimeout, lockExpire);
		if (retIdentifier == null) {//获取锁逻辑失败
			logger.error("class:{} method:{} lockIdentifier:{} get distributedLock failed", arg0.getThis(), arg0.getMethod().getName(),lockIdentifier);
			throw new Exception("get distributedLock failed");
		}
		logger.error("class:{} method:{} lockIdentifier:{} get distributedLock ok", arg0.getThis(), arg0.getMethod().getName(),lockIdentifier);
		//TODO
		Object retuslt = arg0.proceed();
		//释放锁逻辑
		//TODO
		if (ContextUtil.getContext().getBean(DistributedLock.class).releaseLock(lockName, retIdentifier)) {
			logger.error("class:{} method:{} lockIdentifier:{} releaseLock ok", arg0.getThis(), arg0.getMethod().getName(),lockIdentifier);
		}else {//业务超时
			logger.error("class:{} method:{} lockIdentifier:{} releaseLock err", arg0.getThis(), arg0.getMethod().getName(),lockIdentifier);
			throw new Exception("releaseLock distributedLock failed");
		}
		return retuslt;
	}

}
